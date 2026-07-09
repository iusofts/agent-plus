package com.iusofts.agentplus.plugin.llm;

import com.iusofts.agentplus.llm.dto.LlmModelDTO;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.util.StringUtils;

/**
 * LLM 模型工厂（封装所有厂商 ChatModel 构建逻辑）。
 *
 * <p>新增模型厂商仅修改此类，无 DB 依赖。</p>
 *
 * @author Ivan
 */
public class LlmModelFactory {

    private static final String PROVIDER_QWEN = "qwen";

    /**
     * 构建 ChatModel 实例。
     *
     * @param modelDTO 模型配置 DTO
     * @param temperature 温度参数（可为 null）
     * @param maxTokens 最大回复 token 数（可为 null）
     * @return ChatModel 实例
     */
    public static ChatModel createChatModel(LlmModelDTO modelDTO, Double temperature, Integer maxTokens) {
        String provider = modelDTO.getProvider() == null ? "" : modelDTO.getProvider().trim().toLowerCase();

        if (PROVIDER_QWEN.equals(provider)) {
            return buildQwen(modelDTO, temperature, maxTokens);
        }

        // doubao/openai/其他：走 OpenAI 兼容接口
        return buildOpenAiCompatible(modelDTO, temperature, maxTokens);
    }

    private static ChatModel buildQwen(LlmModelDTO modelDTO, Double temperature, Integer maxTokens) {
        QwenChatModel.QwenChatModelBuilder builder = QwenChatModel.builder()
                .apiKey(modelDTO.getApiKey())
                .modelName(modelDTO.getModelName());

        if (StringUtils.hasText(modelDTO.getBaseUrl())) {
            builder.baseUrl(modelDTO.getBaseUrl());
        }

        if (temperature != null) {
            builder.temperature(temperature.floatValue());
        }

        if (maxTokens != null) {
            builder.maxTokens(maxTokens);
        }

        return builder.build();
    }

    private static ChatModel buildOpenAiCompatible(LlmModelDTO modelDTO, Double temperature, Integer maxTokens) {
        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .apiKey(modelDTO.getApiKey())
                .modelName(modelDTO.getModelName());

        if (StringUtils.hasText(modelDTO.getBaseUrl())) {
            builder.baseUrl(modelDTO.getBaseUrl());
        }

        if (temperature != null) {
            builder.temperature(temperature);
        }

        if (maxTokens != null) {
            builder.maxTokens(maxTokens);
        }

        return builder.build();
    }
}
