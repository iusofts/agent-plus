package com.iusofts.agentplus.ailog.exporter;

import com.iusofts.agentplus.ailog.entity.AiTraceSpan;
import com.iusofts.agentplus.ailog.entity.AiTraceSpanPayload;
import com.iusofts.agentplus.ailog.service.AiTraceSpanPayloadService;
import com.iusofts.agentplus.ailog.service.AiTraceSpanService;
import com.iusofts.agentplus.trace.TraceUtil;
import io.opentelemetry.api.common.AttributeKey;
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

import static com.iusofts.agentplus.trace.constants.TraceConstant.*;

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
            Map<String, AiTraceSpan> spanIdToEntity = new HashMap<>();
            Map<String, List<AiTraceSpan>> parentToChildren = new HashMap<>();
            Map<String, Long> spanIdToOwnTokens = new HashMap<>();

            // 第一步：转换并建立映射关系
            for (SpanData span : spans) {
                AiTraceSpanPayload payload = new AiTraceSpanPayload();
                AiTraceSpan entity = toEntity(span, payload);
                entities.add(entity);
                spanIdToEntity.put(entity.getSpanId(), entity);

                // 记录 span 自己的原始 tokens
                Map<String, Object> attrs = entity.getAttributes();
                if (attrs != null && attrs.containsKey(ATTR_TOKENS)) {
                    Object val = attrs.get(ATTR_TOKENS);
                    if (val instanceof Number num) {
                        spanIdToOwnTokens.put(entity.getSpanId(), num.longValue());
                    }
                }

                // 建立父子关系
                String parentSpanId = entity.getParentSpanId();
                if (parentSpanId != null) {
                    parentToChildren.computeIfAbsent(parentSpanId, k -> new ArrayList<>()).add(entity);
                }

                if (payload.getInputPayload() != null || payload.getOutputPayload() != null) {
                    payloads.add(payload);
                }
            }

            // 第二步：汇总所有子孙级 tokens 到每个 span（如果自己没有的话）
            for (AiTraceSpan entity : entities) {
                // 如果父级自己已经有 tokens 了，就不汇总子级的了
                Map<String, Object> attrs = entity.getAttributes();
                if (attrs != null && attrs.containsKey(ATTR_TOKENS)) {
                    Object val = attrs.get(ATTR_TOKENS);
                    if (val instanceof Number num && num.longValue() > 0) {
                        continue;
                    }
                }

                long totalTokens = sumDescendantTokens(entity.getSpanId(), parentToChildren, spanIdToOwnTokens);
                if (totalTokens > 0) {
                    if (attrs == null) {
                        attrs = new HashMap<>();
                        entity.setAttributes(attrs);
                    }
                    attrs.put(ATTR_TOKENS, totalTokens);
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

    /**
     * 递归汇总指定 span 及其所有子孙级的原始 tokens。
     */
    private long sumDescendantTokens(String spanId,
                                      Map<String, List<AiTraceSpan>> parentToChildren,
                                      Map<String, Long> spanIdToOwnTokens) {
        long sum = 0L;

        // 加上自己的 tokens
        Long ownTokens = spanIdToOwnTokens.get(spanId);
        if (ownTokens != null) {
            sum += ownTokens;
        }

        // 递归加上所有子级的 tokens
        List<AiTraceSpan> children = parentToChildren.get(spanId);
        if (children != null) {
            for (AiTraceSpan child : children) {
                sum += sumDescendantTokens(child.getSpanId(), parentToChildren, spanIdToOwnTokens);
            }
        }

        return sum;
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
                if (ATTR_PAYLOAD_INPUT.equals(k) || ATTR_PAYLOAD_OUTPUT.equals(k)) {
                    return;
                }
                attrMap.put(k, value);
            });
            if (!attrMap.isEmpty()) {
                entity.setAttributes(attrMap);
            }

            // 提取业务字段
            Long orgId = attrs.get(AttributeKey.longKey(KEY_ORG_ID));
            entity.setOrgId(orgId != null ? orgId.intValue() : 0);
            entity.setOperatorId(attrs.get(AttributeKey.longKey(KEY_OPERATOR_ID)));

            Long trialFlag = attrs.get(AttributeKey.longKey(ATTR_TRIAL_FLAG));
            entity.setTrialFlag(trialFlag != null ? trialFlag.intValue() : 0);

            payloadOut.setInputPayload(attrs.get(AttributeKey.stringKey(ATTR_PAYLOAD_INPUT)));
            payloadOut.setOutputPayload(attrs.get(AttributeKey.stringKey(ATTR_PAYLOAD_OUTPUT)));

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