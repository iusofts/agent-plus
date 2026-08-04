package com.iusofts.agentplus.engine.executor.impl;

import com.iusofts.agentplus.aiflow.enums.FlowNodeType;
import com.iusofts.agentplus.aiflow.stream.LLMTokenEvent;
import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.data.EndNodeData;
import com.iusofts.agentplus.aiflow.vo.workflow.data.common.OutputParam;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.engine.context.ExecutionContext;
import com.iusofts.agentplus.engine.context.NodeOutput;
import com.iusofts.agentplus.engine.executor.NodeExecutor;
import com.iusofts.agentplus.engine.util.ParamResolver;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * End 节点执行器。将 outputParams 从上游节点采集,组装为最终结果。
 *
 * <p>answerMode=text 时,若 {@code streamOutput=true} 且当前为流式执行,
 * 会把渲染后的 text 通过 {@link LLMTokenEvent} 一次性推给客户端(nodeType=End 与 Output 节点区分),
 * 便于前端在 WorkflowCompleteEvent 之前实时看到最终消息。
 * 关闭 {@code streamOutput} 时仅在 {@code finalOutput.text} 中体现。</p>
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
        String renderedText = null;
        if (data != null && data.getOutputParams() != null) {
            // 校验：answerMode=text 时 outputParams 中不能有 name=text 的字段
            if ("text".equals(data.getAnswerMode())) {
                for (OutputParam p : data.getOutputParams()) {
                    if ("text".equals(p.getName())) {
                        throw new SystemBusinessException("结束节点回答模式为text时，输出参数中不能包含name为text的字段，该字段已被输出内容占用");
                    }
                }
            }
            for (OutputParam p : data.getOutputParams()) {
                outputs.put(p.getName(), ParamResolver.resolve(p.getParamMapKey(), ctx));
            }
        }
        // answerMode=text 时添加 text 字段，存放 answerContent 替换后的值
        if (data != null && "text".equals(data.getAnswerMode())) {
            // 将当前收集到的输出参数作为 localContext，支持 {{output}} 这种直接引用当前输出参数的写法
            renderedText = ParamResolver.renderTemplate(data.getAnswerContent(), ctx, outputs);
            outputs.put("text", renderedText);
        }

        // 流式输出：复用 LLMTokenEvent 一次性推送整段内容（nodeType=End 区分语义）
        if (renderedText != null
                && data != null
                && Boolean.TRUE.equals(data.getStreamOutput())
                && ctx.isStreamingExecution()) {
            String nodeId = node.getId();
            String nodeType = node.getType();
            String nodeName = ctx.getNodeName(nodeId);
            ctx.emitEvent(LLMTokenEvent.token(
                    ctx.getRunId(), nodeId, nodeType, nodeName,
                    renderedText, renderedText, true));
        }

        return new NodeOutput(node.getId(), outputs);
    }
}
