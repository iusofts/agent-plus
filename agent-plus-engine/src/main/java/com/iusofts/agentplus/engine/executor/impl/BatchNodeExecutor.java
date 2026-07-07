package com.iusofts.agentplus.engine.executor.impl;

import com.iusofts.agentplus.aiflow.enums.FlowNodeType;
import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.data.BatchNodeData;
import com.iusofts.agentplus.engine.context.ExecutionContext;
import com.iusofts.agentplus.engine.context.NodeOutput;
import com.iusofts.agentplus.engine.executor.NodeExecutor;
import com.iusofts.agentplus.engine.graph.WorkflowState;
import com.iusofts.agentplus.engine.util.ParamResolver;
import org.bsc.langgraph4j.CompiledGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 批处理节点执行器。
 *
 * <p>由 {@code WorkflowGraphCompiler} 为每个 batch 节点预编译一份子图 {@link CompiledGraph},
 * 并注册到 {@link ExecutionContext}。本执行器每次迭代:</p>
 * <ol>
 *   <li>用 {@link ExecutionContext#newScope(String)} 生成隔离作用域,写入 {@code item/index/items};</li>
 *   <li>通过共享同一份 {@link CompiledGraph} 的 {@code invoke} 驱动子图迭代;</li>
 *   <li>抓取作用域内所有 sub-node 的 outputs 组合为一条 result;</li>
 * </ol>
 *
 * <p>并行由 {@code Executors.newFixedThreadPool(maxParallel)} 拉起,失败位置在 {@code results}
 * 中为 {@code null},不影响其他 iteration。若 batch 未挂子节点则退化为仅输出 {@code items/size}。</p>
 *
 * @author Ivan
 */
public class BatchNodeExecutor implements NodeExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchNodeExecutor.class);
    private static final int DEFAULT_PARALLEL = 4;

    @Override
    public FlowNodeType type() {
        return FlowNodeType.BATCH;
    }

    @Override
    public NodeOutput execute(Node node, ExecutionContext ctx) {
        BatchNodeData data = (BatchNodeData) node.getData();
        Map<String, Object> inputs = ParamResolver.resolveInputs(data == null ? null : data.getInputParams(), ctx);
        List<Object> items = extractCollection(inputs);

        @SuppressWarnings("unchecked")
        CompiledGraph<WorkflowState> subGraph = (CompiledGraph<WorkflowState>) ctx.getBatchSubGraph(node.getId());
        if (subGraph == null) {
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

        ExecutorService pool = Executors.newFixedThreadPool(parallel);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>(items.size());
            for (int i = 0; i < items.size(); i++) {
                final int index = i;
                final Object item = items.get(i);
                futures.add(CompletableFuture.runAsync(() -> {
                    Map<String, Map<String, Object>> iterationResult = runIteration(
                            node.getId(), item, index, items, inputs, subGraph, ctx);
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
                                                          CompiledGraph<WorkflowState> subGraph,
                                                          ExecutionContext parentCtx) {
        ExecutionContext scoped = parentCtx.newScope(batchId + "#" + index);
        Map<String, Object> batchLocal = new LinkedHashMap<>();
        batchLocal.put("item", item);
        batchLocal.put("index", index);
        batchLocal.put("items", items);
        batchLocal.putAll(resolvedInputs);
        scoped.putOutput(new NodeOutput(batchId, batchLocal));

        try {
            subGraph.invoke(Map.of(WorkflowState.CTX_KEY, scoped));
        } catch (Exception e) {
            LOGGER.warn("batch iteration failed batchId={} index={} err={}", batchId, index, e.getMessage());
            return null;
        }

        Map<String, Map<String, Object>> collected = new LinkedHashMap<>();
        for (Map.Entry<String, NodeOutput> entry : scoped.getNodeOutputs().entrySet()) {
            if (entry.getKey().equals(batchId)) {
                continue;
            }
            Map<String, Object> outs = entry.getValue().getOutputs();
            if (outs != null && !outs.isEmpty()) {
                collected.put(entry.getKey(), new LinkedHashMap<>(outs));
            }
        }
        return collected;
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
