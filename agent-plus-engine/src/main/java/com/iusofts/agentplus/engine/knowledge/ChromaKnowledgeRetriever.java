package com.iusofts.agentplus.engine.knowledge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 基于 Chroma 的知识库检索实现（占位符）。
 *
 * <p>当前版本返回空列表，后续完善 Chroma 集成。</p>
 *
 * @author Ivan
 */
public class ChromaKnowledgeRetriever implements KnowledgeRetriever {

    private static final Logger log = LoggerFactory.getLogger(ChromaKnowledgeRetriever.class);

    private final ChromaProperties chromaProperties;

    public ChromaKnowledgeRetriever(ChromaProperties chromaProperties) {
        this.chromaProperties = chromaProperties;
    }

    @Override
    public List<String> retrieve(Long knowledgeId, String query, int topK) {
        log.info("Chroma 检索调用: knowledgeId={}, query={}, topK={}", knowledgeId, query, topK);
        // 当前返回空列表，后续实现真正的 Chroma 集成
        return List.of();
    }
}
