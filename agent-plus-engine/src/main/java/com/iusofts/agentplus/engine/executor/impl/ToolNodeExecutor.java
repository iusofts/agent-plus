package com.iusofts.agentplus.engine.executor.impl;

import com.alibaba.fastjson2.JSON;
import com.iusofts.agentplus.aiflow.enums.FlowNodeType;
import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.data.ToolNodeData;
import com.iusofts.agentplus.aiflow.vo.workflow.data.common.OutputParam;
import com.iusofts.agentplus.engine.context.ExecutionContext;
import com.iusofts.agentplus.engine.context.NodeOutput;
import com.iusofts.agentplus.engine.exception.WorkflowExecutionException;
import com.iusofts.agentplus.engine.executor.NodeExecutor;
import com.iusofts.agentplus.engine.tool.ToolRegistry;
import com.iusofts.agentplus.engine.util.ParamResolver;
import com.iusofts.agentplus.tool.ToolQueryProvider;
import com.iusofts.agentplus.tool.dto.ToolExecuteRequest;
import com.iusofts.agentplus.tool.dto.ToolExecuteResult;
import com.iusofts.agentplus.trace.TraceUtil;
import com.iusofts.agentplus.trace.constants.CallSource;

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

        ToolExecuteRequest request = ToolExecuteRequest.builder()
            .toolId(data.getToolId())
            .params(inputs)
            .build();

        // 设置业务属性到 Span Attributes
        TraceUtil.setAiAttributes(CallSource.FLOW, ctx.getFlowId(), node.getId(),
            ctx.getOperatorId(), ctx.getOrgId());

        ToolExecuteResult result = toolRegistry.execute(request);

        // FIXME 根据错误处理方式 选择重试、中断、返回预设内容
        
        // 工具执行失败：抛出异常，由引擎标记节点为 FAILED，Span 状态置为 ERROR
        if (!result.isSuccess()) {
            throw new WorkflowExecutionException(node.getId(),
                "工具执行失败: " + result.getErrorMessage());
        }

        Map<String, Object> outputs = new LinkedHashMap<>();
        if(result.getData() != null) {
            outputs = JSON.parseObject(JSON.toJSONString(result));
        }
        return new NodeOutput(node.getId(), outputs);
    }
}
