package com.iusofts.agentplus.engine.knowledge;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Chroma 向量数据库配置。
 *
 * @author Ivan
 */
@ConfigurationProperties(prefix = "chroma")
public class ChromaProperties {

    /**
     * 是否启用 Chroma.
     */
    private boolean enabled = false;

    /**
     * Chroma 服务地址.
     * 默认: http://localhost:8000
     */
    private String url = "http://localhost:8000";

    /**
     * 集合名称前缀.
     */
    private String collectionPrefix = "kb_";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCollectionPrefix() {
        return collectionPrefix;
    }

    public void setCollectionPrefix(String collectionPrefix) {
        this.collectionPrefix = collectionPrefix;
    }
}
