package com.iusofts.agentplus.engine;

import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.Workflow;
import com.iusofts.agentplus.aiflow.vo.workflow.config.WorkflowConfig;
import com.iusofts.agentplus.engine.context.ExecutionContext;
import com.iusofts.agentplus.engine.context.NodeExecutionStatus;
import com.iusofts.agentplus.engine.context.NodeOutput;
import com.iusofts.agentplus.engine.exception.WorkflowExecutionException;
import com.iusofts.agentplus.engine.executor.NodeExecutor;
import com.iusofts.agentplus.engine.executor.NodeExecutorRegistry;
import com.iusofts.agentplus.engine.executor.impl.AggregatorNodeExecutor;
import com.iusofts.agentplus.engine.executor.impl.BatchNodeExecutor;
import com.iusofts.agentplus.engine.executor.impl.CodeNodeExecutor;
import com.iusofts.agentplus.engine.executor.impl.ConditionNodeExecutor;
import com.iusofts.agentplus.engine.executor.impl.EndNodeExecutor;
import com.iusofts.agentplus.engine.executor.impl.KnowledgeNodeExecutor;
import com.iusofts.agentplus.engine.executor.impl.LLMNodeExecutor;
import com.iusofts.agentplus.engine.executor.impl.StartNodeExecutor;
import com.iusofts.agentplus.engine.executor.impl.ToolNodeExecutor;
import com.iusofts.agentplus.engine.history.HistoryMessageProvider;
import com.iusofts.agentplus.engine.tool.ToolRegistry;
import com.iusofts.agentplus.tool.ToolQueryProvider;
import com.iusofts.agentplus.engine.graph.ExecutionContextTracker;
import com.iusofts.agentplus.engine.graph.WorkflowGraphCompiler;
import com.iusofts.agentplus.engine.graph.WorkflowState;
import com.iusofts.agentplus.engine.knowledge.KnowledgeRetriever;
import com.iusofts.agentplus.engine.knowledge.NoopKnowledgeRetriever;
import com.iusofts.agentplus.engine.llm.ChatModelProvider;
import com.iusofts.agentplus.trace.TraceUtil;
import com.alibaba.fastjson2.JSON;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import org.bsc.langgraph4j.CompiledGraph;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static com.iusofts.agentplus.trace.TraceUtil.ATTR_LABEL;

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
     * 若 OTel SDK 未初始化(如单元测试),span context 无效时回退使用传入的 {@code runId}。</p>
     */
    public WorkflowExecutionResult execute(WorkflowExecuteRequest request) {
        // 使用 root() 作为父 Context，确保每次都是新的 trace，不继承上一次请求的残留 Context
        return TraceUtil.span("workflow.execute", SpanKind.INTERNAL, io.opentelemetry.context.Context.root(), span -> {
            // 以 OTel traceId 作为 runId;SDK 未初始化时回退传入值
            String effectiveRunId = span.getSpanContext().isValid()
                    ? span.getSpanContext().getTraceId()
                    : request.getRunId();

            span.setAttribute(ATTR_LABEL, request.getFlowName() != null ? request.getFlowName() : "");
            span.setAttribute("workflow.runId", effectiveRunId);
            if (request.getFlowId() != null) {
                span.setAttribute("flowId", request.getFlowId());
            }
            if (request.getOperatorId() != null) {
                span.setAttribute("operatorId", request.getOperatorId());
            }
            if (request.getOrgId() != null) {
                span.setAttribute("orgId", request.getOrgId().longValue());
            }
            if (request.getTrialFlag() != null) {
                span.setAttribute("trialFlag", request.getTrialFlag() != 0);
            }

            // 入参载荷
            if (request.getInputs() != null && !request.getInputs().isEmpty()) {
                span.setAttribute("ap.payload.input", JSON.toJSONString(request.getInputs()));
            }

            // 保存当前 context 作为 root context，供后续节点 span 使用
            io.opentelemetry.context.Context rootContext = io.opentelemetry.context.Context.current();

            WorkflowExecutionResult result = doExecute(request.getWorkflow(), request.getConfig(), request.getInputs(),
                    effectiveRunId, request.getFlowId(), request.getOperatorId(), request.getOrgId(), rootContext);

            // 出参载荷
            if (result.getOutput() != null && !result.getOutput().isEmpty()) {
                span.setAttribute("ap.payload.output", JSON.toJSONString(result.getOutput()));
            }

            return result;
        });
    }

    private WorkflowExecutionResult doExecute(Workflow workflow,
                                              WorkflowConfig config,
                                              Map<String, Object> inputs,
                                              String runId,
                                              Long flowId,
                                              Long operatorId,
                                              Integer orgId,
                                              io.opentelemetry.context.Context rootContext) {
        WorkflowGraphCompiler.Compiled compiled = compiler.compile(workflow);
        ExecutionContext ctx = new ExecutionContext(runId, config, inputs, flowId, operatorId, orgId);
        ctx.setRootContext(rootContext);
        compiled.batchSubGraphs().forEach(ctx::registerBatchSubGraph);

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
        for (Node n : workflow.getNodes()) {
            if (!endNodeIds.contains(n.getId())) {
                continue;
            }
            if (ctx.getStatus(n.getId()) != NodeExecutionStatus.SUCCESS) {
                continue;
            }
            NodeOutput out = ctx.getOutput(n.getId());
            if (out != null) {
                merged.putAll(out.getOutputs());
            }
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
}
