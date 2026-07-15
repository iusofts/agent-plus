package com.iusofts.agentplus.aistat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 按操作类型维度的文档处理用量明细。
 *
 * @author Ivan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "按操作类型维度的文档处理用量明细")
public class DocOperationUsageItem {

    @Schema(description = "操作类型(ADD/UPDATE/DELETE)")
    private String operationType;

    @Schema(description = "总处理次数")
    private Long totalOps;

    @Schema(description = "成功次数")
    private Long successOps;

    @Schema(description = "失败次数")
    private Long failOps;

    @Schema(description = "总分块数")
    private Long totalChunks;

    @Schema(description = "总字符数")
    private Long totalCharCount;

    @Schema(description = "Embedding 消耗总 Token")
    private Long totalEmbeddingTokens;
}
