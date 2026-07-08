package com.iusofts.agentplus.knowledge;

/**
 * 知识库配置查询接口（抽象，不感知数据库）。
 *
 * @author Ivan
 */
public interface KnowledgeBaseQueryProvider {

    /**
     * 根据知识库 ID 获取知识库配置。
     *
     * @param knowledgeBaseId 知识库 ID
     * @return 知识库配置 DTO
     */
    KnowledgeBaseDTO getKnowledgeBase(Long knowledgeBaseId);
}
