package com.iusofts.agentplus.ai.knowledge;

import org.springframework.stereotype.Component;

/**
 * @deprecated 已迁移到 {@link com.iusofts.agentplus.plugin.vectorstore.KnowledgeProperties}
 */
@Deprecated
@Component
public class KnowledgeProperties {

    private final com.iusofts.agentplus.plugin.vectorstore.KnowledgeProperties delegate;

    public KnowledgeProperties(com.iusofts.agentplus.plugin.vectorstore.KnowledgeProperties delegate) {
        this.delegate = delegate;
    }

    public int getDimension() {
        return delegate.getDimension();
    }

    public void setDimension(int dimension) {
        delegate.setDimension(dimension);
    }

    public String getIndexPrefix() {
        return delegate.getIndexPrefix();
    }

    public void setIndexPrefix(String indexPrefix) {
        delegate.setIndexPrefix(indexPrefix);
    }

    public long getProcessTimeoutSeconds() {
        return delegate.getProcessTimeoutSeconds();
    }

    public void setProcessTimeoutSeconds(long processTimeoutSeconds) {
        delegate.setProcessTimeoutSeconds(processTimeoutSeconds);
    }

    public com.iusofts.agentplus.plugin.vectorstore.KnowledgeProperties.Ingest getIngest() {
        return delegate.getIngest();
    }
}
