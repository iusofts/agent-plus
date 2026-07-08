package com.iusofts.agentplus.engine.llm;

import com.iusofts.agentplus.aiflow.vo.workflow.data.llm.LLMNodeData;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认 {@link ChatModelProvider} 实现，支持千问和豆包（通过 OpenAI 兼容 API）。
 *
 * <p>当前实现使用配置文件中设置的默认模型。</p>
 *
 * @author Ivan
 */
public class DefaultChatModelProvider implements ChatModelProvider {

    private final ChatModel chatModel;

    public DefaultChatModelProvider(QwenProperties qwenProperties, DoubaoProperties doubaoProperties) {
        // 优先使用千问
        if (StringUtils.hasText(qwenProperties.getApiKey())) {
            this.chatModel = createOpenAiChatModel(
                    qwenProperties.getApiKey(),
                    qwenProperties.getBaseUrl(),
                    qwenProperties.getModel()
            );
        } else if (StringUtils.hasText(doubaoProperties.getApiKey())) {
            this.chatModel = createOpenAiChatModel(
                    doubaoProperties.getApiKey(),
                    doubaoProperties.getBaseUrl(),
                    doubaoProperties.getModel()
            );
        } else {
            throw new IllegalStateException("请配置千问或豆包的 API Key");
        }
    }

    @Override
    public ChatModel provide(LLMNodeData nodeData) {
        return chatModel;
    }

    private ChatModel createOpenAiChatModel(String apiKey, String baseUrl, String modelName) {
        try {
            // 使用反射创建，避免编译期 API 不兼容问题
            Class<?> builderClass = Class.forName("dev.langchain4j.model.openai.OpenAiChatModel$Builder");
            Class<?> openAiChatModelClass = Class.forName("dev.langchain4j.model.openai.OpenAiChatModel");

            Object builder = openAiChatModelClass.getMethod("builder").invoke(null);

            builderClass.getMethod("apiKey", String.class).invoke(builder, apiKey);
            if (StringUtils.hasText(baseUrl)) {
                builderClass.getMethod("baseUrl", String.class).invoke(builder, baseUrl);
            }
            if (StringUtils.hasText(modelName)) {
                builderClass.getMethod("modelName", String.class).invoke(builder, modelName);
            }

            return (ChatModel) builderClass.getMethod("build").invoke(builder);
        } catch (Exception e) {
            throw new RuntimeException("创建 ChatModel 失败", e);
        }
    }
}
