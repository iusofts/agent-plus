package com.iusofts.agentplus.plugin.llm;

import com.iusofts.agentplus.aiflow.vo.workflow.data.llm.LLMNodeData;
import com.iusofts.agentplus.engine.llm.ChatModelProvider;
import com.iusofts.agentplus.llm.AiChatService;
import com.iusofts.agentplus.llm.dto.AiChatRequest;
import com.iusofts.agentplus.llm.dto.AiChatResponse;
import com.iusofts.agentplus.llm.dto.LlmModelConfigDTO;
import com.iusofts.agentplus.llm.dto.LlmModelDTO;
import com.iusofts.agentplus.llm.LlmModelQueryProvider;
import com.iusofts.agentplus.llm.log.LlmLogRecorder;
import com.iusofts.agentplus.trace.TraceUtil;
import com.iusofts.agentplus.trace.annotation.TraceSpan;
import dev.langchain4j.model.chat.ChatModel;
import io.opentelemetry.api.trace.SpanKind;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.iusofts.agentplus.trace.constants.TraceConstant.ATTR_MODEL_PROVIDER;
import static com.iusofts.agentplus.trace.constants.TraceConstant.ATTR_TOKENS;


/**
 * 基于数据库的 ChatModelProvider 实现（无 DB 依赖，依赖抽象）。
 *
 * <p>仅负责：入参校验、模型实例缓存、编排调用；不直接操作 Mapper、不写厂商构建逻辑。
 *
 * <p>方案一：链路信息自动从 OpenTelemetry Span Attributes 获取，
 * 调用方需通过 TraceUtil 设置属性后再调用。
 *
 * @author Ivan
 */
@Primary
@Component
public class AiModelChatModelProvider implements ChatModelProvider {

    private final LlmModelQueryProvider modelQueryProvider;
    @Resource
    private AiChatService aiChatService;
    @Resource
    private LlmLogRecorder llmLogRecorder;

    /**
     * 缓存 key = modelId + "@" + temperature，避免每次调用重建 ChatModel。
     */
    private final ConcurrentMap<String, ChatModel> cache = new ConcurrentHashMap<>();

    public AiModelChatModelProvider(LlmModelQueryProvider modelQueryProvider) {
        this.modelQueryProvider = modelQueryProvider;
    }

    @Override
    public ChatModel provide(LLMNodeData nodeData) {
        if (nodeData == null || nodeData.getModelId() == null) {
            throw new IllegalStateException("LLM 节点未指定模型 (model 为空)");
        }

        Long modelId = nodeData.getModelId();
        Double temperature = nodeData.getTemperature();
        String cacheKey = modelId + "@" + temperature;

        return cache.computeIfAbsent(cacheKey, k -> {
            LlmModelDTO modelDTO = modelQueryProvider.getModel(modelId);
            LlmModelConfigDTO config = new LlmModelConfigDTO();
            config.setTemperature(temperature);
            return LlmModelFactory.createChatModel(modelDTO, config);
        });
    }

    /**
     * 执行聊天并统一落库到 {@code ai_llm_call_log}。
     *
     * <p>链路信息自动从当前 OpenTelemetry Span Attributes 获取，
     * 调用方需在调用前通过 {@link TraceUtil} 设置属性。
     */
    @Override
    @TraceSpan(name = "llm.chat", kind = SpanKind.INTERNAL)
    public AiChatResponse chat(AiChatRequest request) {
        LocalDateTime startTime = LocalDateTime.now();
        LlmModelDTO modelDTO = null;
        if (request.getModelId() != null) {
            try {
                modelDTO = modelQueryProvider.getModel(request.getModelId());
                if (modelDTO != null) {
                    TraceUtil.setLabel(modelDTO.getModelName());
                    TraceUtil.setSpanAttribute(ATTR_MODEL_PROVIDER, modelDTO.getProvider());
                }
            } catch (Exception e) {
                // 查询模型信息失败不影响主流程，日志将不带模型详情
            }
        }
        try {
            AiChatResponse response = doChat(request);
            if (TraceUtil.hasActiveSpan()) {
                TraceUtil.setSpanAttribute(ATTR_TOKENS, response.getTotalTokens());
                newRecorder(startTime, modelDTO, request)
                    .output(response.getContent(), response.getInputTokens(), response.getOutputTokens())
                    .toolCalls(response.getToolCalls(), response.getFinishReason())
                    .success()
                    .record();
            }
            return response;
        } catch (RuntimeException e) {
            if (TraceUtil.hasActiveSpan()) {
                newRecorder(startTime, modelDTO, request)
                    .error(null, e.getMessage())
                    .record();
            }
            throw e;
        }
    }

    private AiChatResponse doChat(AiChatRequest request) {
        return aiChatService.chat(request.getMessages(), request.getModelId(),
            request.getConfig(), request.getTools());
    }

    @Override
    @TraceSpan(name = "llm.streamChat", kind = SpanKind.INTERNAL)
    public AiChatResponse streamChat(AiChatRequest request, java.util.function.Consumer<String> tokenCallback) {
        LocalDateTime startTime = LocalDateTime.now();
        LlmModelDTO modelDTO = null;
        if (request.getModelId() != null) {
            try {
                modelDTO = modelQueryProvider.getModel(request.getModelId());
                if (modelDTO != null) {
                    TraceUtil.setLabel(modelDTO.getModelName());
                    TraceUtil.setSpanAttribute(ATTR_MODEL_PROVIDER, modelDTO.getProvider());
                }
            } catch (Exception e) {
                // 查询模型信息失败不影响主流程，日志将不带模型详情
            }
        }
        try {
            AiChatResponse response = doStreamChat(request, tokenCallback);
            if (TraceUtil.hasActiveSpan()) {
                TraceUtil.setSpanAttribute(ATTR_TOKENS, response.getTotalTokens());
                newRecorder(startTime, modelDTO, request)
                    .output(response.getContent(), response.getInputTokens(), response.getOutputTokens())
                    .toolCalls(response.getToolCalls(), response.getFinishReason())
                    .success()
                    .record();
            }
            return response;
        } catch (RuntimeException e) {
            if (TraceUtil.hasActiveSpan()) {
                newRecorder(startTime, modelDTO, request)
                    .error(null, e.getMessage())
                    .record();
            }
            throw e;
        }
    }

    private AiChatResponse doStreamChat(AiChatRequest request, java.util.function.Consumer<String> tokenCallback) {
        return aiChatService.streamChat(request.getMessages(), request.getModelId(),
            request.getConfig(), request.getTools(), tokenCallback);
    }

    /** 从当前 Span Attributes 构造日志记录器。 */
    private LlmLogRecorder.LlmCallRecorder newRecorder(LocalDateTime startTime,
                                                        LlmModelDTO modelDTO,
                                                        AiChatRequest request) {
        return llmLogRecorder.recordLlmCall()
            .traceId(LlmLogRecorder.generateTraceId())
            .startTime(startTime)
            .source(TraceUtil.getCallSource(), TraceUtil.getSourceId(), TraceUtil.getSourceNodeId())
            .model(modelDTO)
            .config(request.getConfig())
            .inputMessages(request.getMessages())
            .toolDefinitions(request.getTools())
            .operator(TraceUtil.getOperatorId(), TraceUtil.getOrgId());
    }
}
