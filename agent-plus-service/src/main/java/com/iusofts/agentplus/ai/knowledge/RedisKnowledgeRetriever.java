package com.iusofts.agentplus.ai.knowledge;

import com.iusofts.agentplus.ai.entity.AiKnowledgeBase;
import com.iusofts.agentplus.ai.mapper.AiKnowledgeBaseMapper;
import com.iusofts.agentplus.engine.knowledge.KnowledgeRetriever;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 基于 Redis 向量库的知识库检索实现。
 *
 * <p>覆盖引擎模块默认的 {@code NoopKnowledgeRetriever}({@code @Primary})。
 * 检索流程:查知识库配置 -> 用其嵌入模型把 query 向量化 -> 在对应集合做相似度检索 -> 返回分块文本。</p>
 *
 * @author Ivan
 */
@Primary
@Component
public class RedisKnowledgeRetriever implements KnowledgeRetriever {

    private static final Logger log = LoggerFactory.getLogger(RedisKnowledgeRetriever.class);

    @Resource
    private AiKnowledgeBaseMapper knowledgeBaseMapper;

    @Resource
    private EmbeddingModelProvider embeddingModelProvider;

    @Resource
    private RedisVectorStoreManager vectorStoreManager;

    @Override
    public List<String> retrieve(Long knowledgeId, String query, int topK) {
        if (knowledgeId == null || !StringUtils.hasText(query)) {
            return List.of();
        }
        try {
            AiKnowledgeBase kb = knowledgeBaseMapper.selectById(knowledgeId);
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
