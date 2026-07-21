package com.iusofts.agentplus.engine.llm;

import com.iusofts.agentplus.aiflow.vo.workflow.data.llm.LLMNodeData;
import com.iusofts.agentplus.llm.dto.ChatMessage;
import com.iusofts.agentplus.llm.dto.LlmModelConfigDTO;
import com.iusofts.agentplus.llm.dto.ToolDefinition;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.TokenUsage;
import org.springframework.util.StringUtils;

import java.util.List;
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

    @Override
    public com.iusofts.agentplus.llm.dto.ChatResponse chat(Long modelId, List<ChatMessage> messages,
                                                            LlmModelConfigDTO config, List<ToolDefinition> tools) {
        // 兜底实现：将消息转换为 LangChain4j 格式调用
        ChatModel model = provide(null);
        List<dev.langchain4j.data.message.ChatMessage> lcMessages = convertToLangChain4j(messages);
        ChatRequest request = ChatRequest.builder().messages(lcMessages).build();
        dev.langchain4j.model.chat.response.ChatResponse response = model.chat(request);
        AiMessage aiMessage = response.aiMessage();
        TokenUsage tokenUsage = response.tokenUsage();

        return com.iusofts.agentplus.llm.dto.ChatResponse.builder()
                .content(aiMessage.text())
                .inputTokens(tokenUsage != null ? tokenUsage.inputTokenCount() : null)
                .outputTokens(tokenUsage != null ? tokenUsage.outputTokenCount() : null)
                .build();
    }

    private List<dev.langchain4j.data.message.ChatMessage> convertToLangChain4j(List<ChatMessage> messages) {
        List<dev.langchain4j.data.message.ChatMessage> result = new java.util.ArrayList<>();
        for (ChatMessage msg : messages) {
            switch (msg.getRole().toLowerCase()) {
                case "system":
                    result.add(dev.langchain4j.data.message.SystemMessage.from(msg.getContent()));
                    break;
                case "user":
                    result.add(dev.langchain4j.data.message.UserMessage.from(msg.getContent()));
                    break;
                case "assistant":
                    result.add(dev.langchain4j.data.message.AiMessage.from(msg.getContent()));
                    break;
                // tool messages in tool call are handled by AiChatService in the primary implementation
                default:
                    result.add(dev.langchain4j.data.message.UserMessage.from(msg.getContent()));
            }
        }
        return result;
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
