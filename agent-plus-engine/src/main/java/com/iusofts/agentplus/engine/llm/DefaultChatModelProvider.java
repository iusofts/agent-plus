package com.iusofts.agentplus.engine.llm;

import com.iusofts.agentplus.aiflow.vo.workflow.data.llm.LLMNodeData;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.util.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 默认 {@link ChatModelProvider} 实现，通过 OpenAI 兼容 API 接入千问/豆包。
 *
 * <p>这是<b>未接入数据库模型表时的兜底实现</b>：构造时千问优先、豆包兜底，
 * 选定一个渠道（apiKey + baseUrl + 默认模型名）。业务模块（agent-plus-service）
 * 若提供了按 {@code ai_model} 表路由的 {@link ChatModelProvider} bean，
 * 会通过 {@code @ConditionalOnMissingBean} 覆盖本实现。</p>
 *
 * <p>本实现不解析 {@link LLMNodeData#getModelId()}（模型 ID），因为兜底场景没有 id→渠道 映射；
 * 但节点上的 {@code temperature} 会生效，按温度缓存 {@link ChatModel} 实例避免重建。</p>
 *
 * @author Ivan
 */
public class DefaultChatModelProvider implements ChatModelProvider {

    private final String apiKey;
    private final String baseUrl;
    private final String modelName;

    /** temperature -> ChatModel 缓存；key 为 null 表示使用渠道默认温度。 */
    private final ConcurrentMap<Double, ChatModel> modelCache = new ConcurrentHashMap<>();
    private volatile ChatModel defaultModel;

    public DefaultChatModelProvider(QwenProperties qwenProperties, DoubaoProperties doubaoProperties) {
        // 优先使用千问
        if (StringUtils.hasText(qwenProperties.getApiKey())) {
            this.apiKey = qwenProperties.getApiKey();
            this.baseUrl = qwenProperties.getBaseUrl();
            this.modelName = qwenProperties.getModel();
        } else if (StringUtils.hasText(doubaoProperties.getApiKey())) {
            this.apiKey = doubaoProperties.getApiKey();
            this.baseUrl = doubaoProperties.getBaseUrl();
            this.modelName = doubaoProperties.getModel();
        } else {
            throw new IllegalStateException("请配置千问或豆包的 API Key");
        }
    }

    @Override
    public ChatModel provide(LLMNodeData nodeData) {
        Double temperature = nodeData == null ? null : nodeData.getTemperature();
        if (temperature == null) {
            if (defaultModel == null) {
                synchronized (this) {
                    if (defaultModel == null) {
                        defaultModel = createChatModel(null);
                    }
                }
            }
            return defaultModel;
        }
        return modelCache.computeIfAbsent(temperature, this::createChatModel);
    }

    private ChatModel createChatModel(Double temperature) {
        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .apiKey(apiKey);
        if (StringUtils.hasText(baseUrl)) {
            builder.baseUrl(baseUrl);
        }
        if (StringUtils.hasText(modelName)) {
            builder.modelName(modelName);
        }
        if (temperature != null) {
            builder.temperature(temperature);
        }
        return builder.build();
    }
}
