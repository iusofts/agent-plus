package com.iusofts.agentplus.plugin.llm;

import com.iusofts.agentplus.aiflow.vo.workflow.data.llm.LLMNodeData;
import com.iusofts.agentplus.ailog.dto.AiTraceContext;
import com.iusofts.agentplus.engine.llm.ChatModelProvider;
import com.iusofts.agentplus.llm.AiChatService;
import com.iusofts.agentplus.llm.dto.AiChatRequest;
import com.iusofts.agentplus.llm.dto.AiChatResponse;
import com.iusofts.agentplus.llm.dto.LlmModelConfigDTO;
import com.iusofts.agentplus.llm.dto.LlmModelDTO;
import com.iusofts.agentplus.llm.LlmModelQueryProvider;
import com.iusofts.agentplus.llm.log.LlmLogRecorder;
import dev.langchain4j.model.chat.ChatModel;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 基于数据库的 ChatModelProvider 实现（无 DB 依赖，依赖抽象）。
 *
 * <p>仅负责：入参校验、模型实例缓存、编排调用；不直接操作 Mapper、不写厂商构建逻辑。</p>
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

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        return aiChatService.chat(request.getMessages(), request.getModelId(),
                request.getConfig(), request.getTools());
    }

    /**
     * 执行聊天并统一落库到 {@code ai_llm_call_log}。
     *
     * <p>业务侧（聊天 / 流程 LLM 节点）不再手动记日志，只需构造 {@link AiTraceContext} 透传
     * traceId、来源与操作人；成功/失败均在此处记录，失败时补记 error 日志后原样抛出。</p>
     */
    @Override
    public AiChatResponse chat(AiChatRequest request, AiTraceContext ctx) {
        if (ctx == null) {
            return chat(request);
        }
        LocalDateTime startTime = LocalDateTime.now();
        LlmModelDTO modelDTO = null;
        if (request.getModelId() != null) {
            try {
                modelDTO = modelQueryProvider.getModel(request.getModelId());
            } catch (Exception e) {
                // 查询模型信息失败不影响主流程，日志将不带模型详情
            }
        }
        try {
            AiChatResponse response = chat(request);
            newRecorder(ctx, startTime, modelDTO, request)
                    .output(response.getContent(), response.getInputTokens(), response.getOutputTokens())
                    .toolCalls(response.getToolCalls(), response.getFinishReason())
                    .success()
                    .record();
            return response;
        } catch (RuntimeException e) {
            newRecorder(ctx, startTime, modelDTO, request)
                    .error(null, e.getMessage())
                    .record();
            throw e;
        }
    }

    /** 以调用上下文与请求构造一个已填充公共字段的日志记录器。 */
    private LlmLogRecorder.LlmCallRecorder newRecorder(AiTraceContext ctx, LocalDateTime startTime,
                                                       LlmModelDTO modelDTO, AiChatRequest request) {
        return llmLogRecorder.recordLlmCall()
                .traceId(ctx.getTraceId())
                .startTime(startTime)
                .source(ctx.getCallSource(), ctx.getSourceId(), ctx.getSourceNodeId())
                .model(modelDTO)
                .config(request.getConfig())
                .inputMessages(request.getMessages())
                .toolDefinitions(request.getTools())
                .operator(ctx.getOperatorId(), ctx.getOrgId());
    }
}
