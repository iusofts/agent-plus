package com.iusofts.agentplus.ailog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * LLM 调用统计项。
 *
 * @author Ivan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "LLM 调用统计项")
public class LlmCallStatItem {

    @Schema(description = "统计维度名称（日期/模型名/组织名）")
    private String name;

    @Schema(description = "日期")
    private LocalDate date;

    @Schema(description = "模型 ID")
    private Long modelId;

    @Schema(description = "组织 ID")
    private Integer orgId;

    @Schema(description = "总调用次数")
    private Long totalCalls;

    @Schema(description = "成功次数")
    private Long successCalls;

    @Schema(description = "失败次数")
    private Long failCalls;

    @Schema(description = "总输入 Token")
    private Long totalInputTokens;

    @Schema(description = "总输出 Token")
    private Long totalOutputTokens;

    @Schema(description = "总 Token")
    private Long totalTokens;

    @Schema(description = "平均耗时（毫秒）")
    private BigDecimal avgDurationMs;
}
