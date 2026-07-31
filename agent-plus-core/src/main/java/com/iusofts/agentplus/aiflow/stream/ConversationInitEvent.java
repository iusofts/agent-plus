package com.iusofts.agentplus.aiflow.stream;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * 会话初始化流式事件。
 *
 * <p>由聊天流（AiChatServiceImpl / FlowChatServiceImpl）在新建会话时作为首事件推送，
 * 用于把服务端创建好的 conversationId 回传给前端，避免前端在 SSE 流开启后仍无法
 * 关联到会话/侧栏。</p>
 *
 * @author Ivan Shen
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ConversationInitEvent extends WorkflowStreamEvent {
    /** 会话ID */
    private Long conversationId;

    public static ConversationInitEvent create(String runId, Long conversationId) {
        ConversationInitEvent event = new ConversationInitEvent();
        event.setType(WorkflowStreamEventType.CONVERSATION_INIT.value());
        event.setRunId(runId);
        event.setConversationId(conversationId);
        event.setTimestamp(java.time.Instant.now().toEpochMilli());
        return event;
    }
}
