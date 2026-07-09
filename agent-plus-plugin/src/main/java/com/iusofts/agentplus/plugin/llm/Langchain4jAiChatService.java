package com.iusofts.agentplus.plugin.llm;

import com.iusofts.agentplus.llm.AiChatService;
import com.iusofts.agentplus.llm.LlmModelCacheEvictor;
import com.iusofts.agentplus.llm.dto.ChatMessage;
import com.iusofts.agentplus.llm.dto.ChatResponse;
import com.iusofts.agentplus.llm.dto.LlmModelDTO;
import com.iusofts.agentplus.llm.LlmModelQueryProvider;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * AiChatService 实现，基于 langchain4j。
 *
 * <p>业务侧统一使用此接口调用 LLM，不依赖具体 SDK。
 *
 * @author Ivan
 */
@Service
public class Langchain4jAiChatService implements AiChatService, LlmModelCacheEvictor {

    private final LlmModelQueryProvider modelQueryProvider;

    /**
     * 缓存 key = modelId + "@" + temperature + "@" + maxTokens，避免每次调用重建 ChatModel。
     */
    private final ConcurrentMap<String, ChatModel> cache = new ConcurrentHashMap<>();

    public Langchain4jAiChatService(LlmModelQueryProvider modelQueryProvider) {
        this.modelQueryProvider = modelQueryProvider;
    }

    @Override
    public void evict(Long modelId) {
        if (modelId == null) {
            return;
        }
        // 同一 modelId 可能存在多个不同 temperature 的缓存条目，全部清理
        String prefix = modelId + "@";
        cache.keySet().removeIf(key -> key.startsWith(prefix));
    }

    @Override
    public ChatResponse chat(List<ChatMessage> messages, Long modelId, Double temperature, Integer maxTokens) {
        String cacheKey = modelId + "@" + temperature + "@" + maxTokens;
        ChatModel chatModel = cache.computeIfAbsent(cacheKey, k -> {
            LlmModelDTO modelDTO = modelQueryProvider.getModel(modelId);
            return LlmModelFactory.createChatModel(modelDTO, temperature, maxTokens);
        });

        // 转换消息格式
        List<dev.langchain4j.data.message.ChatMessage> lc4jMessages = new ArrayList<>();
        for (ChatMessage msg : messages) {
            lc4jMessages.add(toLc4jMessage(msg));
        }

        // 调用 langchain4j
        dev.langchain4j.model.chat.response.ChatResponse response = chatModel.chat(
                ChatRequest.builder().messages(lc4jMessages).build()
        );

        // 转换响应格式
        return toChatResponse(response);
    }

    private dev.langchain4j.data.message.ChatMessage toLc4jMessage(ChatMessage message) {
        String role = message.getRole();
        String content = message.getContent();

        if ("system".equalsIgnoreCase(role)) {
            return SystemMessage.from(content);
        } else if ("user".equalsIgnoreCase(role)) {
            return UserMessage.from(content);
        } else if ("assistant".equalsIgnoreCase(role)) {
            return AiMessage.from(content);
        } else {
            return UserMessage.from(content);
        }
    }

    private ChatResponse toChatResponse(dev.langchain4j.model.chat.response.ChatResponse lc4jResponse) {
        String content = lc4jResponse.aiMessage().text();

        return ChatResponse.builder()
                .content(content)
                .inputTokens(null)
                .outputTokens(null)
                .totalTokens(null)
                .build();
    }
}
