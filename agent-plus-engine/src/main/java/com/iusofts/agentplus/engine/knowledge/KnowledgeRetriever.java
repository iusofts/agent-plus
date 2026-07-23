package com.iusofts.agentplus.engine.knowledge;

import com.iusofts.agentplus.knowledge.dto.KnowledgeRetrieveResult;
import com.iusofts.agentplus.llm.log.EmbeddingCallContext;

import java.util.List;

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

    /**
     * 从多个知识库中召回若干文档片段。
     *
     * @param knowledgeIds 知识库 id 列表
     * @param query        检索问句
     * @param topK         召回数量
     * @return 检索结果
     */
    KnowledgeRetrieveResult retrieve(List<Long> knowledgeIds, String query, int topK);

    /**
     * 带调用上下文的单知识库检索，供实现方将 embedding 调用落库到 {@code ai_llm_call_log}。
     * 默认忽略上下文，委托 {@link #retrieve(Long, String, int)}。
     */
    default KnowledgeRetrieveResult retrieve(Long knowledgeId, String query, int topK, EmbeddingCallContext ctx) {
        return retrieve(knowledgeId, query, topK);
    }

    /**
     * 带调用上下文的多知识库检索。默认忽略上下文，委托 {@link #retrieve(List, String, int)}。
     */
    default KnowledgeRetrieveResult retrieve(List<Long> knowledgeIds, String query, int topK, EmbeddingCallContext ctx) {
        return retrieve(knowledgeIds, query, topK);
    }
}

