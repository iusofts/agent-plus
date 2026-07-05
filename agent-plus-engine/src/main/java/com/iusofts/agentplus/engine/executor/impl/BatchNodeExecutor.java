package com.iusofts.agentplus.engine.executor.impl;

import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.data.BatchNodeData;
import com.iusofts.agentplus.aiflow.vo.workflow.data.common.OutputParam;
import com.iusofts.agentplus.engine.context.ExecutionContext;
import com.iusofts.agentplus.engine.context.NodeOutput;
import com.iusofts.agentplus.engine.executor.NodeExecutor;
import com.iusofts.agentplus.engine.util.ParamResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 批处理节点执行器(轻量实现)。
 *
 * <p>解析第一个 inputParam 得到集合,输出:</p>
 * <ul>
 *   <li>{@code items} - 完整集合,供下游 Aggregator 或 LLM 循环消费</li>
 *   <li>{@code size}  - 集合大小</li>
 *   <li>outputParams 首元素 - 与 items 同值,方便自定义命名</li>
 * </ul>
 *
 * <p>对于真正的"子图逐项迭代"能力,可通过实现 {@code parentNode==batchId} 的子节点
 * 并复用 {@link com.iusofts.agentplus.engine.WorkflowEngine} 手动 fan-out;
 * 引擎当前版本聚焦主图 DAG 执行,不深入子图循环。</p>
 *
 * @author Ivan
 */
public class BatchNodeExecutor implements NodeExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchNodeExecutor.class);

    @Override
    public String type() {
        return "Batch";
    }

    @Override
    public NodeOutput execute(Node node, ExecutionContext ctx) {
        BatchNodeData data = (BatchNodeData) node.getData();
        Map<String, Object> inputs = ParamResolver.resolveInputs(data == null ? null : data.getInputParams(), ctx);
        List<Object> items = extractCollection(inputs);
        LOGGER.debug("batch node={} size={} maxParallel={}", node.getId(), items.size(),
                data == null ? null : data.getMaxParallel());

        Map<String, Object> outputs = new LinkedHashMap<>();
        outputs.put("items", items);
        outputs.put("size", items.size());
        if (data != null && data.getOutputParams() != null && !data.getOutputParams().isEmpty()) {
            OutputParam p = data.getOutputParams().get(0);
            if (p.getName() != null) {
                outputs.put(p.getName(), items);
            }
        }
        return new NodeOutput(node.getId(), outputs);
    }

    @SuppressWarnings("unchecked")
    private List<Object> extractCollection(Map<String, Object> inputs) {
        if (inputs.isEmpty()) {
            return List.of();
        }
        Object first = inputs.values().iterator().next();
        if (first == null) {
            return List.of();
        }
        if (first instanceof Collection<?> c) {
            return new ArrayList<>(c);
        }
        if (first.getClass().isArray()) {
            Object[] arr = (Object[]) first;
            return new ArrayList<>(List.of(arr));
        }
        return List.of(first);
    }
}
