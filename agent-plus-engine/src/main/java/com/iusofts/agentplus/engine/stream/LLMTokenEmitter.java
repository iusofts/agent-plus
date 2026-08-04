package com.iusofts.agentplus.engine.stream;

import com.iusofts.agentplus.aiflow.stream.LLMTokenEvent;
import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.engine.context.ExecutionContext;

/**
 * LLMTokenEvent 推送器:统一 emit prefix / chunk / suffix / done 四种事件。
 *
 * <p><b>已弃用:</b>流程不再走逐 token 增量流式,改为节点完成后 emit
 * {@link com.iusofts.agentplus.aiflow.stream.MessageCompleteEvent} 一次性推完整内容。
 * 本类保留以兼容旧代码/旧事件,不再有新调用方。</p>
 *
 * @author Ivan
 * @deprecated 流程不再发送 LLMTokenEvent,改用 {@code MessageCompleteEvent}
 */
@Deprecated
public final class LLMTokenEmitter {

    private LLMTokenEmitter() {
    }

    /**
     * emit 模板前缀(流式槽位前的静态文本)。
     */
    public static void emitPrefix(ExecutionContext ctx, Node node, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return;
        }
        ctx.emitEvent(LLMTokenEvent.token(
                ctx.getRunId(), node.getId(), node.getType(), ctx.getNodeName(node.getId()),
                prefix, prefix, false));
    }

    /**
     * emit 单个 token chunk(LLM 流式增量)。
     * accumulated 由调用方维护并传入,确保前端拿到的是"截至当前的完整内容"。
     */
    public static void emitChunk(ExecutionContext ctx, Node node, String chunk, String accumulated) {
        if (chunk == null) {
            return;
        }
        ctx.emitEvent(LLMTokenEvent.token(
                ctx.getRunId(), node.getId(), node.getType(), ctx.getNodeName(node.getId()),
                chunk, accumulated, false));
    }

    /**
     * emit 模板后缀(流式槽位后的静态文本)。
     */
    public static void emitSuffix(ExecutionContext ctx, Node node, String suffix, String fullText) {
        if (suffix == null || suffix.isEmpty()) {
            return;
        }
        ctx.emitEvent(LLMTokenEvent.token(
                ctx.getRunId(), node.getId(), node.getType(), ctx.getNodeName(node.getId()),
                suffix, fullText, false));
    }

    /**
     * emit 结束信号(done):token 为空,accumulatedContent 为完整文本。
     */
    public static void emitDone(ExecutionContext ctx, Node node, String fullText) {
        ctx.emitEvent(LLMTokenEvent.token(
                ctx.getRunId(), node.getId(), node.getType(), ctx.getNodeName(node.getId()),
                "", fullText, true));
    }

    /**
     * 整段推送(模板无/多流式槽位时降级使用):一次性 emit 整段文本,isLast=true。
     */
    public static void emitAll(ExecutionContext ctx, Node node, String fullText) {
        if (fullText == null) {
            fullText = "";
        }
        ctx.emitEvent(LLMTokenEvent.token(
                ctx.getRunId(), node.getId(), node.getType(), ctx.getNodeName(node.getId()),
                fullText, fullText, true));
    }
}
