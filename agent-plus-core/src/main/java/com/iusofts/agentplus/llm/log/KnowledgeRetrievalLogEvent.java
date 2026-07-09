package com.iusofts.agentplus.llm.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 知识库检索日志事件。
 *
 * @author Ivan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeRetrievalLogEvent {

    /**
     * 链路追踪 ID。
     */
    private String traceId;

    /**
     * 调用来源：AGENT/CHAT/FLOW/API。
     */
    private String callSource;

    /**
     * 来源 ID：智能体 ID/会话 ID/流程 ID。
     */
    private Long sourceId;

    /**
     * 知识库 ID。
     */
    private Long knowledgeBaseId;

    /**
     * 知识库名称。
     */
    private String knowledgeBaseName;

    /**
     * 查询内容。
     */
    private String query;

    /**
     * 查询字符数。
     */
    private Integer queryCharCount;

    /**
     * 查询向量化消耗 Token 数。
     */
    private Integer queryEmbeddingTokens;

    /**
     * 召回数量。
     */
    private Integer topK;

    /**
     * 召回的文档片段列表（简化记录）。
     */
    private List<String> retrievedChunks;

    /**
     * 实际召回数量。
     */
    private Integer retrievedCount;

    /**
     * 是否成功。
     */
    private Boolean success;

    /**
     * 错误信息。
     */
    private String errorMessage;

    /**
     * 调用耗时（毫秒）。
     */
    private Integer durationMs;

    /**
     * 操作人 ID。
     */
    private Long operatorId;

    /**
     * 组织 ID。
     */
    private Integer orgId;
}
