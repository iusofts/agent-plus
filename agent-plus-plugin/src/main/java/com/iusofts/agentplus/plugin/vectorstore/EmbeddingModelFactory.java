package com.iusofts.agentplus.plugin.vectorstore;

import com.iusofts.agentplus.knowledge.EmbeddingModelDTO;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import org.springframework.util.StringUtils;

/**
 * 嵌入模型工厂（封装所有厂商 EmbeddingModel 构建逻辑）。
 *
 * <p>新增模型厂商仅修改此类，无 DB 依赖。</p>
 *
 * @author Ivan
 */
public class EmbeddingModelFactory {

    private static final String PROVIDER_QWEN = "qwen";

    /**
     * 构建 EmbeddingModel 实例。
     *
     * @param modelDTO 模型配置 DTO
     * @return EmbeddingModel 实例
     */
    public static EmbeddingModel createEmbeddingModel(EmbeddingModelDTO modelDTO) {
        String provider = modelDTO.getProvider() == null ? "" : modelDTO.getProvider().trim().toLowerCase();

        if (PROVIDER_QWEN.equals(provider)) {
            return buildQwen(modelDTO);
        }

        // doubao/openai/其他：走 OpenAI 兼容接口
        return buildOpenAiCompatible(modelDTO);
    }

    private static EmbeddingModel buildQwen(EmbeddingModelDTO modelDTO) {
        return QwenEmbeddingModel.builder()
                .apiKey(modelDTO.getApiKey())
                .modelName(modelDTO.getModelName())
                .build();
    }

    private static EmbeddingModel buildOpenAiCompatible(EmbeddingModelDTO modelDTO) {
        OpenAiEmbeddingModel.OpenAiEmbeddingModelBuilder builder = OpenAiEmbeddingModel.builder()
                .apiKey(modelDTO.getApiKey())
                .modelName(modelDTO.getModelName());

        if (StringUtils.hasText(modelDTO.getBaseUrl())) {
            builder.baseUrl(modelDTO.getBaseUrl());
        }

        return builder.build();
    }
}
