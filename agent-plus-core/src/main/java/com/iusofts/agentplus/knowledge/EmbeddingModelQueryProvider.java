package com.iusofts.agentplus.knowledge;

import com.iusofts.agentplus.knowledge.dto.EmbeddingModelDTO;

/**
 * 嵌入模型配置查询接口（抽象，不感知数据库）。
 *
 * @author Ivan
 */
public interface EmbeddingModelQueryProvider {

    /**
     * 根据模型 ID 获取嵌入模型配置。
     *
     * @param modelId 模型 ID
     * @return 嵌入模型配置 DTO
     * @throws IllegalStateException 模型不存在或禁用时抛出
     */
    EmbeddingModelDTO getModel(Long modelId);
}
