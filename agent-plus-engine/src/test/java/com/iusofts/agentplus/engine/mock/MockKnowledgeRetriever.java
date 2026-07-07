package com.iusofts.agentplus.engine.mock;

import com.iusofts.agentplus.engine.knowledge.KnowledgeRetriever;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 测试用 {@link KnowledgeRetriever},不接任何真实向量库。
 *
 * <p>默认行为:返回 <code>topK</code> 条形如
 * <code>[mock kb:{knowledgeId}] chunk {i} for query: {query}</code> 的字符串。</p>
 *
 * <p>需要固定回复时通过构造函数传入 <code>Map&lt;knowledgeId, chunks&gt;</code>,
 * 检索时按 topK 截断该列表。</p>
 */
public class MockKnowledgeRetriever implements KnowledgeRetriever {

    private final Map<Long, List<String>> cannedResponses;

    public MockKnowledgeRetriever() {
        this(Collections.emptyMap());
    }

    public MockKnowledgeRetriever(Map<Long, List<String>> cannedResponses) {
        this.cannedResponses = cannedResponses == null ? Collections.emptyMap() : cannedResponses;
    }

    @Override
    public List<String> retrieve(Long knowledgeId, String query, int topK) {
        int limit = Math.max(1, topK);
        List<String> canned = cannedResponses.get(knowledgeId);
        if (canned != null && !canned.isEmpty()) {
            return canned.subList(0, Math.min(limit, canned.size()));
        }
        List<String> chunks = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
            chunks.add("[mock kb:" + knowledgeId + "] chunk " + i + " for query: " + query);
        }
        return chunks;
    }
}
