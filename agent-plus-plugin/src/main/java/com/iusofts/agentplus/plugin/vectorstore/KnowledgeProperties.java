package com.iusofts.agentplus.plugin.vectorstore;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 知识库管线配置(向量维度、Redis 索引前缀、文档处理线程池、超时补偿)。
 *
 * <p>向量库连接复用 {@code spring.data.redis.*},此处不再单列。</p>
 *
 * @author Ivan
 */
@Component
@ConfigurationProperties(prefix = "knowledge")
public class KnowledgeProperties {

    /** 向量维度,需与所选 embedding 模型输出一致(千问 text-embedding-v3 = 1024)。 */
    private int dimension = 1024;

    /** RedisEmbeddingStore 索引/键前缀,实际索引名 = indexPrefix + collectionName。 */
    private String indexPrefix = "kb:";

    /** 单个文档处理最长时长(秒),超过视为处理中超时,由定时任务重新入队。 */
    private long processTimeoutSeconds = 600;

    /** 文档处理线程池配置。 */
    private final Ingest ingest = new Ingest();

    public static class Ingest {
        /** 核心线程数(常驻)。 */
        private int corePoolSize = 2;
        /** 最大线程数。 */
        private int maxPoolSize = 4;
        /** 有界队列容量,用于并发限制与背压。 */
        private int queueCapacity = 200;
        /** 空闲线程存活秒数。 */
        private int keepAliveSeconds = 60;

        public int getCorePoolSize() {
            return corePoolSize;
        }

        public void setCorePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public int getMaxPoolSize() {
            return maxPoolSize;
        }

        public void setMaxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
        }

        public int getQueueCapacity() {
            return queueCapacity;
        }

        public void setQueueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
        }

        public int getKeepAliveSeconds() {
            return keepAliveSeconds;
        }

        public void setKeepAliveSeconds(int keepAliveSeconds) {
            this.keepAliveSeconds = keepAliveSeconds;
        }
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public String getIndexPrefix() {
        return indexPrefix;
    }

    public void setIndexPrefix(String indexPrefix) {
        this.indexPrefix = indexPrefix;
    }

    public long getProcessTimeoutSeconds() {
        return processTimeoutSeconds;
    }

    public void setProcessTimeoutSeconds(long processTimeoutSeconds) {
        this.processTimeoutSeconds = processTimeoutSeconds;
    }

    public Ingest getIngest() {
        return ingest;
    }
}
