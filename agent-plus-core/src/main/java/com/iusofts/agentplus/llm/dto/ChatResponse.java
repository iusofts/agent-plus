package com.iusofts.agentplus.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI 聊天响应。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String content;
    private Integer inputTokens;
    private Integer outputTokens;
    private Integer totalTokens;

    /**
     * 模型请求的工具调用列表（非空表示需要执行工具后再次推理）。
     */
    private List<ToolCall> toolCalls;

    /**
     * 结束原因，如 STOP / TOOL_EXECUTION。
     */
    private String finishReason;
}
