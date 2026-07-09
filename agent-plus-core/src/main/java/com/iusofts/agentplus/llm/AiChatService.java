package com.iusofts.agentplus.llm;

import com.iusofts.agentplus.llm.dto.ChatMessage;
import com.iusofts.agentplus.llm.dto.ChatResponse;

import java.util.List;

/**
 * AI 聊天服务接口。
 *
 * <p>业务侧统一使用这个接口调用 LLM，不依赖具体 SDK。</p>
 */
public interface AiChatService {

    /**
     * 聊天。
     *
     * @param messages   消息列表
     * @param modelId    模型 ID
     * @param temperature 温度 (可选)
     * @param maxTokens  最大回复 token 数 (可选)
     * @return AI 响应
     */
    ChatResponse chat(List<ChatMessage> messages, Long modelId, Double temperature, Integer maxTokens);
}
