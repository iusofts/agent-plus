package com.iusofts.agentplus.engine.knowledge;

import com.iusofts.agentplus.knowledge.dto.KnowledgeRetrieveResult;

import java.util.Collections;

/**
 * 未接入向量库时的默认实现,始终返回空结果。
 *
 * @author Ivan
 */
public class NoopKnowledgeRetriever implements KnowledgeRetriever {

    @Override
    public KnowledgeRetrieveResult retrieve(Long knowledgeId, String query, int topK) {
        KnowledgeRetrieveResult result = new KnowledgeRetrieveResult();
        result.setSuccess(true);
        result.setQuery(query);
        result.setRewriteQuery(query);
        result.setChunks(Collections.emptyList());
        result.setContextText("");
        result.setTotalHit(0);
        result.setHasResult(false);
        return result;
    }
}
