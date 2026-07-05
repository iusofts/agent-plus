package com.iusofts.agentplus.engine.knowledge;

import java.util.Collections;
import java.util.List;

/**
 * 未接入向量库时的默认实现,始终返回空列表。
 *
 * @author Ivan
 */
public class NoopKnowledgeRetriever implements KnowledgeRetriever {

    @Override
    public List<String> retrieve(Long knowledgeId, String query, int topK) {
        return Collections.emptyList();
    }
}
