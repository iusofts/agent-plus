package com.iusofts.agentplus.llm;

/**
 * LLM 模型配置查询接口（抽象，不感知数据库）。
 *
 * @author Ivan
 */
public interface LlmModelQueryProvider {

    /**
     * 根据模型 ID 获取模型配置。
     *
     * @param modelId 模型 ID
     * @return 模型配置 DTO
     * @throws IllegalStateException 模型不存在或禁用时抛出
     */
    LlmModelDTO getModel(Long modelId);

    /**
     * 获取默认模型 ID。
     *
     * @return 默认模型 ID
     * @throws IllegalStateException 无默认模型时抛出
     */
    Long getDefaultModelId();
}
