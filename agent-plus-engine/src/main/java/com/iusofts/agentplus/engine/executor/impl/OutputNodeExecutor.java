package com.iusofts.agentplus.engine.executor.impl;

import com.iusofts.agentplus.aiflow.enums.FlowNodeType;
import com.iusofts.agentplus.aiflow.stream.LLMTokenEvent;
import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.data.OutputNodeData;
import com.iusofts.agentplus.aiflow.vo.workflow.data.common.OutputParam;
import com.iusofts.agentplus.engine.context.ExecutionContext;
import com.iusofts.agentplus.engine.context.NodeOutput;
import com.iusofts.agentplus.engine.executor.NodeExecutor;
import com.iusofts.agentplus.engine.util.ParamResolver;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Output 节点执行器。用于工作流中间过程的消息输出,执行后流程继续向下流转,不会结束工作流。
 *
 * <p>行为:</p>
 * <ol>
 *   <li>按 {@code outputParams} 从上游节点采集变量,写入 outputs。</li>
 *   <li>渲染 {@code answerContent} 模板,作为 {@code text} 字段放入 outputs。</li>
 *   <li>若 {@code streamOutput=true} 且当前为流式执行,通过 {@link LLMTokenEvent} 一次性把渲染后的整段内容推送给客户端
 *       (前端可按 {@code nodeType=Output} 识别为"输出节点消息",与 LLM token 区分)。</li>
 *   <li>返回 {@link NodeOutput},引擎照常调度下游节点。</li>
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

        // 1. 采集上游变量
        if (data != null && data.getOutputParams() != null) {
            for (OutputParam p : data.getOutputParams()) {
                outputs.put(p.getName(), ParamResolver.resolve(p.getParamMapKey(), ctx));
            }
        }

        // 2. 渲染模板 → text
        String renderedText = (data == null || data.getAnswerContent() == null)
                ? ""
                : ParamResolver.renderTemplate(data.getAnswerContent(), ctx, outputs);
        outputs.put("text", renderedText);

        // 3. 流式输出:复用 LLMTokenEvent 一次性推送整段内容(nodeType=Output 区分语义)
        boolean streamOn = data != null && Boolean.TRUE.equals(data.getStreamOutput());
        if (streamOn && ctx.isStreamingExecution()) {
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
