package com.iusofts.agentplus.knowledge.dto;

import lombok.Data;
import java.util.List;

/**
 * 知识库检索节点 整体返回结果
 */
@Data
public class KnowledgeRetrieveResult {

    /**
     * 检索执行是否成功（区分业务无数据 vs 服务异常）
     */
    private Boolean success;

    /**
     * 用户原始查询词
     */
    private String query;

    /**
     * 经过改写/优化后，实际用于向量库检索的query
     */
    private String rewriteQuery;

    /**
     * 召回切片列表，按score降序排列
     */
    private List<KnowledgeChunk> chunks;

    /**
     * 预拼接好的上下文文本，直接供给LLM使用
     */
    private String contextText;

    /**
     * 召回切片总数
     */
    private Integer totalHit;

    /**
     * 是否存在有效检索结果 totalHit > 0
     */
    private Boolean hasResult;

    /**
     * 异常信息，success=false时填充
     */
    private String errorMessage;
}