package com.iusofts.agentplus.ai.knowledge;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @deprecated 已迁移到 {@link com.iusofts.agentplus.plugin.document.TextChunker}
 */
@Deprecated
@Component
public class TextChunker {

    private final com.iusofts.agentplus.plugin.document.TextChunker delegate;

    public TextChunker(com.iusofts.agentplus.plugin.document.TextChunker delegate) {
        this.delegate = delegate;
    }

    public List<String> split(String text, int chunkSize, int chunkOverlap) {
        return delegate.split(text, chunkSize, chunkOverlap);
    }
}
