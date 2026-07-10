package com.iusofts.agentplus.plugin.vectorstore;

import dev.langchain4j.community.store.embedding.redis.RedisEmbeddingStore;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.UnifiedJedis;

import jakarta.annotation.PreDestroy;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Redis 向量库管理器。
 *
 * <p>按知识库的 {@code collectionName} 缓存 {@link RedisEmbeddingStore}(每个集合独立索引),
 * 底层共享同一个 {@link UnifiedJedis} 连接(复用 {@code spring.data.redis.*} 配置)。
 * 向量检索天然按集合隔离,不同知识库互不干扰。</p>
 *
 * @author Ivan
 */
@Component
public class RedisVectorStoreManager {

    @Value("${spring.data.redis.host:127.0.0.1}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.database:0}")
    private int redisDatabase;

    private final KnowledgeProperties properties;

    /** key = collectionName。 */
    private final ConcurrentMap<String, EmbeddingStore<TextSegment>> stores = new ConcurrentHashMap<>();

    /** 共享的 Jedis 连接,懒初始化。 */
    private volatile UnifiedJedis jedis;

    /** 声明业务中所有会用到的元数据key */
    Set<String> allMetaKeys = Set.of("chunkId", "documentId", "title", "sourceUrl");

    public RedisVectorStoreManager(KnowledgeProperties properties) {
        this.properties = properties;
    }

    private UnifiedJedis jedis() {
        UnifiedJedis localJedis = this.jedis;
        if (localJedis == null) {
            synchronized (this) {
                localJedis = this.jedis;
                if (localJedis == null) {
                    DefaultJedisClientConfig.Builder cfg = DefaultJedisClientConfig.builder()
                            .database(redisDatabase);
                    if (StringUtils.hasText(redisPassword)) {
                        cfg.password(redisPassword);
                    }
                    JedisClientConfig clientConfig = cfg.build();
                    localJedis = new JedisPooled(new HostAndPort(redisHost, redisPort), clientConfig);
                    this.jedis = localJedis;
                }
            }
        }
        return localJedis;
    }

    /**
     * 获取(或创建)指定集合的向量库。
     *
     * @param collectionName 知识库集合名(ai_knowledge_base.collection_name)
     */
    public EmbeddingStore<TextSegment> getStore(String collectionName) {
        if (!StringUtils.hasText(collectionName)) {
            throw new IllegalStateException("知识库缺少 collectionName");
        }
        return stores.computeIfAbsent(collectionName, this::buildStore);
    }

    private EmbeddingStore<TextSegment> buildStore(String collectionName) {
        String indexName = properties.getIndexPrefix() + collectionName;
        return RedisEmbeddingStore.builder()
                .unifiedJedis(jedis())
                .indexName(indexName)
                .prefix(indexName + ":")
                .dimension(properties.getDimension())
                .metadataKeys(allMetaKeys)
                .build();
    }

    /**
     * 批量写入向量。
     *
     * @param collectionName 集合名
     * @param ids            向量 id(与 embeddings/segments 一一对应)
     * @param embeddings     向量
     * @param segments       文本段(含内容与元数据)
     */
    public void addAll(String collectionName, List<String> ids,
                       List<Embedding> embeddings, List<TextSegment> segments) {
        getStore(collectionName).addAll(ids, embeddings, segments);
    }

    /**
     * 按向量 id 批量删除。
     */
    public void removeAll(String collectionName, Collection<String> vectorIds) {
        if (vectorIds == null || vectorIds.isEmpty()) {
            return;
        }
        getStore(collectionName).removeAll(vectorIds);
    }

    /**
     * 相似度检索。
     *
     * @param collectionName 集合名
     * @param query          查询向量
     * @param topK           召回数量
     * @return 命中列表(相关度倒序)
     */
    public List<EmbeddingMatch<TextSegment>> search(String collectionName, Embedding query, int topK) {
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(query)
                .maxResults(topK)
                .build();
        EmbeddingSearchResult<TextSegment> result = getStore(collectionName).search(request);
        return result.matches();
    }

    @PreDestroy
    public void close() {
        UnifiedJedis localJedis = this.jedis;
        if (localJedis != null) {
            localJedis.close();
        }
    }
}
