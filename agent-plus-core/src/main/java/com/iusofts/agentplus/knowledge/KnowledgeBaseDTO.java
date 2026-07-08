package com.iusofts.agentplus.knowledge;

import lombok.Data;

/**
 * 知识库配置 DTO（隔离数据库实体）。
 *
 * @author Ivan
 */
@Data
public class KnowledgeBaseDTO {

    /**
     * 知识库 ID。
     */
    private Long id;

    /**
     * 向量集合名称。
     */
    private String collectionName;

    /**
     * 嵌入模型 ID。
     */
    private Long embeddingModelId;
}
