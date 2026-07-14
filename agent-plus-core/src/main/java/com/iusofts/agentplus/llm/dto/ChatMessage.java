package com.iusofts.agentplus.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 聊天消息。
 *
 * <p>role 取值：system / user / assistant / tool。</p>
 * <ul>
 *   <li>assistant 请求工具调用时，{@link #toolCalls} 非空；</li>
 *   <li>role="tool" 回填工具结果时，{@link #toolCallId} 与 {@link #name} 标识对应的工具调用。</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String role;
    private String content;

    /**
     * assistant 消息中模型请求的工具调用列表。
     */
    private List<ToolCall> toolCalls;

    /**
     * tool 消息回填时对应的工具调用 ID。
     */
    private String toolCallId;

    /**
     * tool 消息回填时对应的工具名称。
     */
    private String name;
}
