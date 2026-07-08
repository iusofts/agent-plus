package com.iusofts.agentplus.ai.llm;

import com.iusofts.agentplus.ai.entity.AiModel;
import com.iusofts.agentplus.ai.mapper.AiModelMapper;
import com.iusofts.agentplus.aiflow.vo.workflow.data.llm.LLMNodeData;
import com.iusofts.agentplus.engine.llm.ChatModelProvider;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 基于 {@code ai_model} 表的 {@link ChatModelProvider} 实现。
 *
 * <p>LLM 节点的 {@link LLMNodeData#getModel()} 即为 {@code ai_model.id}，据此查表拿到
 * provider(qwen/doubao/openai) + apiKey + baseUrl + modelName，再构建对应的 {@link ChatModel}：</p>
 * <ul>
 *   <li>qwen   -> {@link QwenChatModel}（DashScope 专用）</li>
 *   <li>doubao/openai/其他 -> {@link OpenAiChatModel}（OpenAI 兼容 API）</li>
 * </ul>
 *
 * <p>本 bean 标注 {@link Primary}，覆盖引擎模块的兜底 {@code DefaultChatModelProvider}
 * （其 bean 定义带 {@code @ConditionalOnMissingBean}，本 bean 存在时不会创建）。</p>
 *
 * <p>查不到模型、模型被禁用或缺少必要配置时直接抛异常，令节点执行失败，而非静默走兜底。</p>
 *
 * @author Ivan
 */
@Primary
@Component
public class AiModelChatModelProvider implements ChatModelProvider {

    private static final String PROVIDER_QWEN = "qwen";

    private final AiModelMapper aiModelMapper;

    /** 缓存 key = modelId + "@" + temperature，避免每次调用重建 ChatModel。 */
    private final ConcurrentMap<String, ChatModel> cache = new ConcurrentHashMap<>();

    public AiModelChatModelProvider(AiModelMapper aiModelMapper) {
        this.aiModelMapper = aiModelMapper;
    }

    @Override
    public ChatModel provide(LLMNodeData nodeData) {
        if (nodeData == null || nodeData.getModel() == null) {
            throw new IllegalStateException("LLM 节点未指定模型(model 为空)");
        }
        Long modelId = nodeData.getModel();
        Double temperature = nodeData.getTemperature();
        String cacheKey = modelId + "@" + temperature;
        return cache.computeIfAbsent(cacheKey, k -> buildModel(modelId, temperature));
    }

    private ChatModel buildModel(Long modelId, Double temperature) {
        AiModel model = aiModelMapper.selectById(modelId);
        if (model == null) {
            throw new IllegalStateException("找不到模型配置: ai_model.id=" + modelId);
        }
        if (model.getStatus() != null && model.getStatus() == 0) {
            throw new IllegalStateException("模型已禁用: ai_model.id=" + modelId
                    + " (" + model.getDisplayName() + ")");
        }
        if (!StringUtils.hasText(model.getApiKey())) {
            throw new IllegalStateException("模型缺少 apiKey: ai_model.id=" + modelId);
        }
        if (!StringUtils.hasText(model.getModelName())) {
            throw new IllegalStateException("模型缺少 modelName: ai_model.id=" + modelId);
        }

        String provider = model.getProvider() == null ? "" : model.getProvider().trim().toLowerCase();
        if (PROVIDER_QWEN.equals(provider)) {
            return buildQwen(model, temperature);
        }
        // doubao / openai / 其他：走 OpenAI 兼容 API
        return buildOpenAiCompatible(model, temperature);
    }

    private ChatModel buildQwen(AiModel model, Double temperature) {
        QwenChatModel.QwenChatModelBuilder builder = QwenChatModel.builder()
                .apiKey(model.getApiKey())
                .modelName(model.getModelName());
        if (StringUtils.hasText(model.getBaseUrl())) {
            builder.baseUrl(model.getBaseUrl());
        }
        if (temperature != null) {
            // QwenChatModel 温度参数为 Float
            builder.temperature(temperature.floatValue());
        }
        return builder.build();
    }

    private ChatModel buildOpenAiCompatible(AiModel model, Double temperature) {
        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .apiKey(model.getApiKey())
                .modelName(model.getModelName());
        if (StringUtils.hasText(model.getBaseUrl())) {
            builder.baseUrl(model.getBaseUrl());
        }
        if (temperature != null) {
            builder.temperature(temperature);
        }
        return builder.build();
    }
}
