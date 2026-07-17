package com.iusofts.agentplus.knowledge.dto;

import lombok.Data;

import java.util.Map;

/**
 * 知识库单条切片数据
 */
@Data
public class KnowledgeChunk {

    /**
     * 切片唯一ID
     */
    private Long chunkId;

    /**
     * 归属文档ID
     */
    private Long documentId;

    /**
     * 文档标题
     */
    private String title;

    /**
     * 切片原始正文内容（禁止节点内摘要删减）
     */
    private String content;

    /**
     * 相似度分数 0~1，一级字段，用于阈值过滤、排序
     */
    private Double score;

    /**
     * 文档访问地址
     */
    private String sourceUrl;

    /**
     * 扩展元数据：仅存放非通用业务字段
     */
    private Map<String, Object> metadata;

}