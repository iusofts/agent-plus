package com.iusofts.agentplus.aiflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * AI Trace 根 Span 列表项
 * </p>
 *
 * <p>对应 span 表中 parent_span_id = ROOT_SPAN_ID 的根 Span 摘要，
 * 列表页不返回 attributes 整段 JSON，按需由详情接口补全。</p>
 *
 * @author Ivan
 * @since 2026-08-07
 */
@Data
public class AiTraceSpanListVo {

    @Schema(description = "span表主键ID")
    private Long id;

    @Schema(description = "OTel 128-bit traceId(32hex)")
    private String traceId;

    @Schema(description = "OTel 64-bit spanId(16hex)")
    private String spanId;

    @Schema(description = "span名称")
    private String spanName;

    @Schema(description = "span展示标签(取自 attributes.label)")
    private String label;

    @Schema(description = "span状态: OK/ERROR")
    private String status;

    @Schema(description = "错误信息(仅status=ERROR时)")
    private String statusMessage;

    @Schema(description = "span开始时间")
    private LocalDateTime startTime;

    @Schema(description = "span结束时间")
    private LocalDateTime endTime;

    @Schema(description = "span耗时(毫秒)")
    private Long durationMs;

    @Schema(description = "组织ID")
    private Integer orgId;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "试运行标记 0:正式 1:试运行")
    private Integer trialFlag;

}
