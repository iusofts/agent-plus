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
import com.iusofts.agentplus.trace.TraceUtil;
import io.opentelemetry.api.trace.SpanKind;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 工具节点执行器.
 *
 * @author Ivan
 */
public class ToolNodeExecutor implements NodeExecutor {

    private final ToolRegistry toolRegistry;
    private final ToolQueryProvider toolQueryProvider;

    public ToolNodeExecutor(ToolRegistry toolRegistry, ToolQueryProvider toolQueryProvider) {
        this.toolRegistry = toolRegistry;
        this.toolQueryProvider = toolQueryProvider;
    }

    @Override
    public FlowNodeType type() {
        return FlowNodeType.TOOL;
    }

    @Override
    public NodeOutput execute(Node node, ExecutionContext ctx) {
        ToolNodeData data = (ToolNodeData) node.getData();
        Map<String, Object> inputs = ParamResolver.resolveInputs(data.getInputParams(), ctx);

        final ToolExecuteRequest request = ToolExecuteRequest.builder()
            .toolId(data.getToolId())
            .params(inputs)
            .build();

        // 获取工具名称用于 span 名称
        String toolName = "tool";
        if (toolQueryProvider != null && data.getToolId() != null) {
            var toolDTO = toolQueryProvider.getById(data.getToolId());
            if (toolDTO != null) {
                toolName = toolDTO.getName();
            }
        }

        final Long finalToolId = data.getToolId();
        final String finalToolName = toolName;
        final Node finalNode = node;
        final ExecutionContext finalCtx = ctx;
        // 创建 span 包装工具执行
        ToolExecuteResult result = TraceUtil.span("tool." + toolName, SpanKind.INTERNAL, span -> {
            // 设置业务属性到 Span Attributes
            TraceUtil.setAiAttributes("FLOW", finalCtx.getFlowId(), finalNode.getId(),
                finalCtx.getOperatorId(), finalCtx.getOrgId());
            TraceUtil.setLabel(finalToolName);
            // 设置工具相关属性
            span.setAttribute("nodeType", "tool");
            if (finalToolId != null) {
                span.setAttribute("ai.tool_id", finalToolId);
            }
            return toolRegistry.execute(request);
        });

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
