package com.iusofts.agentplus.plugin.llm;

import com.iusofts.agentplus.llm.dto.LlmModelConfigDTO;
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
     * <p>生成参数（temperature/maxTokens 等）统一从 {@link LlmModelConfigDTO} 读取，
     * 新增参数只需扩展该 DTO，无需改动本方法签名。</p>
     *
     * @param modelDTO 模型连接配置 DTO
     * @param config   生成参数配置（可为 null）
     * @return ChatModel 实例
     */
    public static ChatModel createChatModel(LlmModelDTO modelDTO, LlmModelConfigDTO config) {
        String provider = modelDTO.getProvider() == null ? "" : modelDTO.getProvider().trim().toLowerCase();

        if (PROVIDER_QWEN.equals(provider)) {
            return buildQwen(modelDTO, config);
        }

        // doubao/openai/其他：走 OpenAI 兼容接口
        return buildOpenAiCompatible(modelDTO, config);
    }

    private static ChatModel buildQwen(LlmModelDTO modelDTO, LlmModelConfigDTO config) {
        QwenChatModel.QwenChatModelBuilder builder = QwenChatModel.builder()
                .apiKey(modelDTO.getApiKey())
                .modelName(modelDTO.getModelName());

        if (StringUtils.hasText(modelDTO.getBaseUrl())) {
            builder.baseUrl(modelDTO.getBaseUrl());
        }

        if (config != null) {
            if (config.getTemperature() != null) {
                builder.temperature(config.getTemperature().floatValue());
            }
            if (config.getMaxTokens() != null) {
                builder.maxTokens(config.getMaxTokens());
            }
        }

        return builder.build();
    }

    private static ChatModel buildOpenAiCompatible(LlmModelDTO modelDTO, LlmModelConfigDTO config) {
        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .apiKey(modelDTO.getApiKey())
                .modelName(modelDTO.getModelName());

        if (StringUtils.hasText(modelDTO.getBaseUrl())) {
            builder.baseUrl(modelDTO.getBaseUrl());
        }

        if (config != null) {
            if (config.getTemperature() != null) {
                builder.temperature(config.getTemperature());
            }
            if (config.getMaxTokens() != null) {
                builder.maxTokens(config.getMaxTokens());
            }
        }

        return builder.build();
    }
}
