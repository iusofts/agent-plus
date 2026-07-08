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
import com.iusofts.agentplus.engine.executor.impl.ConditionNodeExecutor;
import com.iusofts.agentplus.engine.executor.impl.EndNodeExecutor;
import com.iusofts.agentplus.engine.executor.impl.KnowledgeNodeExecutor;
import com.iusofts.agentplus.engine.executor.impl.LLMNodeExecutor;
import com.iusofts.agentplus.engine.executor.impl.StartNodeExecutor;
import com.iusofts.agentplus.engine.graph.ExecutionContextTracker;
import com.iusofts.agentplus.engine.graph.WorkflowGraphCompiler;
import com.iusofts.agentplus.engine.graph.WorkflowState;
import com.iusofts.agentplus.engine.knowledge.KnowledgeRetriever;
import com.iusofts.agentplus.engine.knowledge.NoopKnowledgeRetriever;
import com.iusofts.agentplus.engine.llm.ChatModelProvider;
import org.bsc.langgraph4j.CompiledGraph;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

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

    public WorkflowExecutionResult execute(Workflow workflow,
                                           WorkflowConfig config,
                                           Map<String, Object> inputs) {
        return execute(workflow, config, inputs, UUID.randomUUID().toString());
    }

    public WorkflowExecutionResult execute(Workflow workflow,
                                           WorkflowConfig config,
                                           Map<String, Object> inputs,
                                           String runId) {
        WorkflowGraphCompiler.Compiled compiled = compiler.compile(workflow);
        ExecutionContext ctx = new ExecutionContext(runId, config, inputs);
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
        return new WorkflowExecutionResult(runId, finalOutput, ctx.snapshotOutputs(), ctx.getNodeStatus());
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
        private final NodeExecutorRegistry registry = new NodeExecutorRegistry();

        public Builder chatModelProvider(ChatModelProvider provider) {
            this.chatModelProvider = provider;
            return this;
        }

        public Builder knowledgeRetriever(KnowledgeRetriever retriever) {
            this.knowledgeRetriever = retriever;
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
                    .register(new LLMNodeExecutor(chatModelProvider));

            return new WorkflowEngine(registry);
        }
    }
}
