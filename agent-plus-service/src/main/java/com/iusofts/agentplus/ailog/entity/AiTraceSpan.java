package com.iusofts.agentplus.ailog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * AI Trace Span 记录。
 *
 * <p>存储 OpenTelemetry Span 数据，与 ai_flow_runtime、ai_llm_call_log 等业务表
 * 通过 trace_id 关联。</p>
 *
 * @author Ivan
 * @since 2026-07-24
 */
@Getter
@Setter
@ToString
@TableName(value = "ai_trace_span", autoResultMap = true)
@Schema(name = "AiTraceSpan", description = "AI Trace Span记录")
public class AiTraceSpan implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "自增主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "OTel 128-bit traceId(32hex)")
    private String traceId;

    @Schema(description = "OTel 64-bit spanId(16hex)")
    private String spanId;

    @Schema(description = "父 spanId")
    private String parentSpanId;

    @Schema(description = "span名称")
    private String spanName;

    @Schema(description = "span类型: INTERNAL/SERVER/CLIENT/PRODUCER/CONSUMER")
    private String spanKind;

    @Schema(description = "span状态: OK/ERROR")
    private String status;

    @Schema(description = "错误信息(仅status=ERROR时)")
    private String statusMessage;

    @Schema(description = "span attribute键值对(含入参/出参等业务信息)")
    @TableField(value = "attributes", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> attributes;

    @Schema(description = "span开始时间(毫秒精度)")
    private LocalDateTime startTime;

    @Schema(description = "span结束时间(毫秒精度)")
    private LocalDateTime endTime;

    @Schema(description = "span耗时(毫秒)")
    private Long durationMs;

    @Schema(description = "组织ID")
    private Integer orgId;

    @Schema(description = "试运行标记 0:正式 1:试运行")
    private Integer trialFlag;

    @Schema(description = "记录创建时间")
    private LocalDateTime createTime;
}