package com.iusofts.agentplus.engine.dag;

import com.iusofts.agentplus.aiflow.enums.FlowNodeType;
import com.iusofts.agentplus.aiflow.vo.workflow.Edge;
import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.Workflow;
import com.iusofts.agentplus.engine.exception.WorkflowExecutionException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 将 {@link Workflow} 编译为 {@link DagGraph},并做环检测。
 *
 * <p>对批处理节点做额外处理:根据 {@code parentNode} 归属识别子节点集合,
 * 根据 4 个约定 handle 分类边(bodyEntry / loopBack / upstream / downstream)。
 * 环检测时会剔除所有 loop-back 边,因此"起终点都是 batch 自己"的合法子图不会误判为环。</p>
 *
 * @author Ivan
 */
public final class DagBuilder {

    public static final String HANDLE_BATCH_INTERNAL_SOURCE = "batch-internal-source";
    public static final String HANDLE_BATCH_INTERNAL_TARGET = "batch-internal-target";
    public static final String HANDLE_BATCH_EXTERNAL_SOURCE = "batch-external-source";
    public static final String HANDLE_BATCH_EXTERNAL_TARGET = "batch-external-target";

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

        Map<String, DagGraph.BatchSubgraph> batchSubgraphs = buildBatchSubgraphs(nodes, outgoing, incoming);
        Set<String> batchInternalNodes = new HashSet<>();
        for (DagGraph.BatchSubgraph sub : batchSubgraphs.values()) {
            batchInternalNodes.addAll(sub.subNodes());
        }

        Set<String> startNodeIds = new LinkedHashSet<>();
        for (Node node : nodes.values()) {
            if (batchInternalNodes.contains(node.getId())) {
                continue;
            }
            boolean noMainIncoming = incoming.getOrDefault(node.getId(), List.of()).stream()
                    .noneMatch(e -> !batchInternalNodes.contains(e.getSource())
                            && !HANDLE_BATCH_INTERNAL_TARGET.equals(e.getTargetHandle()));
            boolean isStart = FlowNodeType.START.getCode().equalsIgnoreCase(node.getType());
            if (noMainIncoming || isStart) {
                startNodeIds.add(node.getId());
            }
        }
        if (startNodeIds.isEmpty()) {
            throw new WorkflowExecutionException("找不到 Start 节点");
        }

        detectCycle(nodes.keySet(), outgoing);

        return new DagGraph(nodes, outgoing, incoming, startNodeIds, batchSubgraphs, batchInternalNodes);
    }

    private static Map<String, DagGraph.BatchSubgraph> buildBatchSubgraphs(Map<String, Node> nodes,
                                                                          Map<String, List<Edge>> outgoing,
                                                                          Map<String, List<Edge>> incoming) {
        Map<String, DagGraph.BatchSubgraph> result = new LinkedHashMap<>();
        Map<String, Set<String>> childrenOf = new HashMap<>();
        for (Node n : nodes.values()) {
            if (n.getParentNode() != null && !n.getParentNode().isEmpty()) {
                childrenOf.computeIfAbsent(n.getParentNode(), k -> new LinkedHashSet<>()).add(n.getId());
            }
        }

        for (Node n : nodes.values()) {
            if (!FlowNodeType.BATCH.getCode().equalsIgnoreCase(n.getType())) {
                continue;
            }
            String batchId = n.getId();
            Set<String> subNodes = childrenOf.getOrDefault(batchId, Collections.emptySet());

            for (String childId : subNodes) {
                Node child = nodes.get(childId);
                if (child != null && FlowNodeType.BATCH.getCode().equalsIgnoreCase(child.getType())) {
                    throw new WorkflowExecutionException("暂不支持嵌套批处理: " + batchId + " 包含 " + childId);
                }
            }

            List<Edge> bodyEntries = new ArrayList<>();
            for (Edge e : outgoing.getOrDefault(batchId, List.of())) {
                if (HANDLE_BATCH_INTERNAL_SOURCE.equals(e.getSourceHandle())) {
                    bodyEntries.add(e);
                }
            }
            List<Edge> loopBacks = new ArrayList<>();
            for (Edge e : incoming.getOrDefault(batchId, List.of())) {
                if (HANDLE_BATCH_INTERNAL_TARGET.equals(e.getTargetHandle())) {
                    loopBacks.add(e);
                }
            }

            Set<String> entryTargets = new LinkedHashSet<>();
            for (Edge e : bodyEntries) {
                entryTargets.add(e.getTarget());
            }
            Set<String> returnSources = new LinkedHashSet<>();
            for (Edge e : loopBacks) {
                returnSources.add(e.getSource());
            }

            result.put(batchId, new DagGraph.BatchSubgraph(
                    Collections.unmodifiableSet(new LinkedHashSet<>(subNodes)),
                    Collections.unmodifiableList(bodyEntries),
                    Collections.unmodifiableList(loopBacks),
                    Collections.unmodifiableSet(entryTargets),
                    Collections.unmodifiableSet(returnSources)));
        }
        return result;
    }

    private static void detectCycle(Set<String> nodeIds, Map<String, List<Edge>> outgoing) {
        Map<String, Integer> state = new HashMap<>();
        for (String id : nodeIds) {
            if (state.getOrDefault(id, 0) == 0 && dfs(id, outgoing, state)) {
                throw new WorkflowExecutionException("工作流存在环: 起点 " + id);
            }
        }
    }

    /** 0=未访问,1=正在访问,2=完成。返回是否检测到环。loop-back 边(targetHandle==batch-internal-target)不参与判环。 */
    private static boolean dfs(String cur, Map<String, List<Edge>> outgoing, Map<String, Integer> state) {
        state.put(cur, 1);
        for (Edge e : outgoing.getOrDefault(cur, List.of())) {
            if (HANDLE_BATCH_INTERNAL_TARGET.equals(e.getTargetHandle())) {
                continue;
            }
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
