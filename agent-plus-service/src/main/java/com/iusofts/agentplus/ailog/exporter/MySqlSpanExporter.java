package com.iusofts.agentplus.ailog.exporter;

import com.iusofts.agentplus.ailog.entity.AiTraceSpan;
import com.iusofts.agentplus.ailog.entity.AiTraceSpanPayload;
import com.iusofts.agentplus.ailog.service.AiTraceSpanPayloadService;
import com.iusofts.agentplus.ailog.service.AiTraceSpanService;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 自定义 SpanExporter —— 将 OTel Span 数据批量写入 MySQL。
 *
 * <p>主表 {@code ai_trace_span} 记录 span 结构（轻量），附表 {@code ai_trace_span_payload}
 * 记录大字段入参/出参。载荷通过约定 attribute key 传递：
 * <ul>
 *   <li>{@code ap.payload.input} — 入参 JSON</li>
 *   <li>{@code ap.payload.output} — 出参 JSON</li>
 * </ul>
 * 导出时自动剥离这两条 key，主表 attributes 不保存大字段。</p>
 *
 * @author Ivan
 * @since 2026-07-24
 */
@Component
public class MySqlSpanExporter implements SpanExporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MySqlSpanExporter.class);

    /** 载荷 attribute key：入参。 */
    private static final String ATTR_PAYLOAD_INPUT = "ap.payload.input";
    /** 载荷 attribute key：出参。 */
    private static final String ATTR_PAYLOAD_OUTPUT = "ap.payload.output";

    @Resource
    private AiTraceSpanService aiTraceSpanService;

    @Resource
    private AiTraceSpanPayloadService aiTraceSpanPayloadService;

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        if (spans.isEmpty()) {
            return CompletableResultCode.ofSuccess();
        }
        try {
            List<AiTraceSpan> entities = new ArrayList<>(spans.size());
            List<AiTraceSpanPayload> payloads = new ArrayList<>();
            for (SpanData span : spans) {
                AiTraceSpanPayload payload = new AiTraceSpanPayload();
                entities.add(toEntity(span, payload));
                if (payload.getInputPayload() != null || payload.getOutputPayload() != null) {
                    payloads.add(payload);
                }
            }
            aiTraceSpanService.batchSave(entities);
            if (!payloads.isEmpty()) {
                aiTraceSpanPayloadService.batchSave(payloads);
            }
        } catch (Exception e) {
            LOGGER.error("MySqlSpanExporter 批量落库失败, 丢失 {} 条 span", spans.size(), e);
        }
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public void close() {
        // 由 BatchSpanProcessor 管理生命周期
    }

    // ============ 内部转换 ============

    /**
     * 转换 span 为主表实体，并提取载荷到 {@code payloadOut}。
     */
    private AiTraceSpan toEntity(SpanData span, AiTraceSpanPayload payloadOut) {
        AiTraceSpan entity = new AiTraceSpan();

        // traceId / spanId / parentSpanId
        entity.setTraceId(span.getTraceId());
        entity.setSpanId(span.getSpanId());
        String parentSpanId = span.getParentSpanId();
        entity.setParentSpanId(parentSpanId == null || parentSpanId.isEmpty() ? null : parentSpanId);

        // 名称 / 类型 / 状态
        entity.setSpanName(span.getName());
        entity.setSpanKind(span.getKind().name());
        entity.setStatus(span.getStatus().getStatusCode().name());
        String statusDesc = span.getStatus().getDescription();
        if (statusDesc != null && !statusDesc.isEmpty()) {
            entity.setStatusMessage(statusDesc);
        }

        // 时间
        long startNanos = span.getStartEpochNanos();
        long endNanos = span.getEndEpochNanos();
        entity.setStartTime(nanosToLocalDateTime(startNanos));
        entity.setEndTime(nanosToLocalDateTime(endNanos));
        entity.setDurationMs(TimeUnit.NANOSECONDS.toMillis(endNanos - startNanos));

        // attributes：剥离载荷 key 后写入主表
        Attributes attrs = span.getAttributes();
        if (!attrs.isEmpty()) {
            Map<String, Object> attrMap = new HashMap<>();
            attrs.forEach((key, value) -> {
                String k = key.getKey();
                if (ATTR_PAYLOAD_INPUT.equals(k)) {
                    payloadOut.setInputPayload(value == null ? null : value.toString());
                } else if (ATTR_PAYLOAD_OUTPUT.equals(k)) {
                    payloadOut.setOutputPayload(value == null ? null : value.toString());
                } else {
                    attrMap.put(k, value);
                }
            });
            if (!attrMap.isEmpty()) {
                entity.setAttributes(attrMap);
            }

            // 提取业务字段
            Object orgIdVal = attrMap.get("orgId");
            if (orgIdVal instanceof Number num) {
                entity.setOrgId(num.intValue());
            }
            Object trialVal = attrMap.get("trialFlag");
            if (trialVal instanceof Boolean b) {
                entity.setTrialFlag(b ? 1 : 0);
            }
        }

        // 回填 payload 的关联键
        payloadOut.setTraceId(span.getTraceId());
        payloadOut.setSpanId(span.getSpanId());

        return entity;
    }

    private static LocalDateTime nanosToLocalDateTime(long epochNanos) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(0, epochNanos),
                ZoneId.systemDefault());
    }
}