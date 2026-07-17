package com.iusofts.agentplus.ailog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 知识库检索统计项。
 *
 * @author Ivan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "知识库检索统计项")
public class KnowledgeRetrievalStatItem {

    @Schema(description = "知识库 ID")
    private Long knowledgeBaseId;

    @Schema(description = "知识库名称")
    private String knowledgeBaseName;

    @Schema(description = "总检索次数")
    private Long totalRetrievals;

    @Schema(description = "成功次数")
    private Long successRetrievals;

    @Schema(description = "召回文档总数")
    private Long totalRetrievedChunks;

    @Schema(description = "Embedding 消耗总 Token")
    private Long totalEmbeddingTokens;

    @Schema(description = "平均耗时（毫秒）")
    private BigDecimal avgDurationMs;
}
