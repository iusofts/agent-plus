package com.iusofts.agentplus.engine.executor.impl;

import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.data.StartNodeData;
import com.iusofts.agentplus.aiflow.vo.workflow.data.common.Param;
import com.iusofts.agentplus.engine.context.ExecutionContext;
import com.iusofts.agentplus.engine.context.NodeOutput;
import com.iusofts.agentplus.engine.executor.NodeExecutor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Start 节点执行器。
 *
 * <p>把全局输入按 {@code inputParams} 声明的顺序装配为节点输出,
 * 允许下游通过 {@code startNodeId} 或 {@code inputs} 别名引用。
 * 未提供的必填参数将回退到 {@code defaultValue}。</p>
 *
 * @author Ivan
 */
public class StartNodeExecutor implements NodeExecutor {

    @Override
    public String type() {
        return "Start";
    }

    @Override
    public NodeOutput execute(Node node, ExecutionContext ctx) {
        Map<String, Object> outputs = new LinkedHashMap<>();
        StartNodeData data = (StartNodeData) node.getData();
        if (data != null && data.getInputParams() != null) {
            for (Param p : data.getInputParams()) {
                Object v = ctx.getGlobalInputs().get(p.getName());
                if (v == null) {
                    v = p.getDefaultValue();
                }
                outputs.put(p.getName(), v);
            }
        }
        return new NodeOutput(node.getId(), outputs);
    }
}
