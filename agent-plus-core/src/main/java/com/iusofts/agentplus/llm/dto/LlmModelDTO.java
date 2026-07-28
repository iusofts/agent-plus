package com.iusofts.agentplus.llm.dto;

import lombok.Data;

/**
 * LLM 模型配置 DTO（隔离数据库实体）。
 *
 * @author Ivan
 */
@Data
public class LlmModelDTO {

    /**
     * 模型 ID。
     */
    private Long id;

    /**
     * 模型提供商 (dashscope/volcengine/openai)。
     */
    private String provider;

    /**
     * API Key。
     */
    private String apiKey;

    /**
     * 基础 URL（兼容 OpenAI 接口）。
     */
    private String baseUrl;

    /**
     * 模型名称。
     */
    private String modelName;
}
