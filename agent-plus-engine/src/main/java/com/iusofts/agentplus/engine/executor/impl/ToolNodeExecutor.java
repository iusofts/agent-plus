package com.iusofts.agentplus.engine.executor.impl;

import com.iusofts.agentplus.aiflow.enums.FlowNodeType;
import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.data.ToolNodeData;
import com.iusofts.agentplus.aiflow.vo.workflow.data.common.OutputParam;
import com.iusofts.agentplus.engine.context.ExecutionContext;
import com.iusofts.agentplus.engine.context.NodeOutput;
import com.iusofts.agentplus.engine.executor.NodeExecutor;
import com.iusofts.agentplus.engine.tool.ToolRegistry;
import com.iusofts.agentplus.engine.util.ParamResolver;
import com.iusofts.agentplus.tool.ToolQueryProvider;
import com.iusofts.agentplus.tool.dto.ToolExecuteRequest;
import com.iusofts.agentplus.tool.dto.ToolExecuteResult;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 工具节点执行器.
 *
 * @author Ivan
 */
public class ToolNodeExecutor implements NodeExecutor {

    private final ToolRegistry toolRegistry;

    public ToolNodeExecutor(ToolRegistry toolRegistry, ToolQueryProvider toolQueryProvider) {
        this.toolRegistry = toolRegistry;
    }

    @Override
    public FlowNodeType type() {
        return FlowNodeType.TOOL;
    }

    @Override
    public NodeOutput execute(Node node, ExecutionContext ctx) {
        ToolNodeData data = (ToolNodeData) node.getData();
        Map<String, Object> inputs = ParamResolver.resolveInputs(data.getInputParams(), ctx);

        ToolExecuteRequest request = ToolExecuteRequest.builder()
            .toolId(data.getToolId())
            .params(inputs)
            .build();

        ToolExecuteResult result = toolRegistry.execute(request);

        Map<String, Object> outputs = new LinkedHashMap<>();
        String outName = "result";
        if (data.getOutputParams() != null && !data.getOutputParams().isEmpty()) {
            OutputParam p = data.getOutputParams().get(0);
            if (p.getName() != null) {
                outName = p.getName();
            }
        }
        outputs.put(outName, result);
        return new NodeOutput(node.getId(), outputs);
    }
}
