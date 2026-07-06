package com.iusofts.agentplus.engine;

import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.Workflow;
import com.iusofts.agentplus.aiflow.vo.workflow.config.WorkflowConfig;
import com.iusofts.agentplus.engine.context.ExecutionContext;
import com.iusofts.agentplus.engine.context.NodeExecutionStatus;
import com.iusofts.agentplus.engine.context.NodeOutput;
import com.iusofts.agentplus.engine.dag.DagBuilder;
import com.iusofts.agentplus.engine.dag.DagGraph;
import com.iusofts.agentplus.engine.dag.TopologicalScheduler;
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
import com.iusofts.agentplus.engine.knowledge.KnowledgeRetriever;
import com.iusofts.agentplus.engine.knowledge.NoopKnowledgeRetriever;
import com.iusofts.agentplus.engine.llm.ChatModelProvider;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 工作流执行引擎入口。
 *
 * <p>使用方式:</p>
 * <pre>{@code
 * WorkflowEngine engine = WorkflowEngine.builder()
 *         .chatModelProvider(myChatModelProvider)
 *         .knowledgeRetriever(myRetriever)
 *         .build();
 * WorkflowExecutionResult result = engine.execute(workflow, config, inputs);
 * }</pre>
 *
 * <p>整个执行流程:</p>
 * <ol>
 *   <li>{@link DagBuilder} 编译 {@link Workflow} 为 {@link DagGraph},做环检测。</li>
 *   <li>创建 {@link ExecutionContext} 装入全局输入与环境变量。</li>
 *   <li>{@link TopologicalScheduler} 按 Kahn 拓扑序调度,遇条件节点执行分支剪枝。</li>
 *   <li>汇总所有 End 节点的输出作为最终结果。</li>
 * </ol>
 *
 * @author Ivan
 */
public class WorkflowEngine {

    private final NodeExecutorRegistry registry;

    private WorkflowEngine(NodeExecutorRegistry registry) {
        this.registry = registry;
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
        DagGraph graph = DagBuilder.build(workflow);
        ExecutionContext ctx = new ExecutionContext(runId, config, inputs);

        TopologicalScheduler scheduler = new TopologicalScheduler(graph, registry);
        scheduler.run(ctx);

        Map<String, Object> finalOutput = collectEndOutputs(graph, ctx);
        return new WorkflowExecutionResult(runId, finalOutput, ctx.snapshotOutputs(), ctx.getNodeStatus());
    }

    private Map<String, Object> collectEndOutputs(DagGraph graph, ExecutionContext ctx) {
        Map<String, Object> merged = new LinkedHashMap<>();
        for (Map.Entry<String, Node> e : graph.getNodes().entrySet()) {
            if (!"End".equalsIgnoreCase(e.getValue().getType())) {
                continue;
            }
            NodeExecutionStatus st = ctx.getStatus(e.getKey());
            if (st != NodeExecutionStatus.SUCCESS) {
                continue;
            }
            NodeOutput out = ctx.getOutput(e.getKey());
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
                    .register(new BatchNodeExecutor(registry))
                    .register(new KnowledgeNodeExecutor(retriever))
                    .register(new LLMNodeExecutor(chatModelProvider));

            return new WorkflowEngine(registry);
        }
    }
}
