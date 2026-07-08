package com.iusofts.agentplus.ai.knowledge;

import com.iusofts.agentplus.ai.entity.AiModel;
import com.iusofts.agentplus.ai.mapper.AiModelMapper;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 基于 {@code ai_model} 表(model_type=2 Embedding)构建 {@link EmbeddingModel}。
 *
 * <p>与 {@code AiModelChatModelProvider} 同构:按 provider 路由</p>
 * <ul>
 *   <li>qwen   -> {@link QwenEmbeddingModel}(DashScope,如 text-embedding-v3)</li>
 *   <li>doubao/openai/其他 -> {@link OpenAiEmbeddingModel}(OpenAI 兼容 API)</li>
 * </ul>
 *
 * <p>按 modelId 缓存,避免重复构建。查不到/被禁用/缺配置直接抛异常。</p>
 *
 * @author Ivan
 */
@Component
public class EmbeddingModelProvider {

    private static final String PROVIDER_QWEN = "qwen";

    /** Embedding 模型类型标识(与 ai_model.model_type 对应)。 */
    private static final int MODEL_TYPE_EMBEDDING = 2;

    private final AiModelMapper aiModelMapper;

    private final ConcurrentMap<Long, EmbeddingModel> cache = new ConcurrentHashMap<>();

    public EmbeddingModelProvider(AiModelMapper aiModelMapper) {
        this.aiModelMapper = aiModelMapper;
    }

    /**
     * 获取指定嵌入模型。
     *
     * @param modelId ai_model.id(model_type=2)
     * @return 就绪的 EmbeddingModel
     */
    public EmbeddingModel provide(Long modelId) {
        if (modelId == null) {
            throw new IllegalStateException("知识库未配置嵌入模型(embeddingModelId 为空)");
        }
        return cache.computeIfAbsent(modelId, this::buildModel);
    }

    private EmbeddingModel buildModel(Long modelId) {
        AiModel model = aiModelMapper.selectById(modelId);
        if (model == null) {
            throw new IllegalStateException("找不到嵌入模型配置: ai_model.id=" + modelId);
        }
        if (model.getStatus() != null && model.getStatus() == 0) {
            throw new IllegalStateException("嵌入模型已禁用: ai_model.id=" + modelId
                    + " (" + model.getDisplayName() + ")");
        }
        if (model.getModelType() != null && model.getModelType() != MODEL_TYPE_EMBEDDING) {
            throw new IllegalStateException("ai_model.id=" + modelId + " 非 Embedding 类型模型");
        }
        if (!StringUtils.hasText(model.getApiKey())) {
            throw new IllegalStateException("嵌入模型缺少 apiKey: ai_model.id=" + modelId);
        }
        if (!StringUtils.hasText(model.getModelName())) {
            throw new IllegalStateException("嵌入模型缺少 modelName: ai_model.id=" + modelId);
        }

        String provider = model.getProvider() == null ? "" : model.getProvider().trim().toLowerCase();
        if (PROVIDER_QWEN.equals(provider)) {
            return QwenEmbeddingModel.builder()
                    .apiKey(model.getApiKey())
                    .modelName(model.getModelName())
                    .build();
        }
        // doubao / openai / 其他:走 OpenAI 兼容 API
        OpenAiEmbeddingModel.OpenAiEmbeddingModelBuilder builder = OpenAiEmbeddingModel.builder()
                .apiKey(model.getApiKey())
                .modelName(model.getModelName());
        if (StringUtils.hasText(model.getBaseUrl())) {
            builder.baseUrl(model.getBaseUrl());
        }
        return builder.build();
    }
}
