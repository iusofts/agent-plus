package com.iusofts.agentplus.engine;

import com.alibaba.fastjson2.JSON;
import com.iusofts.agentplus.aiflow.enums.FlowNodeType;
import com.iusofts.agentplus.aiflow.stream.WorkflowCompleteEvent;
import com.iusofts.agentplus.aiflow.stream.WorkflowStreamEvent;
import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.Workflow;
import com.iusofts.agentplus.aiflow.vo.workflow.config.WorkflowConfig;
import com.iusofts.agentplus.engine.context.ExecutionContext;
import com.iusofts.agentplus.engine.context.NodeExecutionStatus;
import com.iusofts.agentplus.engine.context.NodeOutput;
import com.iusofts.agentplus.engine.exception.WorkflowExecutionException;
import com.iusofts.agentplus.engine.executor.NodeExecutor;
import com.iusofts.agentplus.engine.executor.NodeExecutorRegistry;
import com.iusofts.agentplus.engine.executor.impl.*;
import com.iusofts.agentplus.engine.graph.ExecutionContextTracker;
import com.iusofts.agentplus.engine.graph.WorkflowGraphCompiler;
import com.iusofts.agentplus.engine.graph.WorkflowState;
import com.iusofts.agentplus.engine.history.HistoryMessageProvider;
import com.iusofts.agentplus.engine.knowledge.KnowledgeRetriever;
import com.iusofts.agentplus.engine.knowledge.NoopKnowledgeRetriever;
import com.iusofts.agentplus.engine.llm.ChatModelProvider;
import com.iusofts.agentplus.engine.stream.WorkflowStreamEventCallback;
import com.iusofts.agentplus.engine.tool.ToolRegistry;
import com.iusofts.agentplus.tool.ToolQueryProvider;
import com.iusofts.agentplus.trace.TraceUtil;
import com.iusofts.agentplus.trace.constants.TraceConstant;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import org.bsc.langgraph4j.CompiledGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 工作流执行引擎入口。
 *
 * <p>底层由 <a href="https://github.com/bsorrentino/langgraph4j">langgraph4j</a>
 * 的 {@code StateGraph} 编排。执行时序:</p>
 * <ol>
 *   <li>{@link WorkflowGraphCompiler} 把 {@link Workflow} 编译为主图 + 各批处理节点的子图。</li>
 *   <li>创建共享的 {@link ExecutionContext},注册子图供 {@code BatchNodeExecutor} 复用。</li>
 *   <li>{@code CompiledGraph#invoke} 驱动主图,节点动作内部委托给对应 {@link NodeExecutor}。</li>
 *   <li>汇总所有 End 节点的输出为最终结果,未被访问的节点填 {@code SKIPPED} 状态。</li>
 * </ol>
 *
 * @author Ivan
 */
public class WorkflowEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowEngine.class);

    private final NodeExecutorRegistry registry;
    private final WorkflowGraphCompiler compiler;

    private WorkflowEngine(NodeExecutorRegistry registry) {
        this.registry = registry;
        this.compiler = new WorkflowGraphCompiler(registry);
    }

    public static Builder builder() {
        return new Builder();
    }


    /**
     * 使用请求对象执行工作流。
     *
     * <p>在最外层开启 OTel root span,并以 span 的 traceId 作为本次执行的 runId
     * (即业务 traceId),经 {@link WorkflowExecutionResult#getRunId()} 回传给调用方落库。
     * 若 OTel SDK 未初始化(如单元测试),span context 无效时回退使用传入的 {@code runId}。
     */
    public WorkflowExecutionResult execute(WorkflowExecuteRequest request) {
        // 不传父 Context，使用当前 Context 自动串接（来自自主规划或外部调用）
        return TraceUtil.span(TraceConstant.SPAN_WORKFLOW_EXECUTE, SpanKind.INTERNAL, null, span -> {

            String effectiveRunId = initTraceSpan(request, span);

            // 保存当前 context 作为 root context，供后续节点 span 使用
            io.opentelemetry.context.Context rootContext = io.opentelemetry.context.Context.current();

            WorkflowExecutionResult result = doExecute(request.getWorkflow(), request.getConfig(), request.getInputs(),
                    effectiveRunId, request.getFlowId(), request.getOperatorId(), request.getOrgId(), request.getFlowType(), rootContext, null);

            // 出参载荷
            if (result.getOutput() != null && !result.getOutput().isEmpty()) {
                span.setAttribute(TraceConstant.ATTR_PAYLOAD_OUTPUT, JSON.toJSONString(result.getOutput()));
            }

            return result;
        });
    }

    /**
     * 流式执行工作流，返回事件流。
     *
     * @param request 执行请求
     * @return 事件 Flux
     */
    public Flux<WorkflowStreamEvent> streamExecute(WorkflowExecuteRequest request) {
        // 创建 Sink 用于推送事件
        Sinks.Many<WorkflowStreamEvent> sink = Sinks.many().multicast().onBackpressureBuffer();

        // 在单独的线程中执行工作流
        CompletableFuture.runAsync(() -> {
            try {
                TraceUtil.span(TraceConstant.SPAN_WORKFLOW_STREAM_EXECUTE, SpanKind.INTERNAL, null, span -> {
                    String effectiveRunId = initTraceSpan(request, span);

                    // 保存当前 context 作为 root context，供后续节点 span 使用
                    io.opentelemetry.context.Context rootContext = io.opentelemetry.context.Context.current();

                    // 创建事件回调
                    WorkflowStreamEventCallback callback = sink::tryEmitNext;

                    WorkflowExecutionResult result = doExecute(request.getWorkflow(), request.getConfig(), request.getInputs(),
                            effectiveRunId, request.getFlowId(), request.getOperatorId(), request.getOrgId(), request.getFlowType(), rootContext, callback);

                    // 推送工作流完成事件
                    sink.tryEmitNext(WorkflowCompleteEvent.create(effectiveRunId, result.getOutput()));

                    // 出参载荷
                    if (result.getOutput() != null && !result.getOutput().isEmpty()) {
                        span.setAttribute(TraceConstant.ATTR_PAYLOAD_OUTPUT, JSON.toJSONString(result.getOutput()));
                    }

                    return result;
                });
            } catch (Exception e) {
                // 异常情况下不需要额外推送，因为节点已经推送了错误事件
                LOGGER.error("工作流流式执行异常", e);
            } finally {
                // 完成事件流
                sink.tryEmitComplete();
            }
        });

        return sink.asFlux();
    }

    private WorkflowExecutionResult doExecute(Workflow workflow,
                                              WorkflowConfig config,
                                              Map<String, Object> inputs,
                                              String runId,
                                              Long flowId,
                                              Long operatorId,
                                              Integer orgId,
                                              com.iusofts.agentplus.aiflow.enums.FlowTypeEnum flowType,
                                              io.opentelemetry.context.Context rootContext,
                                              WorkflowStreamEventCallback eventCallback) {
        WorkflowGraphCompiler.Compiled compiled = compiler.compile(workflow);
        ExecutionContext ctx = new ExecutionContext(runId, config, inputs, flowId, operatorId, orgId, flowType);
        ctx.setRootContext(rootContext);
        compiled.batchSubGraphs().forEach(ctx::registerBatchSubGraph);

        // 设置事件回调和流式标志
        if (eventCallback != null) {
            ctx.setEventCallback(eventCallback);
            ctx.setStreamingExecution(true);
        }

        // 设置节点名称和类型映射
        Map<String, String> nodeNameMap = new LinkedHashMap<>();
        Map<String, String> nodeTypeMap = new LinkedHashMap<>();
        if (workflow.getNodes() != null) {
            for (Node node : workflow.getNodes()) {
                String name = node.getData() == null ? null : node.getData().getLabel();
                if (name == null || name.isBlank()) {
                    name = node.getLabel();
                }
                nodeNameMap.put(node.getId(), name);
                nodeTypeMap.put(node.getId(), node.getType());
            }
        }
        ctx.setNodeInfoMaps(nodeNameMap, nodeTypeMap);

        CompiledGraph<WorkflowState> mainGraph = compiled.mainGraph();
        try {
            // 将 ctx 注册到 tracker 中
            ExecutionContextTracker tracker = new ExecutionContextTracker(ctx);
            mainGraph.invoke(Map.of(WorkflowState.CTX_KEY, tracker));
        } catch (WorkflowExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new WorkflowExecutionException("工作流执行失败: " + e.getMessage(), e);
        } finally {
            // 执行完毕清理 tracker，防止内存泄漏
            ExecutionContextTracker.removeRun(runId);
        }

        fillSkipped(compiled.nodeIds(), ctx);
        Map<String, Object> finalOutput = collectEndOutputs(compiled.endNodeIds(), workflow, ctx);
        return new WorkflowExecutionResult(runId, finalOutput,
                ctx.snapshotOutputs(), ctx.getNodeStatus(), ctx.snapshotTimings());
    }

    private void fillSkipped(java.util.Set<String> allNodeIds, ExecutionContext ctx) {
        for (String id : allNodeIds) {
            if (ctx.getStatus(id) == NodeExecutionStatus.PENDING) {
                ctx.updateStatus(id, NodeExecutionStatus.SKIPPED);
                ctx.putOutput(NodeOutput.empty(id));
            }
        }
    }

    private Map<String, Object> collectEndOutputs(java.util.Set<String> endNodeIds,
                                                  Workflow workflow,
                                                  ExecutionContext ctx) {
        Map<String, Object> merged = new LinkedHashMap<>();

        // 1. 合并 End 节点:text 直接作为 finalOutput.text(原行为);
        //    其他字段(inputTokens/outputTokens 等)直接合并。
        for (Node n : workflow.getNodes()) {
            if (!endNodeIds.contains(n.getId())) {
                continue;
            }
            if (ctx.getStatus(n.getId()) != NodeExecutionStatus.SUCCESS) {
                continue;
            }
            NodeOutput out = ctx.getOutput(n.getId());
            if (out != null && out.getOutputs() != null) {
                merged.putAll(out.getOutputs());
            }
        }

        // 2. 合并 Output 节点:每个 Output 节点作为独立消息条目放进 finalOutput.outputs 数组
        //    (供 ChatService 收到 workflow_complete 时各入库一条 ai_message);
        //    Output 节点内容<b>不</b>覆盖 finalOutput.text(End 节点的 text)。
        java.util.List<Map<String, Object>> outputs = new java.util.ArrayList<>();
        for (Node n : workflow.getNodes()) {
            if (!FlowNodeType.OUTPUT.getCode().equalsIgnoreCase(n.getType())) {
                continue;
            }
            if (ctx.getStatus(n.getId()) != NodeExecutionStatus.SUCCESS) {
                continue;
            }
            NodeOutput out = ctx.getOutput(n.getId());
            if (out == null || out.getOutputs() == null) {
                continue;
            }
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("nodeId", n.getId());
            entry.put("nodeName", n.getData() == null ? null : n.getData().getLabel());
            entry.putAll(out.getOutputs());
            outputs.add(entry);
        }
        if (!outputs.isEmpty()) {
            merged.put("outputs", outputs);
        }
        return merged;
    }

    public NodeExecutorRegistry registry() {
        return registry;
    }

    public static class Builder {

        private ChatModelProvider chatModelProvider;
        private KnowledgeRetriever knowledgeRetriever;
        private ToolRegistry toolRegistry;
        private ToolQueryProvider toolQueryProvider;
        private HistoryMessageProvider historyMessageProvider;
        private final NodeExecutorRegistry registry = new NodeExecutorRegistry();

        public Builder chatModelProvider(ChatModelProvider provider) {
            this.chatModelProvider = provider;
            return this;
        }

        public Builder knowledgeRetriever(KnowledgeRetriever retriever) {
            this.knowledgeRetriever = retriever;
            return this;
        }

        public Builder toolRegistry(ToolRegistry toolRegistry) {
            this.toolRegistry = toolRegistry;
            return this;
        }

        public Builder toolQueryProvider(ToolQueryProvider toolQueryProvider) {
            this.toolQueryProvider = toolQueryProvider;
            return this;
        }

        /** 可选:注入历史消息提供者,用于加载会话历史消息。 */
        public Builder historyMessageProvider(HistoryMessageProvider provider) {
            this.historyMessageProvider = provider;
            return this;
        }

        /** 追加/覆盖自定义节点执行器,类型冲突时后注册者胜出。 */
        public Builder registerExecutor(NodeExecutor executor) {
            registry.register(executor);
            return this;
        }

        public WorkflowEngine build() {
            if (chatModelProvider == null) {
                throw new WorkflowExecutionException("chatModelProvider 必填");
            }
            KnowledgeRetriever retriever = knowledgeRetriever == null
                    ? new NoopKnowledgeRetriever()
                    : knowledgeRetriever;

            registry.register(new StartNodeExecutor())
                    .register(new EndNodeExecutor())
                    .register(new OutputNodeExecutor())
                    .register(new ConditionNodeExecutor())
                    .register(new AggregatorNodeExecutor())
                    .register(new BatchNodeExecutor())
                    .register(new KnowledgeNodeExecutor(retriever))
                    .register(new LLMNodeExecutor(chatModelProvider, toolQueryProvider, toolRegistry, historyMessageProvider))
                    .register(new CodeNodeExecutor());

            if (toolRegistry != null && toolQueryProvider != null) {
                registry.register(new ToolNodeExecutor(toolRegistry, toolQueryProvider));
            }

            return new WorkflowEngine(registry);
        }
    }

    private static String initTraceSpan(WorkflowExecuteRequest request, Span span) {
        // 以 OTel traceId 作为 runId;SDK 未初始化时回退传入值
        String effectiveRunId = span.getSpanContext().isValid()
                ? span.getSpanContext().getTraceId()
                : request.getRunId();
        span.setAttribute(TraceConstant.ATTR_LABEL, request.getFlowName() != null ? request.getFlowName() : "");
        span.setAttribute(TraceConstant.ATTR_WORKFLOW_RUN_ID, effectiveRunId);

        // 设置AI属性
        TraceUtil.setAiAttributes("FLOW", request.getFlowId(), null, request.getOperatorId(), request.getOrgId());

        span.setAttribute(TraceConstant.ATTR_TRIAL_FLAG, request.getTrialFlag());

        // 入参载荷
        if (request.getInputs() != null && !request.getInputs().isEmpty()) {
            span.setAttribute(TraceConstant.ATTR_PAYLOAD_INPUT, JSON.toJSONString(request.getInputs()));
        }
        return effectiveRunId;
    }
}
