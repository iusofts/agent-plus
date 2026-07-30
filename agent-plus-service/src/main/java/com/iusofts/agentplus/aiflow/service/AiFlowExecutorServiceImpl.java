package com.iusofts.agentplus.aiflow.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iusofts.agentplus.aiflow.entity.AiFlow;
import com.iusofts.agentplus.aiflow.entity.AiFlowRuntime;
import com.iusofts.agentplus.aiflow.entity.AiFlowRuntimeNode;
import com.iusofts.agentplus.aiflow.entity.AiFlowVersion;
import com.iusofts.agentplus.aiflow.enums.NodeRunStatusEnum;
import com.iusofts.agentplus.aiflow.enums.PublishingStatusEnum;
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
import com.iusofts.agentplus.aiflow.stream.WorkflowStreamEvent;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.engine.WorkflowEngine;
import com.iusofts.agentplus.engine.WorkflowExecutionResult;
import com.iusofts.agentplus.engine.WorkflowExecuteRequest;
import com.iusofts.agentplus.engine.context.NodeExecutionStatus;
import com.iusofts.agentplus.engine.context.NodeOutput;
import com.iusofts.agentplus.engine.context.NodeTiming;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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

        // 3. 创建运行实例（占位 traceId）
        String placeholderTraceId = AiFlowCommonUtils.newPlaceholderTraceId();
        AiFlowRuntime runtime = newRuntime(version, placeholderTraceId, inputs, operatorId, trialFlag);
        runtime.setFlowName(aiFlow.getName());
        aiFlowRuntimeMapper.insert(runtime);

        LocalDateTime startTime = runtime.getStartTime();
        FlowExecuteResult result = new FlowExecuteResult();
        result.setRuntimeId(runtime.getId());
        result.setFlowId(flowId);

        try {
            // 4. 执行工作流
            WorkflowExecutionResult execResult = workflowEngine.execute(
                    WorkflowExecuteRequest.builder()
                            .workflow(workflow)
                            .config(config)
                            .inputs(inputs)
                            .runId(placeholderTraceId)
                            .flowId(flowId)
                            .operatorId(operatorId)
                            .orgId(orgId)
                            .trialFlag(trialFlag)
                            .flowName(aiFlow.getName())
                            .build()
            );

            // 更新真实的 traceId
            String traceId = execResult.getRunId();
            result.setTraceId(traceId);
            AiFlowRuntime traceUpdate = new AiFlowRuntime();
            traceUpdate.setId(runtime.getId());
            traceUpdate.setTraceId(traceId);
            aiFlowRuntimeMapper.updateById(traceUpdate);
            runtime.setTraceId(traceId);

            // 5. 落库节点
            List<AiFlowRuntimeNode> nodeEntities = new ArrayList<>();
            List<FlowExecuteResult.FlowNodeResult> nodeResults = new ArrayList<>();
            Map<String, String> nodeTypeMap = buildNodeTypeMap(workflow);
            Map<String, String> nodeNameMap = buildNodeNameMap(workflow);
            Map<String, NodeTiming> timings = execResult.getNodeTimings();

            for (Map.Entry<String, NodeExecutionStatus> entry : execResult.getNodeStatus().entrySet()) {
                String nodeId = entry.getKey();
                String nodeType = nodeTypeMap.getOrDefault(nodeId, "");
                String nodeName = nodeNameMap.getOrDefault(nodeId, "");
                NodeOutput out = execResult.getNodeOutputs().get(nodeId);
                Map<String, Object> outputs = out == null ? null : out.getOutputs();
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

            // 6. 完成运行
            long costMs = Duration.between(startTime, LocalDateTime.now()).toMillis();
            finishRuntime(runtime, RunStatusEnum.SUCCESS.getCode(), costMs,
                    AiFlowCommonUtils.serialize(execResult.getOutput(), objectMapper), null);

            result.setRunStatus(RunStatusEnum.SUCCESS.getCode());
            result.setOutput(execResult.getOutput());
            result.setCostMs(costMs);
            result.setNodeResults(nodeResults);
            return result;

        } catch (Exception e) {
            long costMs = Duration.between(startTime, LocalDateTime.now()).toMillis();
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
     * 根据版本 ID 执行流程（试运行）。
     *
     * @param versionId  版本 ID
     * @param flowId     流程 ID
     * @param inputs     输入参数
     * @param operatorId 操作人 ID
     * @param orgId      组织 ID
     * @param trialFlag  试运行标记
     * @return 执行结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FlowExecuteResult executeVersion(Long versionId,
                                            Long flowId,
                                            Map<String, Object> inputs,
                                            Long operatorId,
                                            Integer orgId,
                                            int trialFlag) {
        // 1. 加载版本
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
                throw new SystemBusinessException("流程暂无可运行的版本");
            }
        }

        // 2. 反序列化
        Workflow workflow = AiFlowCommonUtils.deserializeWorkflow(version.getFlowJson(), objectMapper);
        WorkflowConfig config = AiFlowCommonUtils.deserializeConfig(version.getConfigJson(), objectMapper);

        // 3. 创建运行实例（占位 traceId）
        String placeholderTraceId = AiFlowCommonUtils.newPlaceholderTraceId();
        AiFlowRuntime runtime = newRuntime(version, placeholderTraceId, inputs, operatorId, trialFlag);
        AiFlow aiFlow = aiFlowMapper.selectById(version.getFlowId());
        if (aiFlow != null) {
            runtime.setFlowName(aiFlow.getName());
        }
        aiFlowRuntimeMapper.insert(runtime);

        LocalDateTime startTime = runtime.getStartTime();
        FlowExecuteResult result = new FlowExecuteResult();
        result.setRuntimeId(runtime.getId());
        result.setFlowId(version.getFlowId());

        try {
            // 4. 执行工作流
            WorkflowExecutionResult execResult = workflowEngine.execute(
                    WorkflowExecuteRequest.builder()
                            .workflow(workflow)
                            .config(config)
                            .inputs(inputs)
                            .runId(placeholderTraceId)
                            .flowId(version.getFlowId())
                            .operatorId(operatorId)
                            .orgId(orgId)
                            .trialFlag(trialFlag)
                            .flowName(aiFlow != null ? aiFlow.getName() : null)
                            .build()
            );

            // 更新真实的 traceId
            String traceId = execResult.getRunId();
            result.setTraceId(traceId);
            AiFlowRuntime traceUpdate = new AiFlowRuntime();
            traceUpdate.setId(runtime.getId());
            traceUpdate.setTraceId(traceId);
            aiFlowRuntimeMapper.updateById(traceUpdate);
            runtime.setTraceId(traceId);

            // 5. 落库节点
            List<AiFlowRuntimeNode> nodeEntities = new ArrayList<>();
            List<FlowExecuteResult.FlowNodeResult> nodeResults = new ArrayList<>();
            Map<String, String> nodeTypeMap = buildNodeTypeMap(workflow);
            Map<String, String> nodeNameMap = buildNodeNameMap(workflow);
            Map<String, NodeTiming> timings = execResult.getNodeTimings();

            for (Map.Entry<String, NodeExecutionStatus> entry : execResult.getNodeStatus().entrySet()) {
                String nodeId = entry.getKey();
                String nodeType = nodeTypeMap.getOrDefault(nodeId, "");
                String nodeName = nodeNameMap.getOrDefault(nodeId, "");
                NodeOutput out = execResult.getNodeOutputs().get(nodeId);
                Map<String, Object> outputs = out == null ? null : out.getOutputs();
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

            // 6. 完成运行
            long costMs = Duration.between(startTime, LocalDateTime.now()).toMillis();
            finishRuntime(runtime, RunStatusEnum.SUCCESS.getCode(), costMs,
                    AiFlowCommonUtils.serialize(execResult.getOutput(), objectMapper), null);

            result.setRunStatus(RunStatusEnum.SUCCESS.getCode());
            result.setOutput(execResult.getOutput());
            result.setCostMs(costMs);
            result.setNodeResults(nodeResults);
            return result;

        } catch (Exception e) {
            long costMs = Duration.between(startTime, LocalDateTime.now()).toMillis();
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
     * 流式执行流程（使用最新已发布版本）。
     *
     * @param flowId     流程 ID
     * @param inputs     输入参数
     * @param operatorId 操作人 ID
     * @param orgId      组织 ID
     * @param trialFlag  试运行标记 0:正式 1:流程试运行 2:节点试运行
     * @return 事件流
     */
    @Override
    public Flux<WorkflowStreamEvent> streamExecuteFlow(Long flowId,
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
            throw new SystemBusinessException("流程无已发布版本，请先发布流程后重试");
        }

        var versionWrapper = Wrappers.<AiFlowVersion>lambdaQuery();
        versionWrapper.eq(AiFlowVersion::getFlowId, flowId)
                .eq(AiFlowVersion::getVersionNo, onlineVersion);
        AiFlowVersion version = aiFlowVersionMapper.selectOne(versionWrapper);
        if (version == null) {
            throw new SystemBusinessException("流程发布版本不存在");
        }

        return streamExecuteVersionInternal(version, aiFlow.getName(), inputs, operatorId, orgId, trialFlag);
    }

    /**
     * 流式执行指定版本（供试运行使用）。
     *
     * @param versionId  版本 ID
     * @param flowId     流程 ID
     * @param inputs     输入参数
     * @param operatorId 操作人 ID
     * @param orgId      组织 ID
     * @param trialFlag  试运行标记
     * @return 事件流
     */
    @Override
    public Flux<WorkflowStreamEvent> streamExecuteVersion(Long versionId,
                                                          Long flowId,
                                                          Map<String, Object> inputs,
                                                          Long operatorId,
                                                          Integer orgId,
                                                          int trialFlag) {
        // 1. 加载版本
        AiFlowVersion version;
        String flowName = null;
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
                throw new SystemBusinessException("流程暂无可运行的版本");
            }
        }

        // 获取流程名称
        if (version.getFlowId() != null) {
            AiFlow aiFlow = aiFlowMapper.selectById(version.getFlowId());
            flowName = aiFlow != null ? aiFlow.getName() : null;
        }

        return streamExecuteVersionInternal(version, flowName, inputs, operatorId, orgId, trialFlag);
    }

    private Flux<WorkflowStreamEvent> streamExecuteVersionInternal(AiFlowVersion version,
                                                                   String flowName,
                                                                   Map<String, Object> inputs,
                                                                   Long operatorId,
                                                                   Integer orgId,
                                                                   int trialFlag) {
        // 1. 反序列化
        Workflow workflow = AiFlowCommonUtils.deserializeWorkflow(version.getFlowJson(), objectMapper);
        WorkflowConfig config = AiFlowCommonUtils.deserializeConfig(version.getConfigJson(), objectMapper);

        // 2. 创建运行实例（占位 traceId）
        String placeholderTraceId = AiFlowCommonUtils.newPlaceholderTraceId();
        AiFlowRuntime runtime = newRuntime(version, placeholderTraceId, inputs, operatorId, trialFlag);
        if (flowName != null) {
            runtime.setFlowName(flowName);
        }
        aiFlowRuntimeMapper.insert(runtime);

        LocalDateTime startTime = runtime.getStartTime();

        // 3. 创建 Sink 用于返回事件流
        Sinks.Many<WorkflowStreamEvent> sink = Sinks.many().multicast().onBackpressureBuffer();

        // 4. 在异步线程中执行工作流
        CompletableFuture.runAsync(() -> {
            try {
                // 构建执行请求
                WorkflowExecuteRequest executeRequest = WorkflowExecuteRequest.builder()
                        .workflow(workflow)
                        .config(config)
                        .inputs(inputs)
                        .runId(placeholderTraceId)
                        .flowId(version.getFlowId())
                        .operatorId(operatorId)
                        .orgId(orgId)
                        .trialFlag(trialFlag)
                        .flowName(flowName)
                        .build();

                // 执行工作流并转发事件，完成后更新运行状态
                workflowEngine.streamExecute(executeRequest)
                        .doOnNext(sink::tryEmitNext)
                        .doOnError(error -> {
                            handleStreamError(runtime, startTime, error);
                            sink.tryEmitError(error);
                        })
                        .doOnComplete(() -> {
                            handleStreamComplete(runtime, startTime);
                            sink.tryEmitComplete();
                        })
                        .subscribe();
            } catch (Exception e) {
                handleStreamError(runtime, startTime, e);
                sink.tryEmitError(e);
            }
        });

        return sink.asFlux();
    }

    private void handleStreamError(AiFlowRuntime runtime, LocalDateTime startTime, Throwable error) {
        long costMs = Duration.between(startTime, LocalDateTime.now()).toMillis();
        finishRuntime(runtime, RunStatusEnum.FAILED.getCode(), costMs,
                null, AiFlowCommonUtils.truncate(error.getMessage()));
    }

    private void handleStreamComplete(AiFlowRuntime runtime, LocalDateTime startTime) {
        long costMs = Duration.between(startTime, LocalDateTime.now()).toMillis();
        finishRuntime(runtime, RunStatusEnum.SUCCESS.getCode(), costMs,
                null, null);
    }

    // ========== 内部辅助方法 ==========

    private AiFlowRuntime newRuntime(AiFlowVersion version, String traceId,
                                     Map<String, Object> inputs, Long operatorId, int trialFlag) {
        AiFlowRuntime runtime = new AiFlowRuntime();
        runtime.setFlowId(version.getFlowId());
        runtime.setVersionNo(version.getVersionNo());
        runtime.setTraceId(traceId);
        runtime.setRunStatus(RunStatusEnum.RUNNING.getCode());
        runtime.setTrialFlag(trialFlag);
        runtime.setStartTime(LocalDateTime.now());
        runtime.setCreateBy(operatorId);
        return runtime;
    }

    private void finishRuntime(AiFlowRuntime runtime, Integer runStatus, long costMs,
                               String outputResult, String errorMsg) {
        AiFlowRuntime update = new AiFlowRuntime();
        update.setId(runtime.getId());
        update.setRunStatus(runStatus);
        update.setEndTime(LocalDateTime.now());
        update.setCostMs(costMs);
        update.setErrorMsg(errorMsg);
        update.setUpdateBy(runtime.getCreateBy());
        aiFlowRuntimeMapper.updateById(update);
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

    /** 构建节点 ID → 节点名称映射，优先取 data.label，其次 node.label。 */
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
