package com.iusofts.agentplus.llm;

import com.iusofts.agentplus.llm.dto.ChatMessage;
import com.iusofts.agentplus.llm.dto.ChatResponse;
import com.iusofts.agentplus.llm.dto.LlmModelConfigDTO;

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
     * @param messages 消息列表
     * @param modelId  模型 ID
     * @param config   生成参数配置 (可为 null，使用模型默认)
     * @return AI 响应
     */
    ChatResponse chat(List<ChatMessage> messages, Long modelId, LlmModelConfigDTO config);
}
