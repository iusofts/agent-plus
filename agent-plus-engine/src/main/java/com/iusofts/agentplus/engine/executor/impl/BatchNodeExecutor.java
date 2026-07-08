package com.iusofts.agentplus.engine.executor.impl;

import com.iusofts.agentplus.aiflow.enums.FlowNodeType;
import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.data.BatchNodeData;
import com.iusofts.agentplus.engine.context.ExecutionContext;
import com.iusofts.agentplus.engine.context.NodeOutput;
import com.iusofts.agentplus.engine.executor.NodeExecutor;
import com.iusofts.agentplus.engine.graph.ExecutionContextTracker;
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
            return finalOutput(node.getId(), items, results, 0, data);
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

        return finalOutput(node.getId(), items, results, success.get(), data);
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

        LOGGER.debug("batch iteration starting batchId={} index={} item={} inputs={}", batchId, index, item, resolvedInputs);

        // 为 scoped ctx 创建 tracker，避免 langgraph4j 克隆问题
        ExecutionContextTracker tracker = new ExecutionContextTracker(scoped);
        try {
            subGraph.invoke(Map.of(WorkflowState.CTX_KEY, tracker));
        } catch (Exception e) {
            LOGGER.warn("batch iteration failed batchId={} index={} err={}", batchId, index, e.getMessage(), e);
            return null;
        } finally {
            // 精确清理该 scope 的 tracker（不能用前缀，否则并行时会误删 index 互为前缀的其他轮次）
            ExecutionContextTracker.removeKey(
                    ExecutionContextTracker.keyOf(scoped.getRunId(), scoped.getScopeKey()));
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
        LOGGER.debug("batch iteration collected batchId={} index={} outputs={}", batchId, index, collected);
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
        // 无张子图时，默认输出 "output" = items
        outputs.put("output", items);
        outputs.put("total", items.size());
        outputs.put("success", 0);
        outputs.put("failed", 0);
        return new NodeOutput(nodeId, outputs);
    }

    private NodeOutput finalOutput(String nodeId,
                                   List<Object> items,
                                   List<Map<String, Map<String, Object>>> results,
                                   int success,
                                   BatchNodeData data) {
        Map<String, Object> outputs = new LinkedHashMap<>();
        List<com.iusofts.agentplus.aiflow.vo.workflow.data.common.OutputParam> outputParams =
                data != null ? data.getOutputParams() : null;

        if (outputParams != null && !outputParams.isEmpty()) {
            // 有自定义输出参数时，按参数名聚合每轮结果
            LOGGER.debug("batch node={} outputParams={}, results={}", nodeId, outputParams, results);
            for (com.iusofts.agentplus.aiflow.vo.workflow.data.common.OutputParam param : outputParams) {
                String paramName = param.getName();
                if (paramName == null || paramName.isEmpty()) {
                    continue;
                }
                List<Object> paramResults = new ArrayList<>();
                for (int i = 0; i < results.size(); i++) {
                    Map<String, Map<String, Object>> round = results.get(i);
                    if (round == null) {
                        paramResults.add(null);
                        continue;
                    }
                    // 从本轮结果中查找该参数
                    Object value = null;
                    if (param.getParamMapKey() != null) {
                        String sourceNodeId = param.getParamMapKey().getNodeId();
                        String sourceParamName = param.getParamMapKey().getName();
                        LOGGER.debug("looking for node={} param={} in round={} data={}",
                                sourceNodeId, sourceParamName, i, round);
                        if (sourceNodeId != null && round.containsKey(sourceNodeId)) {
                            Map<String, Object> nodeOut = round.get(sourceNodeId);
                            if (nodeOut != null) {
                                value = nodeOut.get(sourceParamName);
                            }
                        }
                    }
                    LOGGER.debug("round={} value={}", i, value);
                    paramResults.add(value);
                }
                outputs.put(paramName, paramResults);
            }
        } else {
            // 无自定义输出时，使用默认 "output" 数组
            outputs.put("output", results);
        }

        // 始终包含统计信息（补充字段，非约定但有用）
        outputs.put("total", items.size());
        outputs.put("success", success);
        outputs.put("failed", items.size() - success);
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
