package com.iusofts.agentplus.engine.executor.impl;

import com.iusofts.agentplus.aiflow.enums.FlowNodeType;
import com.iusofts.agentplus.aiflow.vo.workflow.Edge;
import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.data.BatchNodeData;
import com.iusofts.agentplus.engine.context.ExecutionContext;
import com.iusofts.agentplus.engine.context.NodeOutput;
import com.iusofts.agentplus.engine.dag.DagGraph;
import com.iusofts.agentplus.engine.dag.TopologicalScheduler;
import com.iusofts.agentplus.engine.executor.NodeExecutor;
import com.iusofts.agentplus.engine.executor.NodeExecutorRegistry;
import com.iusofts.agentplus.engine.util.ParamResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 批处理节点执行器。
 *
 * <p>接管 {@code parentNode == batchId} 的子节点组成的子图,按 {@code maxParallel}
 * 对每个 item 并行跑一遍子图,聚合默认输出:</p>
 * <ul>
 *   <li>{@code results} - List,每项是本轮迭代内所有子节点 outputs 的 {@code Map<nodeId, outputs>};失败位为 null</li>
 *   <li>{@code total}   - items 数量</li>
 *   <li>{@code success} - 成功迭代数</li>
 *   <li>{@code failed}  - total - success</li>
 * </ul>
 *
 * <p>子作用域内,batch 节点自身的输出会被覆盖为 {@code {item, index, items}},
 * 供子图节点通过 {@code {{<batchId>.item}}} 引用当前项。</p>
 *
 * <p>若子图为空(未挂子节点)则退化为旧行为,仅输出 {@code items/size} 便于下游消费集合。</p>
 *
 * @author Ivan
 */
public class BatchNodeExecutor implements NodeExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchNodeExecutor.class);
    private static final int DEFAULT_PARALLEL = 4;

    private final NodeExecutorRegistry registry;

    public BatchNodeExecutor(NodeExecutorRegistry registry) {
        this.registry = registry;
    }

    @Override
    public FlowNodeType type() {
        return FlowNodeType.BATCH;
    }

    @Override
    public NodeOutput execute(Node node, ExecutionContext ctx) {
        BatchNodeData data = (BatchNodeData) node.getData();
        Map<String, Object> inputs = ParamResolver.resolveInputs(data == null ? null : data.getInputParams(), ctx);
        List<Object> items = extractCollection(inputs);

        DagGraph mainGraph = ctx.getGraph();
        DagGraph.BatchSubgraph sub = mainGraph == null ? null : mainGraph.subgraphOf(node.getId());

        if (sub == null || sub.subNodes().isEmpty()) {
            LOGGER.debug("batch node={} 无子图,退化为集合输出 size={}", node.getId(), items.size());
            return degradedOutput(node.getId(), items);
        }

        int parallel = resolveParallel(data);
        LOGGER.debug("batch node={} size={} parallel={}", node.getId(), items.size(), parallel);

        List<Map<String, Map<String, Object>>> results = new ArrayList<>(Collections.nCopies(items.size(), null));
        AtomicInteger success = new AtomicInteger();

        if (items.isEmpty()) {
            return finalOutput(node.getId(), items, results, 0);
        }

        DagGraph subGraph = buildSubGraph(mainGraph, sub);

        ExecutorService pool = Executors.newFixedThreadPool(parallel);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>(items.size());
            for (int i = 0; i < items.size(); i++) {
                final int index = i;
                final Object item = items.get(i);
                futures.add(CompletableFuture.runAsync(() -> {
                    Map<String, Map<String, Object>> iterationResult = runIteration(
                            node.getId(), item, index, items, inputs, subGraph, ctx, sub);
                    if (iterationResult != null) {
                        results.set(index, iterationResult);
                        success.incrementAndGet();
                    }
                }, pool));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            pool.shutdown();
        }

        return finalOutput(node.getId(), items, results, success.get());
    }

    private Map<String, Map<String, Object>> runIteration(String batchId,
                                                          Object item,
                                                          int index,
                                                          List<Object> items,
                                                          Map<String, Object> resolvedInputs,
                                                          DagGraph subGraph,
                                                          ExecutionContext parentCtx,
                                                          DagGraph.BatchSubgraph sub) {
        ExecutionContext scoped = parentCtx.newScope(batchId + "#" + index);
        Map<String, Object> batchLocal = new LinkedHashMap<>();
        batchLocal.put("item", item);
        batchLocal.put("index", index);
        batchLocal.put("items", items);
        batchLocal.putAll(resolvedInputs);
        scoped.putOutput(new NodeOutput(batchId, batchLocal));

        try {
            new TopologicalScheduler(subGraph, registry).run(scoped);
        } catch (Exception e) {
            LOGGER.warn("batch iteration failed batchId={} index={} err={}", batchId, index, e.getMessage());
            return null;
        }

        Map<String, Map<String, Object>> collected = new LinkedHashMap<>();
        for (String subNodeId : sub.subNodes()) {
            NodeOutput out = scoped.getOutput(subNodeId);
            if (out != null && out.getOutputs() != null && !out.getOutputs().isEmpty()) {
                collected.put(subNodeId, new LinkedHashMap<>(out.getOutputs()));
            }
        }
        return collected;
    }

    private DagGraph buildSubGraph(DagGraph mainGraph, DagGraph.BatchSubgraph sub) {
        Set<String> subNodes = sub.subNodes();
        Map<String, Node> subNodesMap = new LinkedHashMap<>();
        for (String id : subNodes) {
            subNodesMap.put(id, mainGraph.node(id));
        }
        Map<String, List<Edge>> outgoing = new LinkedHashMap<>();
        Map<String, List<Edge>> incoming = new LinkedHashMap<>();
        for (String id : subNodes) {
            for (Edge e : mainGraph.outgoingOf(id)) {
                if (subNodes.contains(e.getTarget())) {
                    outgoing.computeIfAbsent(id, k -> new ArrayList<>()).add(e);
                }
            }
            for (Edge e : mainGraph.incomingOf(id)) {
                if (subNodes.contains(e.getSource())) {
                    incoming.computeIfAbsent(id, k -> new ArrayList<>()).add(e);
                }
            }
        }
        Set<String> starts = sub.entryTargets().isEmpty()
                ? findRoots(subNodes, incoming)
                : new LinkedHashSet<>(sub.entryTargets());
        return new DagGraph(subNodesMap, outgoing, incoming, starts, Collections.emptyMap(), Collections.emptySet());
    }

    private Set<String> findRoots(Set<String> subNodes, Map<String, List<Edge>> incoming) {
        Set<String> roots = new LinkedHashSet<>();
        for (String id : subNodes) {
            if (incoming.getOrDefault(id, List.of()).isEmpty()) {
                roots.add(id);
            }
        }
        return roots;
    }

    private int resolveParallel(BatchNodeData data) {
        if (data == null || data.getMaxParallel() == null || data.getMaxParallel() <= 0) {
            return DEFAULT_PARALLEL;
        }
        return data.getMaxParallel();
    }

    private NodeOutput degradedOutput(String nodeId, List<Object> items) {
        Map<String, Object> outputs = new LinkedHashMap<>();
        outputs.put("items", items);
        outputs.put("size", items.size());
        outputs.put("results", Collections.emptyList());
        outputs.put("total", items.size());
        outputs.put("success", 0);
        outputs.put("failed", 0);
        return new NodeOutput(nodeId, outputs);
    }

    private NodeOutput finalOutput(String nodeId,
                                   List<Object> items,
                                   List<Map<String, Map<String, Object>>> results,
                                   int success) {
        int total = items.size();
        Map<String, Object> outputs = new LinkedHashMap<>();
        outputs.put("results", results);
        outputs.put("total", total);
        outputs.put("success", success);
        outputs.put("failed", total - success);
        return new NodeOutput(nodeId, outputs);
    }

    private List<Object> extractCollection(Map<String, Object> inputs) {
        if (inputs.isEmpty()) {
            return new ArrayList<>();
        }
        Object first = inputs.values().iterator().next();
        if (first == null) {
            return new ArrayList<>();
        }
        if (first instanceof Collection<?> c) {
            return new ArrayList<>(c);
        }
        if (first.getClass().isArray()) {
            Object[] arr = (Object[]) first;
            List<Object> list = new ArrayList<>(arr.length);
            Collections.addAll(list, arr);
            return list;
        }
        List<Object> list = new ArrayList<>(1);
        list.add(first);
        return list;
    }
}
