package com.iusofts.agentplus.plugin.vectorstore;

import com.iusofts.agentplus.knowledge.dto.EmbeddingModelDTO;
import com.iusofts.agentplus.knowledge.EmbeddingModelQueryProvider;
import com.iusofts.agentplus.llm.log.LlmLogRecorder;
import com.iusofts.agentplus.trace.TraceUtil;
import com.iusofts.agentplus.trace.constants.CallSource;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 知识库存储服务（封装向量化、存储逻辑，service 层不直接依赖 langchain4j）。
 *
 * <p>方案一：链路信息自动从 OpenTelemetry Span Attributes 获取，
 * 调用方需在调用前通过 {@link TraceUtil} 设置属性。
 *
 * @author Ivan
 */
@Slf4j
@Component
public class KnowledgeStoreService {

    private static final int EMBED_BATCH_SIZE = 20;

    private final EmbeddingModelQueryProvider embeddingModelQueryProvider;
    private final RedisVectorStoreManager vectorStoreManager;
    private final ObjectProvider<LlmLogRecorder> llmLogRecorderProvider;

    public KnowledgeStoreService(
        EmbeddingModelQueryProvider embeddingModelQueryProvider,
        RedisVectorStoreManager vectorStoreManager,
        ObjectProvider<LlmLogRecorder> llmLogRecorderProvider) {
        this.embeddingModelQueryProvider = embeddingModelQueryProvider;
        this.vectorStoreManager = vectorStoreManager;
        this.llmLogRecorderProvider = llmLogRecorderProvider;
    }

    /**
     * 分批次向量化并存储，自动记录日志。
     *
     * <p>链路信息自动从当前 OpenTelemetry Span 获取。
     *
     * @param collectionName 集合名称
     * @param vectorIds      向量 ID 列表（与 chunkTexts 一一对应）
     * @param chunkTexts     分块文本列表
     * @param chunkMetadatas 分块元数据列表（与 chunkTexts 一一对应）
     * @param embeddingModelId 嵌入模型 ID
     * @param knowledgeBaseId 知识库 ID（用于日志记录）
     * @return 向量化累计消耗的 token 数（部分模型不返回用量时为 0）
     */
    public int batchEmbedAndStore(
        String collectionName,
        List<String> vectorIds,
        List<String> chunkTexts,
        List<Map<String, Object>> chunkMetadatas,
        Long embeddingModelId,
        Long knowledgeBaseId) {

        EmbeddingModelDTO embeddingModelDTO = embeddingModelQueryProvider.getModel(embeddingModelId);
        EmbeddingModel embeddingModel = EmbeddingModelFactory.createEmbeddingModel(embeddingModelDTO);

        int total = chunkTexts.size();
        int totalEmbeddingTokens = 0;

        for (int from = 0; from < total; from += EMBED_BATCH_SIZE) {
            int to = Math.min(from + EMBED_BATCH_SIZE, total);
            List<String> batchTexts = chunkTexts.subList(from, to);
            List<String> batchVectorIds = vectorIds.subList(from, to);
            List<Map<String, Object>> batchMetadatas = chunkMetadatas.subList(from, to);

            List<TextSegment> segments = new ArrayList<>(batchTexts.size());
            for (int i = 0; i < batchTexts.size(); i++) {
                String content = batchTexts.get(i);
                Map<String, Object> metadataMap = batchMetadatas.get(i);
                segments.add(TextSegment.from(content, Metadata.from(metadataMap)));
            }

            LocalDateTime startTime = LocalDateTime.now();
            Response<List<Embedding>> response;
            try {
                response = embeddingModel.embedAll(segments);
            } catch (RuntimeException e) {
                recordEmbeddingCall(embeddingModelDTO, knowledgeBaseId, startTime,
                    batchTexts, null, e.getMessage());
                throw e;
            }

            List<Embedding> embeddings = response.content();
            dev.langchain4j.model.output.TokenUsage tokenUsage = response.tokenUsage();
            if (tokenUsage != null && tokenUsage.totalTokenCount() != null) {
                totalEmbeddingTokens += tokenUsage.totalTokenCount();
            }

            recordEmbeddingCall(embeddingModelDTO, knowledgeBaseId, startTime,
                batchTexts, tokenUsage, null);

            vectorStoreManager.addAll(collectionName, batchVectorIds, embeddings, segments);
        }

        return totalEmbeddingTokens;
    }

    /**
     * 记录一次索引场景的 embedding 调用日志。无 active span 或无记录器时静默跳过。
     */
    private void recordEmbeddingCall(
        EmbeddingModelDTO modelDTO,
        Long knowledgeBaseId,
        LocalDateTime startTime,
        List<String> batchTexts,
        dev.langchain4j.model.output.TokenUsage tokenUsage,
        String errorMessage) {
        // 如果没有 active span，也没有知识库 ID，则不记录日志
        if (!TraceUtil.hasActiveSpan() && knowledgeBaseId == null) {
            return;
        }
        LlmLogRecorder recorder = llmLogRecorderProvider.getIfAvailable();
        if (recorder == null) {
            return;
        }
        try {
            String inputContent = batchTexts.stream().filter(c -> c != null).collect(Collectors.joining("\n"));

            LlmLogRecorder.LlmCallRecorder call = recorder.recordLlmCall()
                .traceId(LlmLogRecorder.generateTraceId())
                .startTime(startTime)
                .embeddingModel(modelDTO)
                .inputContent(inputContent);

            // 设置来源信息
            call.source(CallSource.EMBED_INDEX, knowledgeBaseId, TraceUtil.getSourceFlowId(), TraceUtil.getSourceNodeId());
            // 设置操作人信息
            call.operator(TraceUtil.getOperatorId(), TraceUtil.getOrgId());

            // 设置成功或失败状态
            if (errorMessage != null) {
                call.error(null, errorMessage);
            } else {
                Integer inputTokens = tokenUsage != null ? tokenUsage.inputTokenCount() : null;
                Integer outputTokens = tokenUsage != null ? tokenUsage.outputTokenCount() : null;
                call.output(null, inputTokens, outputTokens).success();
            }

            call.record();
        } catch (Exception e) {
            log.warn("记录索引 embedding 调用日志失败: knowledgeBaseId={}", knowledgeBaseId, e);
        }
    }
}
