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
import com.iusofts.agentplus.aiflow.enums.FlowTypeEnum;
import com.iusofts.agentplus.aiflow.enums.NodeRunStatusEnum;
import com.iusofts.agentplus.aiflow.enums.RunStatusEnum;
import com.iusofts.agentplus.aiflow.interfaces.IAiFlowExecutorService;
import com.iusofts.agentplus.aiflow.interfaces.IAiFlowTrialService;
import com.iusofts.agentplus.aiflow.mapper.AiFlowMapper;
import com.iusofts.agentplus.aiflow.mapper.AiFlowRuntimeMapper;
import com.iusofts.agentplus.aiflow.mapper.AiFlowRuntimeNodeMapper;
import com.iusofts.agentplus.aiflow.mapper.AiFlowVersionMapper;
import com.iusofts.agentplus.aiflow.vo.*;
import com.iusofts.agentplus.aiflow.vo.FlowExecuteResult.FlowNodeResult;
import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.Workflow;
import com.iusofts.agentplus.aiflow.vo.workflow.config.WorkflowConfig;
import com.iusofts.agentplus.aiflow.vo.workflow.data.InputParamNodeData;
import com.iusofts.agentplus.aiflow.vo.workflow.data.NodeData;
import com.iusofts.agentplus.aiflow.vo.workflow.data.common.InputParam;
import com.iusofts.agentplus.aiflow.utils.AiFlowCommonUtils;
import com.iusofts.agentplus.aiflow.vo.workflow.data.common.ParamMapKey;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.engine.WorkflowEngine;
import com.iusofts.agentplus.engine.WorkflowExecutionResult;
import com.iusofts.agentplus.engine.context.ExecutionContext;
import com.iusofts.agentplus.engine.context.NodeExecutionStatus;
import com.iusofts.agentplus.engine.context.NodeOutput;
import com.iusofts.agentplus.engine.context.NodeTiming;
import com.iusofts.agentplus.engine.executor.NodeExecutor;
import com.iusofts.agentplus.engine.util.ParamResolver;
import com.iusofts.agentplus.trace.TraceUtil;
import com.alibaba.fastjson2.JSON;
import io.opentelemetry.api.trace.SpanKind;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 流程试运行 服务实现类
 * </p>
 *
 * <p>试运行走引擎的同步执行,结果落 {@code ai_flow_runtime}及
 * {@code ai_flow_runtime_node},与正式运行记录以 {@code trial_flag} 区分：
 * 1 = 流程试运行, 2 = 节点试运行。</p>
 *
 * @author Ivan
 * @since 2026-07-16
 */
@Service
public class AiFlowTrialServiceImpl implements IAiFlowTrialService {

    /** 流程试运行标记。 */
    private static final int TRIAL_FLAG_FLOW = 1;
    /** 节点试运行标记。 */
    private static final int TRIAL_FLAG_NODE = 2;

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
    @Resource
    private IAiFlowExecutorService aiFlowExecutorService;

    @Override
    public AiFlowTrialRunResultVo runFlow(AiFlowTrialRunFlowReqVo reqVo) {
        // 调用公共执行服务
        FlowExecuteResult result = aiFlowExecutorService.executeVersion(
                reqVo.getVersionId(),
                reqVo.getFlowId(),
                reqVo.getInputs() == null ? new LinkedHashMap<>() : reqVo.getInputs(),
                reqVo.getOperatorId(),
                reqVo.getOrgId(),
                TRIAL_FLAG_FLOW
        );

        // 转换为试运行返回格式
        AiFlowTrialRunResultVo vo = new AiFlowTrialRunResultVo();
        vo.setRuntimeId(result.getRuntimeId());
        vo.setTraceId(result.getTraceId());
        vo.setRunStatus(result.getRunStatus());
        vo.setOutput(result.getOutput());
        vo.setCostMs(result.getCostMs());
        vo.setErrorMsg(result.getErrorMsg());

        // 转换节点结果
        List<AiFlowTrialNodeResultVo> nodeResults = new ArrayList<>();
        if (result.getNodeResults() != null) {
            for (FlowNodeResult nodeResult : result.getNodeResults()) {
                AiFlowTrialNodeResultVo nodeVo = new AiFlowTrialNodeResultVo();
                nodeVo.setNodeId(nodeResult.getNodeId());
                nodeVo.setNodeType(nodeResult.getNodeType());
                nodeVo.setRunStatus(nodeResult.getRunStatus());
                nodeVo.setOutput(nodeResult.getOutput());
                nodeVo.setCostMs(nodeResult.getCostMs());
                nodeVo.setErrorStack(nodeResult.getErrorStack());
                nodeResults.add(nodeVo);
            }
        }
        vo.setNodeResults(nodeResults);
        return vo;
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
        String placeholderTraceId = AiFlowCommonUtils.newPlaceholderTraceId();

        AiFlowRuntime runtime = newRuntime(version, placeholderTraceId, inputs, reqVo.getOperatorId(), TRIAL_FLAG_NODE);
        aiFlowRuntimeMapper.insert(runtime);

        LocalDateTime start = runtime.getStartTime();
        AiFlowTrialRunResultVo result = new AiFlowTrialRunResultVo();
        result.setRuntimeId(runtime.getId());

        try {
            // 使用 root() 作为父 Context，确保每次都是新的 trace，不继承上一次请求的残留 Context
            return TraceUtil.span("flowTrial.runNode", SpanKind.INTERNAL, io.opentelemetry.context.Context.root(), span -> {
                // 以 OTel traceId 作为本次单节点试运行的 traceId
                String traceId = span.getSpanContext().isValid()
                        ? span.getSpanContext().getTraceId()
                        : placeholderTraceId;
                span.setAttribute("nodeId", target.getId());
                span.setAttribute("nodeType", target.getType());
                span.setAttribute("label", resolveNodeName(target));
                span.setAttribute("trialFlag", true);
                if (reqVo.getOrgId() != null) {
                    span.setAttribute("orgId", reqVo.getOrgId().longValue());
                }
                result.setTraceId(traceId);
                // 先单独更新 traceId 字段，避免后续 updateById 时唯一键冲突
                AiFlowRuntime traceUpdate = new AiFlowRuntime();
                traceUpdate.setId(runtime.getId());
                traceUpdate.setTraceId(traceId);
                aiFlowRuntimeMapper.updateById(traceUpdate);
                // 更新本地对象的 traceId（但 finishRuntime 时要注意不要再修改它）
                runtime.setTraceId(traceId);

                ExecutionContext ctx = new ExecutionContext(traceId, config, new LinkedHashMap<>(),
                        version.getFlowId(), reqVo.getOperatorId(), reqVo.getOrgId(),
                        resolveFlowType(version.getFlowId()));
                // 按参数名直接赋值:把用户给的值回填到各输入参数 paramMapKey 指向的位置,不走真实上游
                applyDirectInputs(inputParams, inputs, ctx);

                // 入参载荷:与 WorkflowGraphCompiler 一致,记录已解析的实际入参值,供 Trace 落库
                Map<String, Object> resolvedInputs = ParamResolver.resolveInputs(inputParams, ctx);
                if (resolvedInputs != null && !resolvedInputs.isEmpty()) {
                    span.setAttribute("ap.payload.input", JSON.toJSONString(resolvedInputs));
                }

                NodeExecutor executor = workflowEngine.registry().get(target.getType());
                LocalDateTime nodeStart = LocalDateTime.now();
                NodeOutput output = executor.execute(target, ctx);
                LocalDateTime nodeEnd = LocalDateTime.now();
                Map<String, Object> outputs = output == null ? null : output.getOutputs();

                // 出参载荷:节点输出结果,供 Trace 落库
                if (outputs != null && !outputs.isEmpty()) {
                    span.setAttribute("ap.payload.output", JSON.toJSONString(outputs));
                }
                span.setAttribute("nodeStatus", "SUCCESS");

                long costMs = Duration.between(nodeStart, nodeEnd).toMillis();
                int nodeStatus = NodeRunStatusEnum.SUCCESS.getCode();

                AiFlowRuntimeNode nodeEntity = buildRuntimeNode(runtime.getId(), target.getId(), resolveNodeName(target), target.getType(),
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
            });
        } catch (Exception e) {
            LocalDateTime nodeEnd = LocalDateTime.now();
            long costMs = Duration.between(start, nodeEnd).toMillis();
            String errorStack = truncate(e.toString());

            AiFlowRuntimeNode nodeEntity = buildRuntimeNode(runtime.getId(), target.getId(), resolveNodeName(target), target.getType(),
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
                                     Map<String, Object> inputs, Long operatorId, int trialFlag) {
        AiFlowRuntime runtime = new AiFlowRuntime();
        runtime.setFlowId(version.getFlowId());
        AiFlow aiFlow = aiFlowMapper.selectById(version.getFlowId());
        if (aiFlow != null) {
            runtime.setFlowName(aiFlow.getName());
        }
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
        // 只更新需要的字段，不更新 traceId（已在前面单独更新过），避免唯一键冲突
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

    /** 取节点名称:优先 data.label,其次 node.label。 */
    private String resolveNodeName(Node node) {
        String name = node.getData() == null ? null : node.getData().getLabel();
        if (name == null || name.isBlank()) {
            name = node.getLabel();
        }
        return name;
    }

    /** 根据 flowId 查 AiFlow.type 并解析为 FlowTypeEnum,查不到时返回 null。 */
    private FlowTypeEnum resolveFlowType(Long flowId) {
        if (flowId == null) {
            return null;
        }
        AiFlow aiFlow = aiFlowMapper.selectById(flowId);
        return aiFlow == null ? null : FlowTypeEnum.getByCode(aiFlow.getType());
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

    private Workflow deserializeWorkflow(String flowJson) {
        return AiFlowCommonUtils.deserializeWorkflow(flowJson, objectMapper);
    }

    private WorkflowConfig deserializeConfig(String configJson) {
        return AiFlowCommonUtils.deserializeConfig(configJson, objectMapper);
    }

    private String serialize(Object value) {
        return AiFlowCommonUtils.serialize(value, objectMapper);
    }

    /** 错误信息落库前做长度保护。 */
    private String truncate(String text) {
        return AiFlowCommonUtils.truncate(text);
    }

}
