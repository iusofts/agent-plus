package com.iusofts.agentplus.engine.executor.impl;

import com.iusofts.agentplus.aiflow.enums.FlowNodeType;
import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.data.EndNodeData;
import com.iusofts.agentplus.aiflow.vo.workflow.data.common.OutputParam;
import com.iusofts.agentplus.engine.context.ExecutionContext;
import com.iusofts.agentplus.engine.context.NodeOutput;
import com.iusofts.agentplus.engine.executor.NodeExecutor;
import com.iusofts.agentplus.engine.util.ParamResolver;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * End 节点执行器。将 outputParams 从上游节点采集,组装为最终结果。
 *
 * @author Ivan
 */
public class EndNodeExecutor implements NodeExecutor {

    @Override
    public FlowNodeType type() {
        return FlowNodeType.END;
    }

    @Override
    public NodeOutput execute(Node node, ExecutionContext ctx) {
        Map<String, Object> outputs = new LinkedHashMap<>();
        EndNodeData data = (EndNodeData) node.getData();
        if (data != null && data.getOutputParams() != null) {
            for (OutputParam p : data.getOutputParams()) {
                outputs.put(p.getName(), ParamResolver.resolve(p.getParamMapKey(), ctx));
            }
        }
        return new NodeOutput(node.getId(), outputs);
    }
}
