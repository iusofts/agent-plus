package com.iusofts.agentplus.aistat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * AI 用量总览(三类日志汇总)。
 *
 * @author Ivan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI 用量总览")
public class AiUsageOverviewVo {

    // ===== LLM 调用 =====
    @Schema(description = "LLM 总调用次数")
    private Long llmTotalCalls;

    @Schema(description = "LLM 成功次数")
    private Long llmSuccessCalls;

    @Schema(description = "LLM 失败次数")
    private Long llmFailCalls;

    @Schema(description = "LLM 成功率(%)")
    private BigDecimal llmSuccessRate;

    @Schema(description = "LLM 总输入 Token")
    private Long llmInputTokens;

    @Schema(description = "LLM 总输出 Token")
    private Long llmOutputTokens;

    @Schema(description = "LLM 总 Token")
    private Long llmTotalTokens;

    @Schema(description = "LLM 平均耗时(毫秒)")
    private BigDecimal llmAvgDurationMs;

    // ===== 知识库检索 =====
    @Schema(description = "检索总次数")
    private Long retrievalTotalCalls;

    @Schema(description = "检索成功次数")
    private Long retrievalSuccessCalls;

    @Schema(description = "召回文档块总数")
    private Long retrievalTotalChunks;

    @Schema(description = "检索 Embedding 消耗总 Token")
    private Long retrievalEmbeddingTokens;

    @Schema(description = "检索平均耗时(毫秒)")
    private BigDecimal retrievalAvgDurationMs;

    // ===== 知识库文档处理 =====
    @Schema(description = "文档处理总次数")
    private Long docTotalOps;

    @Schema(description = "文档处理成功次数")
    private Long docSuccessOps;

    @Schema(description = "文档处理总分块数")
    private Long docTotalChunks;

    @Schema(description = "文档处理 Embedding 消耗总 Token")
    private Long docEmbeddingTokens;
}
