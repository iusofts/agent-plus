package com.iusofts.agentplus.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
