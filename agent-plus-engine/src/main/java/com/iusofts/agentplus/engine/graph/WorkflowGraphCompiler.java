package com.iusofts.agentplus.engine.graph;

import com.iusofts.agentplus.aiflow.enums.FlowNodeType;
import com.iusofts.agentplus.aiflow.vo.workflow.Edge;
import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.Workflow;
import com.iusofts.agentplus.aiflow.vo.workflow.data.InputParamNodeData;
import com.iusofts.agentplus.engine.context.ExecutionContext;
import com.iusofts.agentplus.engine.context.NodeExecutionStatus;
import com.iusofts.agentplus.engine.context.NodeOutput;
import com.iusofts.agentplus.engine.exception.WorkflowExecutionException;
import com.iusofts.agentplus.engine.executor.NodeExecutor;
import com.iusofts.agentplus.engine.executor.NodeExecutorRegistry;
import com.iusofts.agentplus.engine.util.ParamResolver;
import com.iusofts.agentplus.trace.TraceUtil;
import com.alibaba.fastjson2.JSON;
import io.opentelemetry.api.trace.SpanKind;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncEdgeAction;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;

/**
 * 将前端 {@link Workflow} 编译为 langgraph4j 的 {@link StateGraph}。
 *
 * <p>规则:</p>
 * <ul>
 *   <li>普通节点 → {@code addNode(id, nodeAction)},动作内部委托给对应 {@link NodeExecutor}。</li>
 *   <li>普通出边(单个 target) → {@code addEdge}。</li>
 *   <li>{@code Condition} 节点 → {@code addConditionalEdges},分支 key 来自
 *       {@code NodeOutput.chosenBranch} / {@code Edge.sourceHandle},未命中走 {@code else}。</li>
 *   <li>{@code Batch} 节点 → 主图内视为普通节点;其子图(node.parentNode == batchId)
 *       另行编译为独立 {@link StateGraph} 并注册到 {@link ExecutionContext#registerBatchSubGraph},
 *       由 {@code BatchNodeExecutor} 每次迭代直接调用。</li>
 *   <li>{@code Start} 节点作为入口,{@code addEdge(START, startNodeId)}。</li>
 *   <li>无出边的节点(含 End) → {@code addEdge(node, END)}。</li>
 * </ul>
 *
 * <p>批处理子图相关的四条约定边(见 {@link Handles})会被剔除,不再参与环检测——
 * langgraph4j 编译阶段本身也不接受环。</p>
 *
 * @author Ivan
 */
public class WorkflowGraphCompiler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowGraphCompiler.class);
    private static final String CONDITION_HANDLE_PREFIX = "condition:";
    private static final String ELSE_BRANCH = "else";

    private final NodeExecutorRegistry registry;

    public WorkflowGraphCompiler(NodeExecutorRegistry registry) {
        this.registry = registry;
    }

    /**
     * 编译结果。
     *
     * @param mainGraph      主图(已编译)
     * @param batchSubGraphs batchId → 该批处理节点对应的已编译子图
     * @param startNodeIds   主图入口节点(通常为 Start)
     * @param nodeIds        主图涉及的全部节点 id(用于最终状态汇总)
     * @param endNodeIds     所有 End 节点 id(用于结果聚合)
     */
    public record Compiled(CompiledGraph<WorkflowState> mainGraph,
                           Map<String, CompiledGraph<WorkflowState>> batchSubGraphs,
                           Set<String> startNodeIds,
                           Set<String> nodeIds,
                           Set<String> endNodeIds) {
    }

    public Compiled compile(Workflow workflow) {
        if (workflow == null || workflow.getNodes() == null || workflow.getNodes().isEmpty()) {
            throw new WorkflowExecutionException("workflow 或节点列表为空");
        }

        Map<String, Node> nodes = indexNodes(workflow);
        List<Edge> edges = workflow.getEdges() == null ? List.of() : workflow.getEdges();
        validateEdges(nodes, edges);

        Map<String, Set<String>> childrenOfBatch = groupBatchChildren(nodes);
        Set<String> batchInternalNodes = new LinkedHashSet<>();
        childrenOfBatch.values().forEach(batchInternalNodes::addAll);

        Map<String, CompiledGraph<WorkflowState>> subGraphs = new LinkedHashMap<>();
        for (Node n : nodes.values()) {
            if (!FlowNodeType.BATCH.getCode().equalsIgnoreCase(n.getType())) {
                continue;
            }
            Set<String> subNodes = childrenOfBatch.getOrDefault(n.getId(), Collections.emptySet());
            if (subNodes.isEmpty()) {
                continue;
            }
            subGraphs.put(n.getId(), compileBatchSubGraph(n, subNodes, nodes, edges));
        }

        Set<String> mainNodeIds = new LinkedHashSet<>();
        for (String id : nodes.keySet()) {
            if (!batchInternalNodes.contains(id)) {
                mainNodeIds.add(id);
            }
        }
        Set<String> startNodeIds = collectMainStartNodes(nodes, edges, batchInternalNodes);
        Set<String> endNodeIds = new LinkedHashSet<>();
        for (String id : mainNodeIds) {
            if (FlowNodeType.END.getCode().equalsIgnoreCase(nodes.get(id).getType())) {
                endNodeIds.add(id);
            }
        }

        CompiledGraph<WorkflowState> mainGraph = buildStateGraph(
                mainNodeIds, nodes, edges, startNodeIds, batchInternalNodes, true);
        return new Compiled(mainGraph, subGraphs, startNodeIds, mainNodeIds, endNodeIds);
    }

    private CompiledGraph<WorkflowState> compileBatchSubGraph(Node batchNode,
                                                              Set<String> subNodes,
                                                              Map<String, Node> nodes,
                                                              List<Edge> edges) {
        for (String childId : subNodes) {
            Node child = nodes.get(childId);
            if (child != null && FlowNodeType.BATCH.getCode().equalsIgnoreCase(child.getType())) {
                throw new WorkflowExecutionException(
                        "暂不支持嵌套批处理: " + batchNode.getId() + " 包含 " + childId);
            }
        }

        Set<String> entries = new LinkedHashSet<>();
        for (Edge e : edges) {
            if (Handles.BATCH_INTERNAL_SOURCE.equals(e.getSourceHandle())
                    && Objects.equals(e.getSource(), batchNode.getId())
                    && subNodes.contains(e.getTarget())) {
                entries.add(e.getTarget());
            }
        }
        if (entries.isEmpty()) {
            for (String id : subNodes) {
                boolean hasIncomingFromSibling = false;
                for (Edge e : edges) {
                    if (subNodes.contains(e.getSource()) && Objects.equals(e.getTarget(), id)) {
                        hasIncomingFromSibling = true;
                        break;
                    }
                }
                if (!hasIncomingFromSibling) {
                    entries.add(id);
                }
            }
        }

        return buildStateGraph(subNodes, nodes, edges, entries, Collections.emptySet(), false);
    }

    private CompiledGraph<WorkflowState> buildStateGraph(Set<String> includedNodes,
                                                         Map<String, Node> allNodes,
                                                         List<Edge> allEdges,
                                                         Set<String> entryNodeIds,
                                                         Set<String> batchInternalNodes,
                                                         boolean isMain) {
        if (includedNodes.isEmpty()) {
            throw new WorkflowExecutionException("待编译的节点集合为空");
        }
        if (entryNodeIds.isEmpty()) {
            throw new WorkflowExecutionException("找不到入口节点");
        }

        StateGraph<WorkflowState> graph = new StateGraph<>(WorkflowState::new);

        try {
            for (String id : includedNodes) {
                Node node = allNodes.get(id);
                graph.addNode(id, wrapExecutor(node));
            }

            for (String startId : entryNodeIds) {
                graph.addEdge(START, startId);
            }

            Map<String, List<Edge>> outgoingBySource = new LinkedHashMap<>();
            for (Edge e : allEdges) {
                if (!includedNodes.contains(e.getSource()) || !includedNodes.contains(e.getTarget())) {
                    continue;
                }
                if (isMain && !isMainScopeEdge(e, batchInternalNodes)) {
                    continue;
                }
                if (!isMain && Handles.BATCH_INTERNAL_TARGET.equals(e.getTargetHandle())) {
                    continue;
                }
                outgoingBySource.computeIfAbsent(e.getSource(), k -> new ArrayList<>()).add(e);
            }

            for (String id : includedNodes) {
                List<Edge> outgoing = outgoingBySource.getOrDefault(id, Collections.emptyList());
                Node node = allNodes.get(id);
                if (outgoing.isEmpty()) {
                    graph.addEdge(id, END);
                    continue;
                }
                if (FlowNodeType.CONDITION.getCode().equalsIgnoreCase(node.getType())) {
                    addConditionalRouting(graph, id, outgoing);
                    continue;
                }
                for (Edge e : outgoing) {
                    graph.addEdge(id, e.getTarget());
                }
            }

            return graph.compile();
        } catch (GraphStateException ex) {
            throw new WorkflowExecutionException("状态图编译失败: " + ex.getMessage(), ex);
        }
    }

    private void addConditionalRouting(StateGraph<WorkflowState> graph,
                                       String conditionNodeId,
                                       List<Edge> outgoing) throws GraphStateException {
        Map<String, String> mapping = new LinkedHashMap<>();
        for (Edge e : outgoing) {
            String key = canonicalBranchKey(e.getSourceHandle());
            if (key == null) {
                key = ELSE_BRANCH;
            }
            mapping.putIfAbsent(key, e.getTarget());
        }
        mapping.putIfAbsent(ELSE_BRANCH, END);

        AsyncEdgeAction<WorkflowState> router = state -> {
            NodeOutput out = state.ctx().getOutput(conditionNodeId);
            String chosen = out == null ? null : out.getChosenBranch();
            String key = mapping.containsKey(chosen) ? chosen : ELSE_BRANCH;
            return CompletableFuture.completedFuture(key);
        };
        graph.addConditionalEdges(conditionNodeId, router, mapping);
    }

    private AsyncNodeAction<WorkflowState> wrapExecutor(Node node) {
        NodeExecutor executor = registry.get(node.getType());
        return state -> {
            ExecutionContext ctx = state.ctx();
            ctx.updateStatus(node.getId(), NodeExecutionStatus.RUNNING);
            java.time.LocalDateTime startTime = java.time.LocalDateTime.now();
            try {
                LOGGER.debug("execute node id={} type={}", node.getId(), node.getType());
                // 使用 rootContext 作为父 context，确保所有节点都是平级的
                NodeOutput out = TraceUtil.span("node." + node.getId(), SpanKind.INTERNAL, ctx.getRootContext(), span -> {
                    span.setAttribute("nodeId", node.getId());
                    span.setAttribute("nodeType", node.getType());
                    // 取节点名称:优先 data.label,其次 node.label
                    String label = node.getData() == null ? null : node.getData().getLabel();
                    if (label == null || label.isBlank()) {
                        label = node.getLabel();
                    }
                    span.setAttribute("label", label);

                    // 入参载荷：已解析的实际入参值（仅 InputParamNodeData 子类有入参）
                    if (node.getData() instanceof InputParamNodeData inputParamNode) {
                        java.util.List<com.iusofts.agentplus.aiflow.vo.workflow.data.common.InputParam> params = inputParamNode.getInputParams();
                        if (params != null && !params.isEmpty()) {
                            java.util.Map<String, Object> resolved = ParamResolver.resolveInputs(params, ctx);
                            span.setAttribute("ap.payload.input", JSON.toJSONString(resolved));
                        }
                    }

                    NodeOutput result = executor.execute(node, ctx);

                    // 出参载荷：节点输出结果
                    if (result != null && result.getOutputs() != null && !result.getOutputs().isEmpty()) {
                        span.setAttribute("ap.payload.output", JSON.toJSONString(result.getOutputs()));
                    }

                    span.setAttribute("nodeStatus", "SUCCESS");
                    return result;
                });
                ctx.putOutput(out);
                ctx.updateStatus(node.getId(), NodeExecutionStatus.SUCCESS);
            } catch (WorkflowExecutionException e) {
                ctx.updateStatus(node.getId(), NodeExecutionStatus.FAILED);
                throw e;
            } catch (Exception e) {
                ctx.updateStatus(node.getId(), NodeExecutionStatus.FAILED);
                throw new WorkflowExecutionException(node.getId(), "节点执行异常", e);
            } finally {
                ctx.recordTiming(node.getId(), startTime, java.time.LocalDateTime.now());
            }
            return CompletableFuture.completedFuture(Collections.emptyMap());
        };
    }

    private String canonicalBranchKey(String sourceHandle) {
        if (sourceHandle == null || sourceHandle.isEmpty()) {
            return null;
        }
        if (sourceHandle.startsWith(CONDITION_HANDLE_PREFIX)) {
            return sourceHandle.substring(CONDITION_HANDLE_PREFIX.length());
        }
        return sourceHandle;
    }

    private boolean isMainScopeEdge(Edge edge, Set<String> batchInternalNodes) {
        if (Handles.BATCH_INTERNAL_TARGET.equals(edge.getTargetHandle())) {
            return false;
        }
        if (Handles.BATCH_INTERNAL_SOURCE.equals(edge.getSourceHandle())) {
            return false;
        }
        return !batchInternalNodes.contains(edge.getSource())
                && !batchInternalNodes.contains(edge.getTarget());
    }

    private Set<String> collectMainStartNodes(Map<String, Node> nodes,
                                              List<Edge> edges,
                                              Set<String> batchInternalNodes) {
        Map<String, Boolean> hasMainIncoming = new HashMap<>();
        for (String id : nodes.keySet()) {
            hasMainIncoming.put(id, false);
        }
        for (Edge e : edges) {
            if (!isMainScopeEdge(e, batchInternalNodes)) {
                continue;
            }
            if (nodes.containsKey(e.getTarget())) {
                hasMainIncoming.put(e.getTarget(), true);
            }
        }
        Set<String> starts = new LinkedHashSet<>();
        for (Node n : nodes.values()) {
            if (batchInternalNodes.contains(n.getId())) {
                continue;
            }
            boolean isStart = FlowNodeType.START.getCode().equalsIgnoreCase(n.getType());
            if (isStart || !Boolean.TRUE.equals(hasMainIncoming.get(n.getId()))) {
                starts.add(n.getId());
            }
        }
        if (starts.isEmpty()) {
            throw new WorkflowExecutionException("找不到 Start 节点");
        }
        return starts;
    }

    private Map<String, Node> indexNodes(Workflow workflow) {
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
        return nodes;
    }

    private void validateEdges(Map<String, Node> nodes, List<Edge> edges) {
        for (Edge edge : edges) {
            if (edge.getSource() == null || edge.getTarget() == null) {
                throw new WorkflowExecutionException("边缺失 source/target: " + edge.getId());
            }
            if (!nodes.containsKey(edge.getSource()) || !nodes.containsKey(edge.getTarget())) {
                throw new WorkflowExecutionException("边指向未知节点: " + edge.getId());
            }
        }
    }

    private Map<String, Set<String>> groupBatchChildren(Map<String, Node> nodes) {
        Map<String, Set<String>> childrenOf = new HashMap<>();
        for (Node n : nodes.values()) {
            if (n.getParentNode() != null && !n.getParentNode().isEmpty()) {
                childrenOf.computeIfAbsent(n.getParentNode(), k -> new LinkedHashSet<>()).add(n.getId());
            }
        }
        return childrenOf;
    }

    /** 批处理节点约定的边 handle。 */
    public static final class Handles {
        public static final String BATCH_INTERNAL_SOURCE = "batch-internal-source";
        public static final String BATCH_INTERNAL_TARGET = "batch-internal-target";
        public static final String BATCH_EXTERNAL_SOURCE = "batch-external-source";
        public static final String BATCH_EXTERNAL_TARGET = "batch-external-target";

        private Handles() {
        }
    }
}
