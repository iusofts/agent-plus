package com.iusofts.agentplus.ailog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Span 入参返回值载荷附表。
 *
 * <p>与 ai_trace_span 通过 (trace_id, span_id) 关联，用于存储大字段入参/出参，
 * 使主表保持轻量。</p>
 *
 * @author Ivan
 * @since 2026-07-24
 */
@Getter
@Setter
@ToString
@TableName("ai_trace_span_payload")
@Schema(name = "AiTraceSpanPayload", description = "Span入参返回值载荷")
public class AiTraceSpanPayload implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "traceId")
    private String traceId;

    @Schema(description = "spanId")
    private String spanId;

    @Schema(description = "节点入参(JSON)")
    private String inputPayload;

    @Schema(description = "节点返回值(JSON)")
    private String outputPayload;

    @Schema(description = "记录创建时间")
    private LocalDateTime createTime;
}