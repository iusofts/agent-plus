package com.iusofts.agentplus.aistat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 按模型维度的 LLM 用量明细。
 *
 * @author Ivan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "按模型维度的 LLM 用量明细")
public class LlmModelUsageItem {

    @Schema(description = "模型 ID")
    private Long modelId;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "模型提供商")
    private String modelProvider;

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

    @Schema(description = "平均耗时(毫秒)")
    private BigDecimal avgDurationMs;
}
