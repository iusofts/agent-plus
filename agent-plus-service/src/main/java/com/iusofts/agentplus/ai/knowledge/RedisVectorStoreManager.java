package com.iusofts.agentplus.ai.knowledge;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * @deprecated 已迁移到 {@link com.iusofts.agentplus.plugin.vectorstore.RedisVectorStoreManager}
 */
@Deprecated
@Component
public class RedisVectorStoreManager {

    private final com.iusofts.agentplus.plugin.vectorstore.RedisVectorStoreManager delegate;

    public RedisVectorStoreManager(com.iusofts.agentplus.plugin.vectorstore.RedisVectorStoreManager delegate) {
        this.delegate = delegate;
    }

    public EmbeddingStore<TextSegment> getStore(String collectionName) {
        return delegate.getStore(collectionName);
    }

    public void addAll(String collectionName, List<String> ids, List<Embedding> embeddings, List<TextSegment> segments) {
        delegate.addAll(collectionName, ids, embeddings, segments);
    }

    public void removeAll(String collectionName, Collection<String> vectorIds) {
        delegate.removeAll(collectionName, vectorIds);
    }

    public List<EmbeddingMatch<TextSegment>> search(String collectionName, Embedding query, int topK) {
        return delegate.search(collectionName, query, topK);
    }
}
