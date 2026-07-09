package com.iusofts.agentplus.llm.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识库文档处理日志事件。
 *
 * @author Ivan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeDocLogEvent {

    /**
     * 知识库 ID。
     */
    private Long knowledgeBaseId;

    /**
     * 知识库名称。
     */
    private String knowledgeBaseName;

    /**
     * 文档 ID。
     */
    private Long docId;

    /**
     * 文档名称。
     */
    private String docName;

    /**
     * 操作类型：ADD/UPDATE/DELETE。
     */
    private String operationType;

    /**
     * 分块数量。
     */
    private Integer chunkCount;

    /**
     * 总字符数。
     */
    private Integer totalCharCount;

    /**
     * Embedding 总消耗 Token 数。
     */
    private Integer totalEmbeddingTokens;

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
