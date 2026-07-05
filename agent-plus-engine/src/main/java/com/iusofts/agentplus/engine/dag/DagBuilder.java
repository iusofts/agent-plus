package com.iusofts.agentplus.engine.dag;

import com.iusofts.agentplus.aiflow.vo.workflow.Edge;
import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.Workflow;
import com.iusofts.agentplus.engine.exception.WorkflowExecutionException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 将 {@link Workflow} 编译为 {@link DagGraph},并做环检测。
 *
 * @author Ivan
 */
public final class DagBuilder {

    private DagBuilder() {
    }

    public static DagGraph build(Workflow workflow) {
        if (workflow == null || workflow.getNodes() == null || workflow.getNodes().isEmpty()) {
            throw new WorkflowExecutionException("workflow 或节点列表为空");
        }

        Map<String, Node> nodes = new LinkedHashMap<>();
        for (Node node : workflow.getNodes()) {
            if (node.getId() == null) {
                throw new WorkflowExecutionException("存在缺失 id 的节点");
            }
            if (nodes.containsKey(node.getId())) {
                throw new WorkflowExecutionException("节点 id 重复: " + node.getId());
            }
            nodes.put(node.getId(), node);
        }

        Map<String, List<Edge>> outgoing = new HashMap<>();
        Map<String, List<Edge>> incoming = new HashMap<>();
        List<Edge> edges = workflow.getEdges() == null ? List.of() : workflow.getEdges();
        for (Edge edge : edges) {
            if (edge.getSource() == null || edge.getTarget() == null) {
                throw new WorkflowExecutionException("边缺失 source/target: " + edge.getId());
            }
            if (!nodes.containsKey(edge.getSource()) || !nodes.containsKey(edge.getTarget())) {
                throw new WorkflowExecutionException("边指向未知节点: " + edge.getId());
            }
            outgoing.computeIfAbsent(edge.getSource(), k -> new ArrayList<>()).add(edge);
            incoming.computeIfAbsent(edge.getTarget(), k -> new ArrayList<>()).add(edge);
        }

        Set<String> startNodeIds = new HashSet<>();
        for (Node node : nodes.values()) {
            boolean noIncoming = !incoming.containsKey(node.getId());
            boolean isStart = "Start".equalsIgnoreCase(node.getType());
            if (noIncoming || isStart) {
                startNodeIds.add(node.getId());
            }
        }
        if (startNodeIds.isEmpty()) {
            throw new WorkflowExecutionException("找不到 Start 节点");
        }

        detectCycle(nodes.keySet(), outgoing);

        return new DagGraph(nodes, outgoing, incoming, startNodeIds);
    }

    private static void detectCycle(Set<String> nodeIds, Map<String, List<Edge>> outgoing) {
        Map<String, Integer> state = new HashMap<>();
        for (String id : nodeIds) {
            if (state.getOrDefault(id, 0) == 0 && dfs(id, outgoing, state)) {
                throw new WorkflowExecutionException("工作流存在环: 起点 " + id);
            }
        }
    }

    /** 0=未访问,1=正在访问,2=完成。返回是否检测到环。 */
    private static boolean dfs(String cur, Map<String, List<Edge>> outgoing, Map<String, Integer> state) {
        state.put(cur, 1);
        for (Edge e : outgoing.getOrDefault(cur, List.of())) {
            Integer s = state.getOrDefault(e.getTarget(), 0);
            if (s == 1) {
                return true;
            }
            if (s == 0 && dfs(e.getTarget(), outgoing, state)) {
                return true;
            }
        }
        state.put(cur, 2);
        return false;
    }
}
