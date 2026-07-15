package com.iusofts.agentplus.plugin.vectorstore;

import com.iusofts.agentplus.engine.knowledge.KnowledgeRetriever;
import com.iusofts.agentplus.knowledge.dto.KnowledgeBaseDTO;
import com.iusofts.agentplus.knowledge.dto.KnowledgeChunk;
import com.iusofts.agentplus.knowledge.dto.KnowledgeRetrieveResult;
import com.iusofts.agentplus.knowledge.KnowledgeBaseQueryProvider;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于 Redis 向量库的知识库检索实现（无 DB 依赖，依赖抽象）。
 *
 * @author Ivan
 */
@Primary
@Component
public class RedisKnowledgeRetriever implements KnowledgeRetriever {

    private static final Logger log = LoggerFactory.getLogger(RedisKnowledgeRetriever.class);

    private final KnowledgeBaseQueryProvider knowledgeBaseQueryProvider;
    private final EmbeddingModelProvider embeddingModelProvider;
    private final RedisVectorStoreManager vectorStoreManager;

    public RedisKnowledgeRetriever(
            KnowledgeBaseQueryProvider knowledgeBaseQueryProvider,
            EmbeddingModelProvider embeddingModelProvider,
            RedisVectorStoreManager vectorStoreManager) {
        this.knowledgeBaseQueryProvider = knowledgeBaseQueryProvider;
        this.embeddingModelProvider = embeddingModelProvider;
        this.vectorStoreManager = vectorStoreManager;
    }

    @Override
    public KnowledgeRetrieveResult retrieve(Long knowledgeId, String query, int topK) {
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

        try {
            KnowledgeBaseDTO kb = knowledgeBaseQueryProvider.getKnowledgeBase(knowledgeId);
            if (kb == null) {
                log.warn("知识库不存在: knowledgeId={}", knowledgeId);
                result.setSuccess(true);
                result.setChunks(List.of());
                result.setContextText("");
                result.setTotalHit(0);
                result.setHasResult(false);
                return result;
            }

            EmbeddingModel embeddingModel = embeddingModelProvider.provide(kb.getEmbeddingModelId());
            Response<Embedding> embeddingResponse = embeddingModel.embed(query);
            Embedding queryEmbedding = embeddingResponse.content();
            Integer embeddingTokens = embeddingResponse.tokenUsage() != null
                    ? embeddingResponse.tokenUsage().totalTokenCount()
                    : null;

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
        return result;
    }

    @Override
    public KnowledgeRetrieveResult retrieve(List<Long> knowledgeIds, String query, int topK) {
        if (knowledgeIds == null || knowledgeIds.isEmpty()) {
            return retrieve((Long) null, query, topK);
        }
        if (knowledgeIds.size() == 1) {
            return retrieve(knowledgeIds.get(0), query, topK);
        }
        // 多个知识库时，从每个知识库检索后合并
        int perKbK = Math.max(1, topK / knowledgeIds.size());
        List<KnowledgeChunk> allChunks = new ArrayList<>();
        Integer totalEmbeddingTokens = null;
        for (Long knowledgeId : knowledgeIds) {
            KnowledgeRetrieveResult singleResult = retrieve(knowledgeId, query, perKbK);
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
