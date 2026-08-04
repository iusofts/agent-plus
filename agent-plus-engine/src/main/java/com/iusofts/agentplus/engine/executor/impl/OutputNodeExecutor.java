package com.iusofts.agentplus.engine.executor.impl;

import com.iusofts.agentplus.aiflow.enums.FlowNodeType;
import com.iusofts.agentplus.aiflow.stream.MessageCompleteEvent;
import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.data.OutputNodeData;
import com.iusofts.agentplus.aiflow.vo.workflow.data.common.OutputParam;
import com.iusofts.agentplus.engine.context.ExecutionContext;
import com.iusofts.agentplus.engine.context.NodeOutput;
import com.iusofts.agentplus.engine.executor.NodeExecutor;
import com.iusofts.agentplus.engine.util.ParamResolver;
import com.iusofts.agentplus.engine.util.TemplateRenderer;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Output 节点执行器。用于工作流中间过程的消息输出,执行后流程继续向下流转,不会结束工作流。
 *
 * <p>行为:</p>
 * <ol>
 *   <li>按 {@code outputParams} 从上游节点采集变量,写入 outputs。</li>
 *   <li>渲染 {@code answerContent} 模板(支持 {{node.name}} 和 {{name}} 占位符),作为 {@code text} 字段放入 outputs。</li>
 *   <li>若当前为流式执行,emit {@link MessageCompleteEvent}(content=渲染后完整文本, isOutput=true),客户端按 nodeType 区分展示。</li>
 * </ol>
 *
 * <p>本节点不参与 {@code collectEndOutputs} 汇总(仅 End 节点进入),因此 finalOutput 不会包含本节点的 text,
 * finalOutput 完全由 End 节点决定。</p>
 *
 * @author Ivan
 */
public class OutputNodeExecutor implements NodeExecutor {

    @Override
    public FlowNodeType type() {
        return FlowNodeType.OUTPUT;
    }

    @Override
    public NodeOutput execute(Node node, ExecutionContext ctx) {
        Map<String, Object> outputs = new LinkedHashMap<>();
        OutputNodeData data = (OutputNodeData) node.getData();

        // 1. 采集上游变量(localContext,供 {{name}} 简单占位)
        if (data != null && data.getOutputParams() != null) {
            for (OutputParam p : data.getOutputParams()) {
                outputs.put(p.getName(), ParamResolver.resolve(p.getParamMapKey(), ctx));
            }
        }

        // 2. 渲染模板(完整返回,不再流式增量)
        String template = (data == null || data.getAnswerContent() == null) ? "" : data.getAnswerContent();
        String renderedText = TemplateRenderer.render(template, ctx, outputs);
        outputs.put("text", renderedText);

        // 3. 流式执行时,emit MessageCompleteEvent 一次性推完整内容
        if (ctx.isStreamingExecution()) {
            ctx.emitEvent(MessageCompleteEvent.create(
                    ctx.getRunId(), node.getId(), node.getType(),
                    ctx.getNodeName(node.getId()), renderedText, true));
        }

        return new NodeOutput(node.getId(), outputs);
    }
}
