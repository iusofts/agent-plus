package com.iusofts.agentplus.engine.executor.impl;

import com.iusofts.agentplus.aiflow.enums.FlowNodeType;
import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.data.AggregatorNodeData;
import com.iusofts.agentplus.aiflow.vo.workflow.data.aggregator.OutputGroup;
import com.iusofts.agentplus.aiflow.vo.workflow.data.common.ParamMapKey;
import com.iusofts.agentplus.engine.context.ExecutionContext;
import com.iusofts.agentplus.engine.context.NodeOutput;
import com.iusofts.agentplus.engine.executor.NodeExecutor;
import com.iusofts.agentplus.engine.util.ParamResolver;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 变量聚合节点执行器。
 *
 * <p>按 outputGroup 汇聚多个上游变量:</p>
 * <ul>
 *   <li>group.type == list  -> 合并为 List,忽略 null。</li>
 *   <li>group.type == first -> 取首个非 null 变量。</li>
 *   <li>其余 -> Map,变量名 -> 值。</li>
 * </ul>
 *
 * @author Ivan
 */
public class AggregatorNodeExecutor implements NodeExecutor {

    @Override
    public FlowNodeType type() {
        return FlowNodeType.AGGREGATOR;
    }

    @Override
    public NodeOutput execute(Node node, ExecutionContext ctx) {
        AggregatorNodeData data = (AggregatorNodeData) node.getData();
        Map<String, Object> outputs = new LinkedHashMap<>();
        if (data == null || data.getOutputGroups() == null) {
            return new NodeOutput(node.getId(), outputs);
        }
        for (OutputGroup group : data.getOutputGroups()) {
            outputs.put(group.getName(), aggregate(group, ctx));
        }
        return new NodeOutput(node.getId(), outputs);
    }

    private Object aggregate(OutputGroup group, ExecutionContext ctx) {
        List<ParamMapKey> vars = group.getVariables();
        if (vars == null || vars.isEmpty()) {
            return null;
        }
        String t = group.getType() == null ? "list" : group.getType().toLowerCase();
        return switch (t) {
            case "first" -> vars.stream()
                    .map(k -> ParamResolver.resolve(k, ctx))
                    .filter(v -> v != null)
                    .findFirst()
                    .orElse(null);
            case "map", "object" -> {
                Map<String, Object> m = new LinkedHashMap<>();
                for (ParamMapKey k : vars) {
                    m.put(k.getName(), ParamResolver.resolve(k, ctx));
                }
                yield m;
            }
            default -> {
                List<Object> list = new ArrayList<>();
                for (ParamMapKey k : vars) {
                    Object v = ParamResolver.resolve(k, ctx);
                    if (v != null) {
                        list.add(v);
                    }
                }
                yield list;
            }
        };
    }
}
