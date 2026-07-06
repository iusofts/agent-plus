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
 * 基于 Kahn 算法的拓扑调度器,同时处理条件分支剪枝与批处理子图跳过。
 *
 * <p>核心思路:每个节点跟踪一个"待决 incoming 边"计数与"是否至少有一条 incoming 被激活"标志。
 * 当所有 incoming 边都出结果时:</p>
 * <ul>
 *   <li>有激活边 -> 调度执行;</li>
 *   <li>全部被剪 -> 标记 SKIPPED,并继续向下游传播剪枝信号。</li>
 * </ul>
 *
 * <p>批处理子节点(node.parentNode == batchId)不在主循环中调度,由
 * {@code BatchNodeExecutor} 在 batch 节点执行时接管;批处理节点的 loop-back 入边
 * 与 body-entry 出边被剔除,不参与主图 pending/release 计算。</p>
 *
 * @author Ivan
 */
public class TopologicalScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopologicalScheduler.class);
    private static final String CONDITION_HANDLE_PREFIX = "condition:";

    private final DagGraph graph;
    private final NodeExecutorRegistry registry;

    public TopologicalScheduler(DagGraph graph, NodeExecutorRegistry registry) {
        this.graph = graph;
        this.registry = registry;
    }

    public void run(ExecutionContext ctx) {
        if (ctx.getGraph() == null) {
            ctx.attachGraph(graph);
        }

        Map<String, Integer> pending = new HashMap<>();
        Map<String, Boolean> hasLive = new HashMap<>();
        for (String id : graph.getNodes().keySet()) {
            if (graph.isBatchInternal(id)) {
                continue;
            }
            pending.put(id, countPendingIncoming(id));
            hasLive.put(id, false);
        }

        Deque<String> ready = new ArrayDeque<>(graph.getStartNodeIds());
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
            releaseDownstream(cur, output, pending, hasLive, ready);
        }
    }

    private int countPendingIncoming(String nodeId) {
        int count = 0;
        for (Edge edge : graph.incomingOf(nodeId)) {
            if (isMainScopeEdge(edge)) {
                count++;
            }
        }
        return count;
    }

    private boolean isMainScopeEdge(Edge edge) {
        // 剔除 batch 子图相关的边:loop-back(回流)与 body-entry(进入子图)。
        if (DagBuilder.HANDLE_BATCH_INTERNAL_TARGET.equals(edge.getTargetHandle())) {
            return false;
        }
        if (DagBuilder.HANDLE_BATCH_INTERNAL_SOURCE.equals(edge.getSourceHandle())) {
            return false;
        }
        // batch-internal 节点之间的边也不属于主图。
        return !graph.isBatchInternal(edge.getSource()) && !graph.isBatchInternal(edge.getTarget());
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
                                   Map<String, Integer> pending,
                                   Map<String, Boolean> hasLive,
                                   Deque<String> ready) {
        String chosen = output.getChosenBranch();
        for (Edge edge : graph.outgoingOf(cur)) {
            if (!isMainScopeEdge(edge)) {
                continue;
            }
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
            if (!isMainScopeEdge(edge)) {
                continue;
            }
            decrement(edge.getTarget(), pending, ready);
        }
    }

    private void decrement(String target, Map<String, Integer> pending, Deque<String> ready) {
        Integer cur = pending.get(target);
        if (cur == null) {
            return;
        }
        int left = cur - 1;
        pending.put(target, left);
        if (left <= 0) {
            ready.offerLast(target);
        }
    }

    private boolean matchesBranch(Edge edge, String chosen) {
        String handle = edge.getSourceHandle();
        if (Objects.equals(handle, chosen) || Objects.equals(edge.getId(), chosen)) {
            return true;
        }
        // 兼容前端形如 "condition:<id>" 的 handle 写法
        if (handle != null && handle.startsWith(CONDITION_HANDLE_PREFIX)
                && Objects.equals(handle.substring(CONDITION_HANDLE_PREFIX.length()), chosen)) {
            return true;
        }
        return handle == null && "else".equalsIgnoreCase(chosen);
    }

    // 参数保留供未来扩展并行调度使用
    @SuppressWarnings("unused")
    private List<Edge> outgoing(String id) {
        return graph.outgoingOf(id);
    }
}
