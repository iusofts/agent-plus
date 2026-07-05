package com.iusofts.agentplus.engine.dag;

import com.iusofts.agentplus.aiflow.vo.workflow.Edge;
import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.engine.context.ExecutionContext;
import com.iusofts.agentplus.engine.context.NodeExecutionStatus;
import com.iusofts.agentplus.engine.context.NodeOutput;
import com.iusofts.agentplus.engine.exception.WorkflowExecutionException;
import com.iusofts.agentplus.engine.executor.NodeExecutorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 基于 Kahn 算法的拓扑调度器,同时处理条件分支剪枝。
 *
 * <p>核心思路:每个节点跟踪一个"待决 incoming 边"计数与"是否至少有一条 incoming 被激活"标志。
 * 当所有 incoming 边都出结果时:</p>
 * <ul>
 *   <li>有激活边 -> 调度执行;</li>
 *   <li>全部被剪 -> 标记 SKIPPED,并继续向下游传播剪枝信号。</li>
 * </ul>
 *
 * <p>条件节点执行后,{@link NodeOutput#getChosenBranch()} 决定哪个 sourceHandle 被激活,
 * 其他 outgoing 边视为 dead。</p>
 *
 * @author Ivan
 */
public class TopologicalScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopologicalScheduler.class);

    private final DagGraph graph;
    private final NodeExecutorRegistry registry;

    public TopologicalScheduler(DagGraph graph, NodeExecutorRegistry registry) {
        this.graph = graph;
        this.registry = registry;
    }

    public void run(ExecutionContext ctx) {
        Map<String, Integer> pending = new HashMap<>();
        Map<String, Boolean> hasLive = new HashMap<>();
        for (String id : graph.getNodes().keySet()) {
            pending.put(id, graph.incomingOf(id).size());
            hasLive.put(id, false);
        }

        Deque<String> ready = new ArrayDeque<>(graph.getStartNodeIds());
        // Start 节点视为默认激活
        for (String id : graph.getStartNodeIds()) {
            hasLive.put(id, true);
        }

        while (!ready.isEmpty()) {
            String cur = ready.pollFirst();
            NodeExecutionStatus status = ctx.getStatus(cur);
            if (status == NodeExecutionStatus.SUCCESS
                    || status == NodeExecutionStatus.SKIPPED
                    || status == NodeExecutionStatus.FAILED) {
                continue;
            }

            Node node = graph.node(cur);
            boolean live = Boolean.TRUE.equals(hasLive.get(cur));
            if (!live) {
                markSkipped(cur, ctx, pending, hasLive, ready);
                continue;
            }

            NodeOutput output = executeNode(node, ctx);
            ctx.putOutput(output);
            ctx.updateStatus(cur, NodeExecutionStatus.SUCCESS);
            releaseDownstream(cur, output, ctx, pending, hasLive, ready);
        }
    }

    private NodeOutput executeNode(Node node, ExecutionContext ctx) {
        ctx.updateStatus(node.getId(), NodeExecutionStatus.RUNNING);
        try {
            LOGGER.debug("execute node id={} type={}", node.getId(), node.getType());
            return registry.get(node.getType()).execute(node, ctx);
        } catch (WorkflowExecutionException e) {
            ctx.updateStatus(node.getId(), NodeExecutionStatus.FAILED);
            throw e;
        } catch (Exception e) {
            ctx.updateStatus(node.getId(), NodeExecutionStatus.FAILED);
            throw new WorkflowExecutionException(node.getId(), "节点执行异常", e);
        }
    }

    private void releaseDownstream(String cur,
                                   NodeOutput output,
                                   ExecutionContext ctx,
                                   Map<String, Integer> pending,
                                   Map<String, Boolean> hasLive,
                                   Deque<String> ready) {
        String chosen = output.getChosenBranch();
        for (Edge edge : graph.outgoingOf(cur)) {
            boolean alive = chosen == null || matchesBranch(edge, chosen);
            String target = edge.getTarget();
            if (alive) {
                hasLive.put(target, true);
            }
            decrement(target, pending, ready);
        }
    }

    private void markSkipped(String cur,
                             ExecutionContext ctx,
                             Map<String, Integer> pending,
                             Map<String, Boolean> hasLive,
                             Deque<String> ready) {
        ctx.updateStatus(cur, NodeExecutionStatus.SKIPPED);
        ctx.putOutput(NodeOutput.empty(cur));
        LOGGER.debug("skip node id={}", cur);
        for (Edge edge : graph.outgoingOf(cur)) {
            decrement(edge.getTarget(), pending, ready);
        }
    }

    private void decrement(String target, Map<String, Integer> pending, Deque<String> ready) {
        int left = pending.getOrDefault(target, 0) - 1;
        pending.put(target, left);
        if (left <= 0) {
            ready.offerLast(target);
        }
    }

    private boolean matchesBranch(Edge edge, String chosen) {
        // 优先看 sourceHandle,其次看 edge.id
        return Objects.equals(edge.getSourceHandle(), chosen)
                || Objects.equals(edge.getId(), chosen)
                || (edge.getSourceHandle() == null && "else".equalsIgnoreCase(chosen));
    }

    // 参数保留供未来扩展并行调度使用
    @SuppressWarnings("unused")
    private List<Edge> outgoing(String id) {
        return graph.outgoingOf(id);
    }
}
