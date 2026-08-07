package com.iusofts.agentplus.plugin.vectorstore;

import com.iusofts.agentplus.engine.knowledge.KnowledgeRetriever;
import com.iusofts.agentplus.knowledge.EmbeddingModelQueryProvider;
import com.iusofts.agentplus.knowledge.KnowledgeBaseQueryProvider;
import com.iusofts.agentplus.knowledge.dto.EmbeddingModelDTO;
import com.iusofts.agentplus.knowledge.dto.KnowledgeBaseDTO;
import com.iusofts.agentplus.knowledge.dto.KnowledgeChunk;
import com.iusofts.agentplus.knowledge.dto.KnowledgeRetrieveResult;
import com.iusofts.agentplus.llm.log.LlmLogRecorder;
import com.iusofts.agentplus.trace.TraceUtil;
import com.iusofts.agentplus.trace.annotation.TraceSpan;
import com.iusofts.agentplus.trace.constants.CallSource;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import io.opentelemetry.api.trace.SpanKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.iusofts.agentplus.trace.constants.TraceConstant.ATTR_MODEL_NAME;
import static com.iusofts.agentplus.trace.constants.TraceConstant.ATTR_TOKENS;


/**
 * 基于 Redis 向量库的知识库检索实现（无 DB 依赖，依赖抽象）。
 *
 * <p>方案一：链路信息自动从 OpenTelemetry Span Attributes 获取，
 * 检索时的 query 向量化调用按 {@code EMBED_RETRIEVE} 来源记录到 {@code ai_llm_call_log}；
 * 每次单库检索另记一条 {@code ai_knowledge_retrieval_log}（含召回明细与 topK）。
 * 多知识库场景逐库各记一条。
 *
 * @author Ivan
 */
@Primary
@Component
public class RedisKnowledgeRetriever implements KnowledgeRetriever {

    private static final Logger log = LoggerFactory.getLogger(RedisKnowledgeRetriever.class);

    private final KnowledgeBaseQueryProvider knowledgeBaseQueryProvider;
    private final EmbeddingModelProvider embeddingModelProvider;
    private final EmbeddingModelQueryProvider embeddingModelQueryProvider;
    private final RedisVectorStoreManager vectorStoreManager;
    private final ObjectProvider<LlmLogRecorder> llmLogRecorderProvider;

    @Lazy
    @Autowired
    private KnowledgeRetriever self;

    public RedisKnowledgeRetriever(
        KnowledgeBaseQueryProvider knowledgeBaseQueryProvider,
        EmbeddingModelProvider embeddingModelProvider,
        EmbeddingModelQueryProvider embeddingModelQueryProvider,
        RedisVectorStoreManager vectorStoreManager,
        ObjectProvider<LlmLogRecorder> llmLogRecorderProvider) {
        this.knowledgeBaseQueryProvider = knowledgeBaseQueryProvider;
        this.embeddingModelProvider = embeddingModelProvider;
        this.embeddingModelQueryProvider = embeddingModelQueryProvider;
        this.vectorStoreManager = vectorStoreManager;
        this.llmLogRecorderProvider = llmLogRecorderProvider;
    }

    @Override
    @TraceSpan(name = "knowledge.retrieve", kind = SpanKind.INTERNAL)
    public KnowledgeRetrieveResult retrieve(Long knowledgeId, String query, int topK) {
        LocalDateTime retrieveStart = LocalDateTime.now();
        KnowledgeRetrieveResult result = new KnowledgeRetrieveResult();
        result.setQuery(query);
        result.setRewriteQuery(query);

        if (knowledgeId == null || !StringUtils.hasText(query)) {
            result.setSuccess(true);
            result.setChunks(List.of());
            result.setContextText("");
            result.setTotalHit(0);
            result.setHasResult(false);
            return result;
        }

        KnowledgeBaseDTO kb = null;
        try {
            kb = knowledgeBaseQueryProvider.getKnowledgeBase(knowledgeId);
            if (kb == null) {
                log.warn("知识库不存在: knowledgeId={}", knowledgeId);
                result.setSuccess(true);
                result.setChunks(List.of());
                result.setContextText("");
                result.setTotalHit(0);
                result.setHasResult(false);
                return result;
            }

            TraceUtil.setLabel(kb.getName());

            EmbeddingModelDTO modelDTO = null;
            try {
                modelDTO = embeddingModelQueryProvider.getModel(kb.getEmbeddingModelId());
                if (modelDTO != null) {
                    TraceUtil.setSpanAttribute(ATTR_MODEL_NAME, modelDTO.getModelName());
                }
            } catch (Exception e) {
                log.debug("查询嵌入模型信息失败,日志将不带模型详情", e);
            }

            EmbeddingModel embeddingModel = embeddingModelProvider.provide(kb.getEmbeddingModelId());
            Response<Embedding> embeddingResponse = embedQueryWithLog(embeddingModel, kb, modelDTO, query);
            Embedding queryEmbedding = embeddingResponse.content();
            Integer embeddingTokens = embeddingResponse.tokenUsage() != null
                ? embeddingResponse.tokenUsage().totalTokenCount()
                : null;

            TraceUtil.setSpanAttribute(ATTR_TOKENS, embeddingTokens);

            int limit = topK > 0 ? topK : 3;
            List<EmbeddingMatch<TextSegment>> matches =
                vectorStoreManager.search(kb.getCollectionName(), queryEmbedding, limit);

            List<KnowledgeChunk> chunks = new ArrayList<>();
            for (int i = 0; i < matches.size(); i++) {
                EmbeddingMatch<TextSegment> match = matches.get(i);
                TextSegment segment = match.embedded();
                KnowledgeChunk chunk = new KnowledgeChunk();

                // 从 metadata 中读取通用字段
                KnowledgeMetadata.populate(segment.metadata(), chunk);
                chunk.setContent(segment.text());
                chunk.setScore(match.score());

                chunks.add(chunk);
            }

            String contextText = chunks.stream()
                .map(KnowledgeChunk::getContent)
                .collect(Collectors.joining("\n\n"));

            result.setSuccess(true);
            result.setChunks(chunks);
            result.setContextText(contextText);
            result.setTotalHit(chunks.size());
            result.setEmbeddingTokens(embeddingTokens);
            result.setHasResult(!chunks.isEmpty());
        } catch (Exception e) {
            log.error("知识库检索失败: knowledgeId={}, query={}", knowledgeId, query, e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            result.setChunks(List.of());
            result.setContextText("");
            result.setTotalHit(0);
            result.setHasResult(false);
        }
        recordRetrievalCall(knowledgeId, kb, query, topK, result, retrieveStart);
        return result;
    }

    /**
     * 记录一次检索日志到 {@code ai_knowledge_retrieval_log}。
     * 无 active span 或无记录器时静默跳过。
     * 多知识库场景由上层循环调用单库检索，此处每库各记一条,召回明细精确到单库。
     */
    private void recordRetrievalCall(Long knowledgeId, KnowledgeBaseDTO kb, String query, int topK,
                                     KnowledgeRetrieveResult result, LocalDateTime start) {
        if (!TraceUtil.hasActiveSpan()) {
            return;
        }
        LlmLogRecorder recorder = llmLogRecorderProvider.getIfAvailable();
        if (recorder == null) {
            return;
        }
        try {
            String kbName = kb != null ? kb.getName() : null;
            LlmLogRecorder.KnowledgeRetrievalRecorder call = recorder.recordKnowledgeRetrieval()
                .traceId(TraceUtil.currentTraceId())
                .startTime(start)
                .source(CallSource.fromCode(TraceUtil.getCallSource()), TraceUtil.getSourceId(), TraceUtil.getSourceNodeId())
                .knowledgeBase(knowledgeId, kbName)
                .query(query)
                .topK(topK)
                .operator(TraceUtil.getOperatorId(), TraceUtil.getOrgId());
            if (result != null && Boolean.TRUE.equals(result.getSuccess())) {
                call.retrievedResult(result).success();
            } else {
                call.error(result != null ? result.getErrorMessage() : "检索失败");
            }
            call.record();
        } catch (Exception e) {
            log.warn("记录知识库检索日志失败: knowledgeId={}", knowledgeId, e);
        }
    }

    /**
     * 执行 query 向量化，并把这次 embedding 调用落库到 {@code ai_llm_call_log}。
     */
    private Response<Embedding> embedQueryWithLog(EmbeddingModel embeddingModel, KnowledgeBaseDTO kb, EmbeddingModelDTO modelDTO,
                                                  String query) {
        LocalDateTime start = LocalDateTime.now();
        Response<Embedding> embeddingResponse;
        try {
            embeddingResponse = embeddingModel.embed(query);
        } catch (RuntimeException e) {
            recordEmbeddingCall(kb, modelDTO, query, null, start, e.getMessage());
            throw e;
        }
        recordEmbeddingCall(kb, modelDTO, query, embeddingResponse.tokenUsage(), start, null);
        return embeddingResponse;
    }

    /** 记录一次检索场景的 embedding 调用日志。无 active span 或无记录器时静默跳过。 */
    private void recordEmbeddingCall(KnowledgeBaseDTO kb, EmbeddingModelDTO modelDTO, String query,
                                     dev.langchain4j.model.output.TokenUsage tokenUsage,
                                     LocalDateTime start, String errorMessage) {
        if (!TraceUtil.hasActiveSpan()) {
            return;
        }
        LlmLogRecorder recorder = llmLogRecorderProvider.getIfAvailable();
        if (recorder == null) {
            return;
        }
        try {
            
            LlmLogRecorder.LlmCallRecorder call = recorder.recordLlmCall()
                .traceId(TraceUtil.currentTraceId())
                .startTime(start)
                .sourceFromTrace()
                .embeddingModel(modelDTO)
                .inputContent(query)
                .operator(TraceUtil.getOperatorId(), TraceUtil.getOrgId());
            if (errorMessage != null) {
                call.error(null, errorMessage);
            } else {
                Integer inputTokens = tokenUsage != null ? tokenUsage.inputTokenCount() : null;
                Integer outputTokens = tokenUsage != null ? tokenUsage.outputTokenCount() : null;
                call.output(null, inputTokens, outputTokens).success();
            }
            call.record();
        } catch (Exception e) {
            log.warn("记录检索 embedding 调用日志失败: knowledgeId={}", kb.getId(), e);
        }
    }

    @Override
    @TraceSpan(name = "knowledge.multi_retrieve", label = "多库召回", kind = SpanKind.INTERNAL)
    public KnowledgeRetrieveResult retrieve(List<Long> knowledgeIds, String query, int topK) {
        if (knowledgeIds == null || knowledgeIds.isEmpty()) {
            return self.retrieve((Long) null, query, topK);
        }
        if (knowledgeIds.size() == 1) {
            return self.retrieve(knowledgeIds.get(0), query, topK);
        }
        // 多个知识库时，从每个知识库检索后合并
        int perKbK = Math.max(1, topK / knowledgeIds.size());
        List<KnowledgeChunk> allChunks = new ArrayList<>();
        Integer totalEmbeddingTokens = null;
        for (Long knowledgeId : knowledgeIds) {
            KnowledgeRetrieveResult singleResult = self.retrieve(knowledgeId, query, perKbK);
            if (singleResult.getChunks() != null) {
                allChunks.addAll(singleResult.getChunks());
            }
            if (singleResult.getEmbeddingTokens() != null) {
                totalEmbeddingTokens = (totalEmbeddingTokens == null ? 0 : totalEmbeddingTokens) + singleResult.getEmbeddingTokens();
            }
        }
        // 按分数排序并截取 topK
        allChunks.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        if (allChunks.size() > topK) {
            allChunks = allChunks.subList(0, topK);
        }
        String contextText = allChunks.stream()
            .map(KnowledgeChunk::getContent)
            .collect(Collectors.joining("\n\n"));

        KnowledgeRetrieveResult result = new KnowledgeRetrieveResult();
        result.setSuccess(true);
        result.setQuery(query);
        result.setRewriteQuery(query);
        result.setChunks(allChunks);
        result.setContextText(contextText);
        result.setTotalHit(allChunks.size());
        result.setEmbeddingTokens(totalEmbeddingTokens);
        result.setHasResult(!allChunks.isEmpty());
        return result;
    }
}
