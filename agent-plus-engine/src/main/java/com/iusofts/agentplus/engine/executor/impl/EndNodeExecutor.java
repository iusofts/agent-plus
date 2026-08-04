package com.iusofts.agentplus.engine.executor.impl;

import com.iusofts.agentplus.aiflow.enums.AnswerModeEnum;
import com.iusofts.agentplus.aiflow.enums.FlowNodeType;
import com.iusofts.agentplus.aiflow.enums.FlowTypeEnum;
import com.iusofts.agentplus.aiflow.stream.MessageCompleteEvent;
import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.data.EndNodeData;
import com.iusofts.agentplus.aiflow.vo.workflow.data.common.OutputParam;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.engine.context.ExecutionContext;
import com.iusofts.agentplus.engine.context.NodeOutput;
import com.iusofts.agentplus.engine.executor.NodeExecutor;
import com.iusofts.agentplus.engine.util.ParamResolver;
import com.iusofts.agentplus.engine.util.TemplateRenderer;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * End 节点执行器。将 outputParams 从上游节点采集,组装为最终结果。
 *
 * <p>{@code answerMode=VARIABLE}:返回所有 outputParams 的变量映射(原 JSON 形式)。</p>
 *
 * <p>{@code answerMode=TEXT}:渲染 {@code answerContent} 模板得到完整文本,作为 {@code text} 字段放入 outputs。
 * 流式执行时,emit {@link MessageCompleteEvent}(content=渲染后完整文本) 给客户端,
 * 让前端按 nodeType=end 切到 End 节点气泡(独立气泡,与 Output 节点气泡分开)。</p>
 *
 * <p>对话流(CHATFLOW) + {@code answerMode!=TEXT} 时,text 与 renderedText 兜底为
 * "流程未配置回答内容",以保证对话流有内容可展示,不会因上游未配置文本模板而空白。
 * 工作流(WORKFLOW) 行为保持不变,不强制兜底。</p>
 *
 * <p>End 节点和 Output 节点是<b>两个独立的消息</b>:
 * <ul>
 *   <li>前端:各自一个气泡(按 nodeType 切)</li>
 *   <li>入库:ChatService 收到 workflow_complete 时,finalOutput.text 和 finalOutput.outputs
 *       各入库一条 ai_message</li>
 * </ul>
 * </p>
 *
 * @author Ivan
 */
public class EndNodeExecutor implements NodeExecutor {

    /** 对话流 + 非 TEXT 模式时的兜底文本。 */
    static final String DEFAULT_ANSWER_TEXT = "流程未配置回答内容";

    @Override
    public FlowNodeType type() {
        return FlowNodeType.END;
    }

    @Override
    public NodeOutput execute(Node node, ExecutionContext ctx) {
        Map<String, Object> outputs = new LinkedHashMap<>();
        EndNodeData data = (EndNodeData) node.getData();
        AnswerModeEnum answerMode = data == null ? null : data.getAnswerMode();
        if (data != null && data.getOutputParams() != null) {
            // 校验:answerMode=TEXT 时 outputParams 中不能有 name=text 的字段
            if (answerMode == AnswerModeEnum.TEXT) {
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

        String renderedText = null;
        if (answerMode == AnswerModeEnum.TEXT) {
            String template = data == null || data.getAnswerContent() == null ? "" : data.getAnswerContent();
            renderedText = TemplateRenderer.render(template, ctx, outputs);
            outputs.put("text", renderedText);
        } else if (ctx.getFlowType() == FlowTypeEnum.CHATFLOW) {
            // 对话流 + answerMode!=TEXT:兜底输出,避免对话流空白
            renderedText = DEFAULT_ANSWER_TEXT;
            outputs.put("text", renderedText);
        }

        // 流式执行时,emit MessageCompleteEvent 让前端按 nodeType=end 切到独立气泡
        if (ctx.isStreamingExecution() && renderedText != null) {
            ctx.emitEvent(MessageCompleteEvent.create(
                    ctx.getRunId(), node.getId(), node.getType(),
                    ctx.getNodeName(node.getId()), renderedText, true));
        }

        return new NodeOutput(node.getId(), outputs);
    }
}
