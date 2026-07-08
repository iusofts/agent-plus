package com.iusofts.agentplus.knowledge.dto;

import lombok.Data;

/**
 * 嵌入模型配置 DTO（隔离数据库实体）。
 *
 * @author Ivan
 */
@Data
public class EmbeddingModelDTO {

    /**
     * 模型 ID。
     */
    private Long id;

    /**
     * 模型提供商 (qwen/doubao/openai)。
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
