package com.iusofts.agentplus.engine.knowledge;

import com.iusofts.agentplus.knowledge.dto.KnowledgeRetrieveResult;

/**
 * 知识库检索扩展点。
 *
 * <p>引擎自身不绑定具体向量库,业务侧需要实现本接口并注入到
 * {@link com.iusofts.agentplus.engine.WorkflowEngine} 中。</p>
 *
 * @author Ivan
 */
public interface KnowledgeRetriever {

    /**
     * 从指定知识库中召回若干文档片段。
     *
     * @param knowledgeId 知识库 id
     * @param query       检索问句
     * @param topK        召回数量
     * @return 检索结果
     */
    KnowledgeRetrieveResult retrieve(Long knowledgeId, String query, int topK);
}
