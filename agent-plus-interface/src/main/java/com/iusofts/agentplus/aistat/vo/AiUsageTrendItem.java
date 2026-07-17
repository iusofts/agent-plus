package com.iusofts.agentplus.aistat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * AI 用量趋势项(按天或按小时的时间序列点)。
 *
 * @author Ivan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI 用量趋势项")
public class AiUsageTrendItem {

    @Schema(description = "时间标签(按天:2026-07-15;按小时:2026-07-15 13)")
    private String timeLabel;

    @Schema(description = "日期")
    private LocalDate date;

    @Schema(description = "小时(0-23,按天聚合时为 null)")
    private Integer hour;

    @Schema(description = "LLM 调用次数")
    private Long llmCalls;

    @Schema(description = "LLM 总 Token")
    private Long llmTotalTokens;

    @Schema(description = "LLM 平均耗时(毫秒)")
    private BigDecimal llmAvgDurationMs;

    @Schema(description = "检索次数")
    private Long retrievals;

    @Schema(description = "检索 Embedding Token")
    private Long retrievalEmbeddingTokens;

    @Schema(description = "文档处理次数")
    private Long docOps;
}
