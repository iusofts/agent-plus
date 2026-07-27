package com.iusofts.agentplus.engine.knowledge;

import com.iusofts.agentplus.knowledge.dto.KnowledgeRetrieveResult;

import java.util.List;

/**
 * 知识库检索扩展点。
 *
 * <p>引擎自身不绑定具体向量库，业务侧需要实现本接口并注入到
 * {@link com.iusofts.agentplus.engine.WorkflowEngine} 中。
 *
 * <p>方案一：链路信息通过 OpenTelemetry Span Attributes 传递，
 * 调用方需在调用前通过 {@link com.iusofts.agentplus.trace.TraceUtil} 设置属性。
 *
 * @author Ivan
 */
public interface KnowledgeRetriever {

    /**
     * 从指定知识库中召回若干文档片段，并自动记录日志。
     *
     * <p>链路信息（traceId、来源、操作人）自动从当前 OpenTelemetry Span 获取。
     *
     * @param knowledgeId 知识库 id
     * @param query       检索问句
     * @param topK        召回数量
     * @return 检索结果
     */
    KnowledgeRetrieveResult retrieve(Long knowledgeId, String query, int topK);

    /**
     * 从多个知识库中召回若干文档片段，并自动记录日志。
     *
     * <p>链路信息（traceId、来源、操作人）自动从当前 OpenTelemetry Span 获取。
     *
     * @param knowledgeIds 知识库 id 列表
     * @param query        检索问句
     * @param topK         召回数量
     * @return 检索结果
     */
    KnowledgeRetrieveResult retrieve(List<Long> knowledgeIds, String query, int topK);
}
