package com.iusofts.agentplus.aiflow.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iusofts.agentplus.aiflow.entity.AiFlow;
import com.iusofts.agentplus.aiflow.entity.AiFlowRuntime;
import com.iusofts.agentplus.aiflow.entity.AiFlowRuntimeNode;
import com.iusofts.agentplus.aiflow.entity.AiFlowVersion;
import com.iusofts.agentplus.aiflow.enums.NodeRunStatusEnum;
import com.iusofts.agentplus.aiflow.enums.RunStatusEnum;
import com.iusofts.agentplus.aiflow.interfaces.IAiFlowExecutorService;
import com.iusofts.agentplus.aiflow.mapper.AiFlowMapper;
import com.iusofts.agentplus.aiflow.mapper.AiFlowRuntimeMapper;
import com.iusofts.agentplus.aiflow.mapper.AiFlowRuntimeNodeMapper;
import com.iusofts.agentplus.aiflow.mapper.AiFlowVersionMapper;
import com.iusofts.agentplus.aiflow.utils.AiFlowCommonUtils;
import com.iusofts.agentplus.aiflow.vo.FlowExecuteResult;
import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.Workflow;
import com.iusofts.agentplus.aiflow.vo.workflow.config.WorkflowConfig;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.engine.WorkflowEngine;
import com.iusofts.agentplus.engine.WorkflowExecutionResult;
import com.iusofts.agentplus.engine.context.NodeExecutionStatus;
import com.iusofts.agentplus.engine.context.NodeOutput;
import com.iusofts.agentplus.engine.context.NodeTiming;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AI流程公共执行服务。
 * 抽离流程执行的通用逻辑，供试运行和对话流智能体共用。
 * 负责：加载版本 -> 执行流程 -> 落库运行记录 -> 返回执行结果
 *
 * @author Ivan Shen
 */
@Slf4j
@Service
public class AiFlowExecutorServiceImpl implements IAiFlowExecutorService {

    @Resource
    private WorkflowEngine workflowEngine;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private AiFlowMapper aiFlowMapper;
    @Resource
    private AiFlowVersionMapper aiFlowVersionMapper;
    @Resource
    private AiFlowRuntimeMapper aiFlowRuntimeMapper;
    @Resource
    private AiFlowRuntimeNodeMapper aiFlowRuntimeNodeMapper;

    /**
     * 执行流程（公共入口）。
     * 加载最新发布版本 -> 执行 -> 落库运行记录和节点明细 -> 返回结果。
     *
     * @param flowId     流程ID
     * @param inputs     输入参数
     * @param operatorId 操作人ID
     * @param orgId      组织ID
     * @param trialFlag  试运行标记 0:正式 1:流程试运行 2:节点试运行
     * @return 执行结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FlowExecuteResult executeFlow(Long flowId,
                                         Map<String, Object> inputs,
                                         Long operatorId,
                                         Integer orgId,
                                         int trialFlag) {
        // 1. 获取最新已发布版本
        AiFlow aiFlow = aiFlowMapper.selectById(flowId);
        if (aiFlow == null) {
            throw new SystemBusinessException("流程不存在");
        }
        String onlineVersion = aiFlow.getOnlineVersion();
        if (onlineVersion == null || onlineVersion.isBlank()) {
            throw new SystemBusinessException("流程无已发布版本，请先发布");
        }

        var versionWrapper = Wrappers.<AiFlowVersion>lambdaQuery();
        versionWrapper.eq(AiFlowVersion::getFlowId, flowId)
                .eq(AiFlowVersion::getVersionNo, onlineVersion);
        AiFlowVersion version = aiFlowVersionMapper.selectOne(versionWrapper);
        if (version == null) {
            throw new SystemBusinessException("流程发布版本不存在");
        }

        // 2. 反序列化
        Workflow workflow = AiFlowCommonUtils.deserializeWorkflow(version.getFlowJson(), objectMapper);
        WorkflowConfig config = AiFlowCommonUtils.deserializeConfig(version.getConfigJson(), objectMapper);

        // 3. 创建运行实例(占位 traceId,执行后回填真实 OTel traceId)
        String placeholderTraceId = AiFlowCommonUtils.newPlaceholderTraceId();
        AiFlowRuntime runtime = newRuntime(version, placeholderTraceId, inputs, operatorId, trialFlag);
        runtime.setFlowName(aiFlow.getName());
        aiFlowRuntimeMapper.insert(runtime);

        LocalDateTime start = runtime.getStartTime();
        FlowExecuteResult result = new FlowExecuteResult();
        result.setRuntimeId(runtime.getId());
        result.setFlowId(flowId);

        try {
            // 4. 执行工作流(引擎内开 root span,返回真实 OTel traceId)
            WorkflowExecutionResult execResult = workflowEngine.execute(
                    workflow,
                    config,
                    inputs,
                    placeholderTraceId,
                    flowId,
                    operatorId,
                    orgId,
                    trialFlag
            );

            // 回填真实 traceId
            String traceId = execResult.getRunId();
            runtime.setTraceId(traceId);
            result.setTraceId(traceId);

            // 5. 落库所有节点执行状态
            List<AiFlowRuntimeNode> nodeEntities = new ArrayList<>();
            List<FlowExecuteResult.FlowNodeResult> nodeResults = new ArrayList<>();
            Map<String, String> nodeTypeMap = buildNodeTypeMap(workflow);
            Map<String, String> nodeNameMap = buildNodeNameMap(workflow);
            Map<String, NodeTiming> timings = execResult.getNodeTimings();

            for (Map.Entry<String, NodeExecutionStatus> entry : execResult.getNodeStatus().entrySet()) {
                String nodeId = entry.getKey();
                String nodeType = nodeTypeMap.getOrDefault(nodeId, "");
                String nodeName = nodeNameMap.getOrDefault(nodeId, "");
                NodeOutput output = execResult.getNodeOutputs().get(nodeId);
                Map<String, Object> outputs = output == null ? null : output.getOutputs();
                int nodeStatus = mapNodeStatus(entry.getValue());
                NodeTiming timing = timings == null ? null : timings.get(nodeId);
                LocalDateTime nodeStart = timing == null ? null : timing.getStartTime();
                LocalDateTime nodeEnd = timing == null ? null : timing.getEndTime();
                Long nodeCost = timing == null ? null : timing.getCostMs();

                nodeEntities.add(buildRuntimeNode(runtime.getId(), nodeId, nodeName, nodeType, nodeStatus,
                        null, outputs, null, operatorId, nodeStart, nodeEnd, nodeCost));
                nodeResults.add(buildNodeResult(nodeId, nodeType, nodeStatus, outputs, nodeCost, null));
            }

            for (AiFlowRuntimeNode nodeEntity : nodeEntities) {
                aiFlowRuntimeNodeMapper.insert(nodeEntity);
            }

            // 6. 完成运行实例
            long costMs = Duration.between(start, LocalDateTime.now()).toMillis();
            finishRuntime(runtime, RunStatusEnum.SUCCESS.getCode(), costMs,
                    AiFlowCommonUtils.serialize(execResult.getOutput(), objectMapper), null);

            result.setRunStatus(RunStatusEnum.SUCCESS.getCode());
            result.setOutput(execResult.getOutput());
            result.setCostMs(costMs);
            result.setNodeResults(nodeResults);
            return result;

        } catch (Exception e) {
            long costMs = Duration.between(start, LocalDateTime.now()).toMillis();
            finishRuntime(runtime, RunStatusEnum.FAILED.getCode(), costMs,
                    null, AiFlowCommonUtils.truncate(e.getMessage()));

            result.setRunStatus(RunStatusEnum.FAILED.getCode());
            result.setCostMs(costMs);
            result.setErrorMsg(AiFlowCommonUtils.truncate(e.getMessage()));
            result.setNodeResults(new ArrayList<>());
            throw e;
        }
    }

    /**
     * 根据版本ID执行（供试运行使用）。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FlowExecuteResult executeVersion(Long versionId,
                                            Long flowId,
                                            Map<String, Object> inputs,
                                            Long operatorId,
                                            Integer orgId,
                                            int trialFlag) {
        // 加载指定版本
        AiFlowVersion version;
        if (versionId != null) {
            version = aiFlowVersionMapper.selectById(versionId);
            if (version == null) {
                throw new SystemBusinessException("流程版本不存在");
            }
        } else {
            if (flowId == null) {
                throw new SystemBusinessException("versionId 与 flowId 不能同时为空");
            }
            var wrapper = Wrappers.<AiFlowVersion>lambdaQuery();
            wrapper.eq(AiFlowVersion::getFlowId, flowId);
            wrapper.orderByDesc(AiFlowVersion::getId);
            wrapper.last("LIMIT 1");
            version = aiFlowVersionMapper.selectOne(wrapper);
            if (version == null) {
                throw new SystemBusinessException("流程尚无可运行的版本");
            }
        }

        // 反序列化
        Workflow workflow = AiFlowCommonUtils.deserializeWorkflow(version.getFlowJson(), objectMapper);
        WorkflowConfig config = AiFlowCommonUtils.deserializeConfig(version.getConfigJson(), objectMapper);

        // 创建运行实例(占位 traceId,执行后回填真实 OTel traceId)
        String placeholderTraceId = AiFlowCommonUtils.newPlaceholderTraceId();
        AiFlowRuntime runtime = newRuntime(version, placeholderTraceId, inputs, operatorId, trialFlag);
        AiFlow aiFlow = aiFlowMapper.selectById(version.getFlowId());
        if (aiFlow != null) {
            runtime.setFlowName(aiFlow.getName());
        }
        aiFlowRuntimeMapper.insert(runtime);

        LocalDateTime start = runtime.getStartTime();
        FlowExecuteResult result = new FlowExecuteResult();
        result.setRuntimeId(runtime.getId());
        result.setFlowId(version.getFlowId());

        try {
            // 执行(引擎内开 root span,返回真实 OTel traceId)
            WorkflowExecutionResult execResult = workflowEngine.execute(
                    workflow,
                    config,
                    inputs,
                    placeholderTraceId,
                    version.getFlowId(),
                    operatorId,
                    orgId,
                    trialFlag
            );

            // 回填真实 traceId
            String traceId = execResult.getRunId();
            runtime.setTraceId(traceId);
            result.setTraceId(traceId);

            // 落库节点
            List<AiFlowRuntimeNode> nodeEntities = new ArrayList<>();
            List<FlowExecuteResult.FlowNodeResult> nodeResults = new ArrayList<>();
            Map<String, String> nodeTypeMap = buildNodeTypeMap(workflow);
            Map<String, String> nodeNameMap = buildNodeNameMap(workflow);
            Map<String, NodeTiming> timings = execResult.getNodeTimings();

            for (Map.Entry<String, NodeExecutionStatus> entry : execResult.getNodeStatus().entrySet()) {
                String nodeId = entry.getKey();
                String nodeType = nodeTypeMap.getOrDefault(nodeId, "");
                String nodeName = nodeNameMap.getOrDefault(nodeId, "");
                NodeOutput output = execResult.getNodeOutputs().get(nodeId);
                Map<String, Object> outputs = output == null ? null : output.getOutputs();
                int nodeStatus = mapNodeStatus(entry.getValue());
                NodeTiming timing = timings == null ? null : timings.get(nodeId);
                LocalDateTime nodeStart = timing == null ? null : timing.getStartTime();
                LocalDateTime nodeEnd = timing == null ? null : timing.getEndTime();
                Long nodeCost = timing == null ? null : timing.getCostMs();

                nodeEntities.add(buildRuntimeNode(runtime.getId(), nodeId, nodeName, nodeType, nodeStatus,
                        null, outputs, null, operatorId, nodeStart, nodeEnd, nodeCost));
                nodeResults.add(buildNodeResult(nodeId, nodeType, nodeStatus, outputs, nodeCost, null));
            }
            for (AiFlowRuntimeNode nodeEntity : nodeEntities) {
                aiFlowRuntimeNodeMapper.insert(nodeEntity);
            }

            // 完成
            long costMs = Duration.between(start, LocalDateTime.now()).toMillis();
            finishRuntime(runtime, RunStatusEnum.SUCCESS.getCode(), costMs,
                    AiFlowCommonUtils.serialize(execResult.getOutput(), objectMapper), null);

            result.setRunStatus(RunStatusEnum.SUCCESS.getCode());
            result.setOutput(execResult.getOutput());
            result.setCostMs(costMs);
            result.setNodeResults(nodeResults);
            return result;

        } catch (Exception e) {
            long costMs = Duration.between(start, LocalDateTime.now()).toMillis();
            finishRuntime(runtime, RunStatusEnum.FAILED.getCode(), costMs,
                    null, AiFlowCommonUtils.truncate(e.getMessage()));

            result.setRunStatus(RunStatusEnum.FAILED.getCode());
            result.setCostMs(costMs);
            result.setErrorMsg(AiFlowCommonUtils.truncate(e.getMessage()));
            result.setNodeResults(new ArrayList<>());
            throw e;
        }
    }

    // ============ 内部辅助方法 ============

    private AiFlowRuntime newRuntime(AiFlowVersion version, String traceId,
                                     Map<String, Object> inputs, Long operatorId, int trialFlag) {
        AiFlowRuntime runtime = new AiFlowRuntime();
        runtime.setFlowId(version.getFlowId());
        runtime.setVersionNo(version.getVersionNo());
        runtime.setTraceId(traceId);
        runtime.setRunStatus(RunStatusEnum.RUNNING.getCode());
        runtime.setTrialFlag(trialFlag);
        runtime.setStartTime(LocalDateTime.now());
        runtime.setInputParams(AiFlowCommonUtils.serialize(inputs, objectMapper));
        runtime.setCreateBy(operatorId);
        return runtime;
    }

    private void finishRuntime(AiFlowRuntime runtime, Integer runStatus, long costMs,
                               String outputResult, String errorMsg) {
        runtime.setRunStatus(runStatus);
        runtime.setEndTime(LocalDateTime.now());
        runtime.setCostMs(costMs);
        runtime.setOutputResult(outputResult);
        runtime.setErrorMsg(errorMsg);
        runtime.setUpdateBy(runtime.getCreateBy());
        aiFlowRuntimeMapper.updateById(runtime);
    }

    private AiFlowRuntimeNode buildRuntimeNode(Long runtimeId, String nodeId, String nodeName, String nodeType, int runStatus,
                                               String nodeInput, Map<String, Object> outputs, String errorStack,
                                               Long operatorId, LocalDateTime startTime, LocalDateTime endTime,
                                               Long costMs) {
        AiFlowRuntimeNode node = new AiFlowRuntimeNode();
        node.setRuntimeId(runtimeId);
        node.setNodeId(nodeId);
        node.setNodeName(nodeName);
        node.setNodeType(nodeType);
        node.setRunStatus(runStatus);
        node.setNodeInput(nodeInput);
        node.setNodeOutput(outputs == null ? null : AiFlowCommonUtils.serialize(outputs, objectMapper));
        node.setErrorStack(errorStack);
        node.setStartTime(startTime);
        node.setEndTime(endTime);
        node.setCostMs(costMs);
        node.setCreateBy(operatorId);
        return node;
    }

    private FlowExecuteResult.FlowNodeResult buildNodeResult(String nodeId, String nodeType, int runStatus,
                                           Map<String, Object> outputs, Long costMs, String errorStack) {
        FlowExecuteResult.FlowNodeResult vo = new FlowExecuteResult.FlowNodeResult();
        vo.setNodeId(nodeId);
        vo.setNodeType(nodeType);
        vo.setRunStatus(runStatus);
        vo.setOutput(outputs);
        vo.setCostMs(costMs);
        vo.setErrorStack(errorStack);
        return vo;
    }

    private Map<String, String> buildNodeTypeMap(Workflow workflow) {
        Map<String, String> map = new java.util.HashMap<>();
        if (workflow.getNodes() != null) {
            for (Node node : workflow.getNodes()) {
                map.put(node.getId(), node.getType());
            }
        }
        return map;
    }

    /** 构建 节点ID -> 节点名称 映射，优先取 data.label，其次 node.label。 */
    private Map<String, String> buildNodeNameMap(Workflow workflow) {
        Map<String, String> map = new java.util.HashMap<>();
        if (workflow.getNodes() != null) {
            for (Node node : workflow.getNodes()) {
                String name = node.getData() == null ? null : node.getData().getLabel();
                if (name == null || name.isBlank()) {
                    name = node.getLabel();
                }
                map.put(node.getId(), name);
            }
        }
        return map;
    }

    /** 引擎节点状态 → 落库状态码。 */
    private int mapNodeStatus(NodeExecutionStatus status) {
        if (status == null) {
            return NodeRunStatusEnum.NOT_EXECUTED.getCode();
        }
        return switch (status) {
            case RUNNING -> NodeRunStatusEnum.RUNNING.getCode();
            case SUCCESS -> NodeRunStatusEnum.SUCCESS.getCode();
            case FAILED -> NodeRunStatusEnum.FAILED.getCode();
            case SKIPPED -> NodeRunStatusEnum.SKIPPED.getCode();
            case PENDING -> NodeRunStatusEnum.NOT_EXECUTED.getCode();
        };
    }
}
