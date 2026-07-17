package com.iusofts.agentplus.aiflow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iusofts.agentplus.aiflow.entity.AiFlow;
import com.iusofts.agentplus.aiflow.entity.AiFlowRuntime;
import com.iusofts.agentplus.aiflow.entity.AiFlowRuntimeNode;
import com.iusofts.agentplus.aiflow.entity.AiFlowVersion;
import com.iusofts.agentplus.aiflow.enums.FlowNodeType;
import com.iusofts.agentplus.aiflow.enums.NodeRunStatusEnum;
import com.iusofts.agentplus.aiflow.enums.RunStatusEnum;
import com.iusofts.agentplus.aiflow.interfaces.IAiFlowTrialService;
import com.iusofts.agentplus.aiflow.mapper.AiFlowMapper;
import com.iusofts.agentplus.aiflow.mapper.AiFlowRuntimeMapper;
import com.iusofts.agentplus.aiflow.mapper.AiFlowRuntimeNodeMapper;
import com.iusofts.agentplus.aiflow.mapper.AiFlowVersionMapper;
import com.iusofts.agentplus.aiflow.vo.AiFlowTrialNodeResultVo;
import com.iusofts.agentplus.aiflow.vo.AiFlowTrialRunFlowReqVo;
import com.iusofts.agentplus.aiflow.vo.AiFlowTrialRunNodeReqVo;
import com.iusofts.agentplus.aiflow.vo.AiFlowTrialRunResultVo;
import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.Workflow;
import com.iusofts.agentplus.aiflow.vo.workflow.config.WorkflowConfig;
import com.iusofts.agentplus.aiflow.vo.workflow.data.InputParamNodeData;
import com.iusofts.agentplus.aiflow.vo.workflow.data.NodeData;
import com.iusofts.agentplus.aiflow.vo.workflow.data.common.InputParam;
import com.iusofts.agentplus.aiflow.vo.workflow.data.common.ParamMapKey;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.engine.WorkflowEngine;
import com.iusofts.agentplus.engine.WorkflowExecutionResult;
import com.iusofts.agentplus.engine.context.ExecutionContext;
import com.iusofts.agentplus.engine.context.NodeExecutionStatus;
import com.iusofts.agentplus.engine.context.NodeOutput;
import com.iusofts.agentplus.engine.context.NodeTiming;
import com.iusofts.agentplus.engine.executor.NodeExecutor;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * <p>
 * 流程试运行 服务实现类
 * </p>
 *
 * <p>试运行走引擎的同步执行,结果落 {@code ai_flow_runtime}(trialFlag=1)及
 * {@code ai_flow_runtime_node},与正式运行记录以 {@code trial_flag} 区分。</p>
 *
 * @author Ivan
 * @since 2026-07-16
 */
@Service
public class AiFlowTrialServiceImpl implements IAiFlowTrialService {

    /** 试运行标记。 */
    private static final int TRIAL_FLAG = 1;

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

    @Override
    public AiFlowTrialRunResultVo runFlow(AiFlowTrialRunFlowReqVo reqVo) {
        AiFlowVersion version = loadVersion(reqVo.getVersionId(), reqVo.getFlowId());
        Workflow workflow = deserializeWorkflow(version.getFlowJson());
        WorkflowConfig config = deserializeConfig(version.getConfigJson());

        Map<String, Object> inputs = reqVo.getInputs() == null ? new LinkedHashMap<>() : reqVo.getInputs();
        String traceId = newTraceId();

        AiFlowRuntime runtime = newRuntime(version, traceId, inputs, reqVo.getOperatorId());
        aiFlowRuntimeMapper.insert(runtime);

        LocalDateTime start = runtime.getStartTime();
        AiFlowTrialRunResultVo result = new AiFlowTrialRunResultVo();
        result.setRuntimeId(runtime.getId());
        result.setTraceId(traceId);

        try {
            WorkflowExecutionResult execResult = workflowEngine.execute(workflow, config, inputs, traceId,
                    version.getFlowId(), reqVo.getOperatorId(), reqVo.getOrgId());

            List<AiFlowRuntimeNode> nodeEntities = new ArrayList<>();
            List<AiFlowTrialNodeResultVo> nodeResults = new ArrayList<>();
            Map<String, String> nodeTypeMap = buildNodeTypeMap(workflow);
            Map<String, NodeTiming> timings = execResult.getNodeTimings();
            for (Map.Entry<String, NodeExecutionStatus> entry : execResult.getNodeStatus().entrySet()) {
                String nodeId = entry.getKey();
                String nodeType = nodeTypeMap.getOrDefault(nodeId, "");
                NodeOutput output = execResult.getNodeOutputs().get(nodeId);
                Map<String, Object> outputs = output == null ? null : output.getOutputs();
                int nodeStatus = mapNodeStatus(entry.getValue());
                NodeTiming timing = timings == null ? null : timings.get(nodeId);
                LocalDateTime nodeStart = timing == null ? null : timing.getStartTime();
                LocalDateTime nodeEnd = timing == null ? null : timing.getEndTime();
                Long nodeCost = timing == null ? null : timing.getCostMs();

                nodeEntities.add(buildRuntimeNode(runtime.getId(), nodeId, nodeType, nodeStatus,
                        null, outputs, null, reqVo.getOperatorId(), nodeStart, nodeEnd, nodeCost));
                nodeResults.add(buildNodeResultVo(nodeId, nodeType, nodeStatus, outputs, nodeCost, null));
            }
            for (AiFlowRuntimeNode nodeEntity : nodeEntities) {
                aiFlowRuntimeNodeMapper.insert(nodeEntity);
            }

            long costMs = Duration.between(start, LocalDateTime.now()).toMillis();
            finishRuntime(runtime, RunStatusEnum.SUCCESS.getCode(), costMs,
                    serialize(execResult.getOutput()), null);

            result.setRunStatus(RunStatusEnum.SUCCESS.getCode());
            result.setOutput(execResult.getOutput());
            result.setCostMs(costMs);
            result.setNodeResults(nodeResults);
            return result;
        } catch (Exception e) {
            long costMs = Duration.between(start, LocalDateTime.now()).toMillis();
            finishRuntime(runtime, RunStatusEnum.FAILED.getCode(), costMs, null, truncate(e.getMessage()));

            result.setRunStatus(RunStatusEnum.FAILED.getCode());
            result.setCostMs(costMs);
            result.setErrorMsg(truncate(e.getMessage()));
            result.setNodeResults(new ArrayList<>());
            return result;
        }
    }

    @Override
    public AiFlowTrialRunResultVo runNode(AiFlowTrialRunNodeReqVo reqVo) {
        AiFlowVersion version = loadVersion(reqVo.getVersionId(), reqVo.getFlowId());
        Workflow workflow = deserializeWorkflow(version.getFlowJson());
        WorkflowConfig config = deserializeConfig(version.getConfigJson());

        Node target = findNode(workflow, reqVo.getNodeId());
        // Start 节点不支持单节点试运行
        if (FlowNodeType.START.getCode().equals(target.getType())) {
            throw new SystemBusinessException("开始节点不支持单节点试运行");
        }
        // 仅支持含 InputParam 类型输入参数的节点
        List<InputParam> inputParams = extractInputParams(target);

        Map<String, Object> inputs = reqVo.getInputs() == null ? new LinkedHashMap<>() : reqVo.getInputs();
        String traceId = newTraceId();

        AiFlowRuntime runtime = newRuntime(version, traceId, inputs, reqVo.getOperatorId());
        aiFlowRuntimeMapper.insert(runtime);

        LocalDateTime start = runtime.getStartTime();
        AiFlowTrialRunResultVo result = new AiFlowTrialRunResultVo();
        result.setRuntimeId(runtime.getId());
        result.setTraceId(traceId);

        try {
            ExecutionContext ctx = new ExecutionContext(traceId, config, new LinkedHashMap<>(),
                    version.getFlowId(), reqVo.getOperatorId(), reqVo.getOrgId());
            // 按参数名直接赋值:把用户给的值回填到各输入参数 paramMapKey 指向的位置,不走真实上游
            applyDirectInputs(inputParams, inputs, ctx);

            NodeExecutor executor = workflowEngine.registry().get(target.getType());
            LocalDateTime nodeStart = LocalDateTime.now();
            NodeOutput output = executor.execute(target, ctx);
            LocalDateTime nodeEnd = LocalDateTime.now();
            Map<String, Object> outputs = output == null ? null : output.getOutputs();

            long costMs = Duration.between(nodeStart, nodeEnd).toMillis();
            int nodeStatus = NodeRunStatusEnum.SUCCESS.getCode();

            AiFlowRuntimeNode nodeEntity = buildRuntimeNode(runtime.getId(), target.getId(), target.getType(),
                    nodeStatus, serialize(inputs), outputs, null, reqVo.getOperatorId(),
                    nodeStart, nodeEnd, costMs);
            aiFlowRuntimeNodeMapper.insert(nodeEntity);

            finishRuntime(runtime, RunStatusEnum.SUCCESS.getCode(), costMs, serialize(outputs), null);

            result.setRunStatus(RunStatusEnum.SUCCESS.getCode());
            result.setOutput(outputs);
            result.setCostMs(costMs);
            List<AiFlowTrialNodeResultVo> nodeResults = new ArrayList<>();
            nodeResults.add(buildNodeResultVo(target.getId(), target.getType(), nodeStatus, outputs, costMs, null));
            result.setNodeResults(nodeResults);
            return result;
        } catch (Exception e) {
            LocalDateTime nodeEnd = LocalDateTime.now();
            long costMs = Duration.between(start, nodeEnd).toMillis();
            String errorStack = truncate(e.toString());

            AiFlowRuntimeNode nodeEntity = buildRuntimeNode(runtime.getId(), target.getId(), target.getType(),
                    NodeRunStatusEnum.FAILED.getCode(), serialize(inputs), null, errorStack, reqVo.getOperatorId(),
                    start, nodeEnd, costMs);
            aiFlowRuntimeNodeMapper.insert(nodeEntity);

            finishRuntime(runtime, RunStatusEnum.FAILED.getCode(), costMs, null, truncate(e.getMessage()));

            result.setRunStatus(RunStatusEnum.FAILED.getCode());
            result.setCostMs(costMs);
            result.setErrorMsg(truncate(e.getMessage()));
            List<AiFlowTrialNodeResultVo> nodeResults = new ArrayList<>();
            nodeResults.add(buildNodeResultVo(target.getId(), target.getType(),
                    NodeRunStatusEnum.FAILED.getCode(), null, costMs, errorStack));
            result.setNodeResults(nodeResults);
            return result;
        }
    }

    // ------------------------------------------------------------------
    // 内部辅助
    // ------------------------------------------------------------------

    /** versionId 优先,否则按 flowId 取最新版本。 */
    private AiFlowVersion loadVersion(Long versionId, Long flowId) {
        if (versionId != null) {
            AiFlowVersion version = aiFlowVersionMapper.selectById(versionId);
            if (version == null) {
                throw new SystemBusinessException("流程版本不存在");
            }
            return version;
        }
        if (flowId == null) {
            throw new SystemBusinessException("versionId 与 flowId 不能同时为空");
        }
        AiFlow aiFlow = aiFlowMapper.selectById(flowId);
        if (aiFlow == null) {
            throw new SystemBusinessException("流程不存在");
        }
        LambdaQueryWrapper<AiFlowVersion> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AiFlowVersion::getFlowId, flowId);
        wrapper.orderByDesc(AiFlowVersion::getId);
        wrapper.last("LIMIT 1");
        AiFlowVersion version = aiFlowVersionMapper.selectOne(wrapper);
        if (version == null) {
            throw new SystemBusinessException("流程尚无可运行的版本");
        }
        return version;
    }

    private AiFlowRuntime newRuntime(AiFlowVersion version, String traceId,
                                     Map<String, Object> inputs, Long operatorId) {
        AiFlowRuntime runtime = new AiFlowRuntime();
        runtime.setFlowId(version.getFlowId());
        runtime.setVersionNo(version.getVersionNo());
        runtime.setTraceId(traceId);
        runtime.setRunStatus(RunStatusEnum.RUNNING.getCode());
        runtime.setTrialFlag(TRIAL_FLAG);
        runtime.setStartTime(LocalDateTime.now());
        runtime.setInputParams(serialize(inputs));
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

    private AiFlowRuntimeNode buildRuntimeNode(Long runtimeId, String nodeId, String nodeType, int runStatus,
                                               String nodeInput, Map<String, Object> outputs, String errorStack,
                                               Long operatorId, LocalDateTime startTime, LocalDateTime endTime,
                                               Long costMs) {
        AiFlowRuntimeNode node = new AiFlowRuntimeNode();
        node.setRuntimeId(runtimeId);
        node.setNodeId(nodeId);
        node.setNodeType(nodeType);
        node.setRunStatus(runStatus);
        node.setNodeInput(nodeInput);
        node.setNodeOutput(outputs == null ? null : serialize(outputs));
        node.setErrorStack(errorStack);
        node.setStartTime(startTime);
        node.setEndTime(endTime);
        node.setCostMs(costMs);
        node.setCreateBy(operatorId);
        return node;
    }

    private AiFlowTrialNodeResultVo buildNodeResultVo(String nodeId, String nodeType, int runStatus,
                                                      Map<String, Object> outputs, Long costMs, String errorStack) {
        AiFlowTrialNodeResultVo vo = new AiFlowTrialNodeResultVo();
        vo.setNodeId(nodeId);
        vo.setNodeType(nodeType);
        vo.setRunStatus(runStatus);
        vo.setOutput(outputs);
        vo.setCostMs(costMs);
        vo.setErrorStack(errorStack);
        return vo;
    }

    private Map<String, String> buildNodeTypeMap(Workflow workflow) {
        Map<String, String> map = new LinkedHashMap<>();
        if (workflow.getNodes() != null) {
            for (Node node : workflow.getNodes()) {
                map.put(node.getId(), node.getType());
            }
        }
        return map;
    }

    private Node findNode(Workflow workflow, String nodeId) {
        if (nodeId == null) {
            throw new SystemBusinessException("节点ID不能为空");
        }
        if (workflow.getNodes() != null) {
            for (Node node : workflow.getNodes()) {
                if (nodeId.equals(node.getId())) {
                    return node;
                }
            }
        }
        throw new SystemBusinessException("节点不存在: " + nodeId);
    }

    /**
     * 提取目标节点的输入参数定义。仅 {@link InputParamNodeData} 子类(LLM/知识库/工具/批处理)
     * 支持单节点试运行,其余节点抛异常。
     */
    private List<InputParam> extractInputParams(Node target) {
        NodeData data = target.getData();
        if (!(data instanceof InputParamNodeData inputParamData)) {
            throw new SystemBusinessException("该节点类型不支持单节点试运行");
        }
        List<InputParam> inputParams = inputParamData.getInputParams();
        if (inputParams == null || inputParams.isEmpty()) {
            throw new SystemBusinessException("该节点没有可赋值的输入参数");
        }
        return inputParams;
    }

    /**
     * 按参数名直接赋值:用户传入的 {@code 参数名 → 值},按节点各输入参数的 {@code paramMapKey}
     * 回填到上下文对应位置,使执行器解析时直接拿到用户给的值,不触碰真实上游节点。
     */
    private void applyDirectInputs(List<InputParam> inputParams, Map<String, Object> inputs,
                                   ExecutionContext ctx) {
        // 按来源节点归组,组装为该节点的模拟输出写入上下文
        Map<String, Map<String, Object>> grouped = new LinkedHashMap<>();
        for (InputParam param : inputParams) {
            if (param.getName() == null) {
                continue;
            }
            Object value = inputs.get(param.getName());
            ParamMapKey key = param.getParamMapKey();
            if (key == null || key.getNodeId() == null || key.getName() == null) {
                continue;
            }
            grouped.computeIfAbsent(key.getNodeId(), k -> new LinkedHashMap<>())
                    .put(key.getName(), value);
        }
        for (Map.Entry<String, Map<String, Object>> entry : grouped.entrySet()) {
            String sourceNodeId = entry.getKey();
            Map<String, Object> values = entry.getValue();
            // 全局输入/环境变量走对应容器,其余作为上游节点输出注入
            if ("inputs".equalsIgnoreCase(sourceNodeId) || "start".equalsIgnoreCase(sourceNodeId)) {
                ctx.getGlobalInputs().putAll(values);
            } else if ("env".equalsIgnoreCase(sourceNodeId)) {
                ctx.getEnvVars().putAll(values);
            } else {
                ctx.putOutput(new NodeOutput(sourceNodeId, values));
            }
        }
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

    private String newTraceId() {
        return "trial-" + UUID.randomUUID().toString().replace("-", "");
    }

    private Workflow deserializeWorkflow(String flowJson) {
        if (flowJson == null || flowJson.isBlank()) {
            throw new SystemBusinessException("流程定义为空,无法运行");
        }
        try {
            return objectMapper.readValue(flowJson, Workflow.class);
        } catch (JsonProcessingException e) {
            throw new SystemBusinessException("流程数据解析失败");
        }
    }

    private WorkflowConfig deserializeConfig(String configJson) {
        if (configJson == null || configJson.isBlank()) {
            return new WorkflowConfig();
        }
        try {
            return objectMapper.readValue(configJson, WorkflowConfig.class);
        } catch (JsonProcessingException e) {
            throw new SystemBusinessException("流程配置数据解析失败");
        }
    }

    private String serialize(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /** 错误信息落库前做长度保护。 */
    private String truncate(String text) {
        if (text == null) {
            return null;
        }
        int max = 2000;
        return text.length() > max ? text.substring(0, max) : text;
    }

}
