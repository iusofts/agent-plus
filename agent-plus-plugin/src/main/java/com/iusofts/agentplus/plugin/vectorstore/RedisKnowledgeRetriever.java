package com.iusofts.agentplus.plugin.vectorstore;

import com.iusofts.agentplus.engine.knowledge.KnowledgeRetriever;
import com.iusofts.agentplus.knowledge.KnowledgeBaseDTO;
import com.iusofts.agentplus.knowledge.KnowledgeBaseQueryProvider;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

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
    public List<String> retrieve(Long knowledgeId, String query, int topK) {
        if (knowledgeId == null || !StringUtils.hasText(query)) {
            return List.of();
        }

        try {
            KnowledgeBaseDTO kb = knowledgeBaseQueryProvider.getKnowledgeBase(knowledgeId);
            if (kb == null) {
                log.warn("知识库不存在: knowledgeId={}", knowledgeId);
                return List.of();
            }

            EmbeddingModel embeddingModel = embeddingModelProvider.provide(kb.getEmbeddingModelId());
            Embedding queryEmbedding = embeddingModel.embed(query).content();

            int limit = topK > 0 ? topK : 3;
            List<EmbeddingMatch<TextSegment>> matches =
                    vectorStoreManager.search(kb.getCollectionName(), queryEmbedding, limit);

            return matches.stream()
                    .map(m -> m.embedded().text())
                    .toList();
        } catch (Exception e) {
            log.error("知识库检索失败: knowledgeId={}, query={}", knowledgeId, query, e);
            return List.of();
        }
    }
}
