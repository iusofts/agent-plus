package com.iusofts.agentplus.ai.knowledge;

import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @deprecated 已迁移到 {@link com.iusofts.agentplus.plugin.document.DocumentContentExtractor}
 */
@Deprecated
@Component
public class DocumentContentExtractor {

    private final com.iusofts.agentplus.plugin.document.DocumentContentExtractor delegate;

    public DocumentContentExtractor(com.iusofts.agentplus.plugin.document.DocumentContentExtractor delegate) {
        this.delegate = delegate;
    }

    public String extract(String docUrl) throws IOException, TikaException {
        return delegate.extract(docUrl);
    }
}
