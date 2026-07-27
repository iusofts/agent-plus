package com.iusofts.agentplus.plugin.llm;

import com.iusofts.agentplus.common.enums.ParamTypeEnum;
import com.iusofts.agentplus.llm.AiChatService;
import com.iusofts.agentplus.llm.LlmModelCacheEvictor;
import com.iusofts.agentplus.llm.LlmModelQueryProvider;
import com.iusofts.agentplus.llm.dto.*;
import com.iusofts.agentplus.tool.dto.ToolParam;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.json.*;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * AiChatService 实现，基于 langchain4j。
 *
 * <p>业务侧统一使用此接口调用 LLM，不依赖具体 SDK。支持 function calling：
 * 下发工具规格、解析模型请求的工具调用、回填工具执行结果。
 *
 * @author Ivan
 */
@Service
public class Langchain4jAiChatService implements AiChatService, LlmModelCacheEvictor {

    private final LlmModelQueryProvider modelQueryProvider;

    /**
     * 缓存 key = modelId + 生成参数，避免每次调用重建 ChatModel。
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
    public AiChatResponse chat(List<AiChatMessage> messages, Long modelId, LlmModelConfigDTO config, List<ToolDefinition> tools) {
        String cacheKey = buildCacheKey(modelId, config);
        ChatModel chatModel = cache.computeIfAbsent(cacheKey, k -> {
            LlmModelDTO modelDTO = modelQueryProvider.getModel(modelId);
            return LlmModelFactory.createChatModel(modelDTO, config);
        });

        // 转换消息格式
        List<ChatMessage> lc4jMessages = new ArrayList<>();
        for (AiChatMessage msg : messages) {
            lc4jMessages.add(toLc4jMessage(msg));
        }

        // 构建请求，按需下发工具规格
        ChatRequest.Builder requestBuilder = ChatRequest.builder().messages(lc4jMessages);
        if (!CollectionUtils.isEmpty(tools)) {
            List<ToolSpecification> specs = new ArrayList<>();
            for (ToolDefinition tool : tools) {
                specs.add(toToolSpecification(tool));
            }
            requestBuilder.toolSpecifications(specs);
        }

        // 调用 langchain4j
        dev.langchain4j.model.chat.response.ChatResponse response = chatModel.chat(requestBuilder.build());

        // 转换响应格式
        return toChatResponse(response);
    }

    private ChatMessage toLc4jMessage(AiChatMessage message) {
        String role = message.getRole();
        String content = message.getContent();

        if ("system".equalsIgnoreCase(role)) {
            return SystemMessage.from(content);
        } else if ("user".equalsIgnoreCase(role)) {
            return UserMessage.from(content);
        } else if ("assistant".equalsIgnoreCase(role)) {
            // assistant 请求工具调用时，转为带 ToolExecutionRequest 的 AiMessage
            if (!CollectionUtils.isEmpty(message.getToolCalls())) {
                List<ToolExecutionRequest> requests = new ArrayList<>();
                for (ToolCall call : message.getToolCalls()) {
                    requests.add(ToolExecutionRequest.builder()
                            .id(call.getId())
                            .name(call.getName())
                            .arguments(call.getArguments())
                            .build());
                }
                if (content != null && !content.isBlank()) {
                    return AiMessage.from(content, requests);
                }
                return AiMessage.from(requests);
            }
            return AiMessage.from(content);
        } else if ("tool".equalsIgnoreCase(role)) {
            // 工具执行结果回填
            return ToolExecutionResultMessage.from(message.getToolCallId(), message.getName(), content);
        } else {
            return UserMessage.from(content);
        }
    }

    private AiChatResponse toChatResponse(dev.langchain4j.model.chat.response.ChatResponse lc4jResponse) {
        AiMessage aiMessage = lc4jResponse.aiMessage();
        String content = aiMessage.text();

        List<ToolCall> toolCalls = null;
        if (aiMessage.hasToolExecutionRequests()) {
            toolCalls = new ArrayList<>();
            for (ToolExecutionRequest req : aiMessage.toolExecutionRequests()) {
                toolCalls.add(ToolCall.builder()
                        .id(req.id())
                        .name(req.name())
                        .arguments(req.arguments())
                        .build());
            }
        }

        Integer inputTokens = null;
        Integer outputTokens = null;
        Integer totalTokens = null;
        TokenUsage tokenUsage = lc4jResponse.tokenUsage();
        if (tokenUsage != null) {
            inputTokens = tokenUsage.inputTokenCount();
            outputTokens = tokenUsage.outputTokenCount();
            totalTokens = tokenUsage.totalTokenCount();
        }

        FinishReason finishReason = lc4jResponse.finishReason();

        return AiChatResponse.builder()
                .content(content)
                .inputTokens(inputTokens)
                .outputTokens(outputTokens)
                .totalTokens(totalTokens)
                .toolCalls(toolCalls)
                .finishReason(finishReason != null ? finishReason.name() : null)
                .build();
    }

    /**
     * 将工具规格转换为 langchain4j 的 {@link ToolSpecification}。
     */
    private ToolSpecification toToolSpecification(ToolDefinition tool) {
        ToolSpecification.Builder builder = ToolSpecification.builder()
                .name(tool.getName())
                .description(tool.getDescription());

        List<ToolParam> params = tool.getParameters();
        if (!CollectionUtils.isEmpty(params)) {
            builder.parameters(buildObjectSchema(params));
        }

        return builder.build();
    }

    /**
     * 由参数列表构建 {@link JsonObjectSchema}，递归处理 Object/Array 的子参数。
     */
    private JsonObjectSchema buildObjectSchema(List<ToolParam> params) {
        JsonObjectSchema.Builder schemaBuilder = JsonObjectSchema.builder();
        List<String> required = new ArrayList<>();
        for (ToolParam param : params) {
            if (param.getEnabled() != null && !param.getEnabled()) {
                continue;
            }
            schemaBuilder.addProperty(param.getName(), toSchemaElement(param));
            if (Boolean.TRUE.equals(param.getRequired())) {
                required.add(param.getName());
            }
        }
        if (!required.isEmpty()) {
            schemaBuilder.required(required);
        }
        return schemaBuilder.build();
    }

    /**
     * 将单个参数转换为 JSON schema 元素；文件类等复杂类型统一按字符串处理。
     *
     * <ul>
     *   <li>Object：递归以 children 构建嵌套对象；</li>
     *   <li>Array：以 itemType 或 children 推断 items 类型。</li>
     * </ul>
     */
    private JsonSchemaElement toSchemaElement(ToolParam param) {
        String description = param.getDescription();
        ParamTypeEnum type = ParamTypeEnum.fromValue(param.getType());
        if (type == null) {
            return JsonStringSchema.builder().description(description).build();
        }
        switch (type) {
            case INTEGER:
                return JsonIntegerSchema.builder().description(description).build();
            case NUMBER:
                return JsonNumberSchema.builder().description(description).build();
            case BOOLEAN:
                return JsonBooleanSchema.builder().description(description).build();
            case OBJECT:
                if (!CollectionUtils.isEmpty(param.getChildren())) {
                    JsonObjectSchema nested = buildObjectSchema(param.getChildren());
                    return description == null ? nested : nested.toBuilder().description(description).build();
                }
                return JsonObjectSchema.builder().description(description).build();
            case ARRAY:
                return JsonArraySchema.builder()
                        .description(description)
                        .items(buildArrayItemSchema(param))
                        .build();
            case STRING:
            default:
                return JsonStringSchema.builder().description(description).build();
        }
    }

    /**
     * 推断数组元素的 schema：优先按 children（对象数组），其次按 itemType，缺省为字符串。
     */
    private JsonSchemaElement buildArrayItemSchema(ToolParam param) {
        if (!CollectionUtils.isEmpty(param.getChildren())) {
            return buildObjectSchema(param.getChildren());
        }
        ParamTypeEnum itemType = ParamTypeEnum.fromValue(param.getItemType());
        if (itemType == null) {
            return JsonStringSchema.builder().build();
        }
        switch (itemType) {
            case INTEGER:
                return JsonIntegerSchema.builder().build();
            case NUMBER:
                return JsonNumberSchema.builder().build();
            case BOOLEAN:
                return JsonBooleanSchema.builder().build();
            case OBJECT:
                return JsonObjectSchema.builder().build();
            case STRING:
            default:
                return JsonStringSchema.builder().build();
        }
    }

    /**
     * 生成缓存 key：modelId 叠加生成参数，参数不同则使用不同 ChatModel 实例。
     */
    private String buildCacheKey(Long modelId, LlmModelConfigDTO config) {
        if (config == null) {
            return modelId + "@default";
        }
        return modelId + "@" + config.getTemperature() + "@" + config.getMaxTokens();
    }
}
