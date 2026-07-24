package com.iusofts.agentplus.trace;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

import java.util.function.Supplier;

/**
 * OpenTelemetry Trace 工具类。
 *
 * <p>提供同步 Span 模板、上下文传递、业务属性存取等能力。
 * <p>使用示例：
 * <pre>{@code
 * // 同步 Span 模板
 * String result = TraceUtil.span("workflow.execute", SpanKind.INTERNAL, span -> {
 *     span.setAttribute("workflow.instanceId", instanceId);
 *     return doBusinessLogic();
 * });
 *
 * // 设置业务属性（存储在 Baggage 中）
 * TraceUtil.setCallSource("CHAT", 123L, null);
 * TraceUtil.setOperator(456L, 789);
 *
 * // 读取业务属性（从 Baggage 中读取）
 * String traceId = TraceUtil.currentTraceId();
 * String callSource = TraceUtil.getCallSource();
 * }</pre>
 *
 * @author Ivan
 */
public final class TraceUtil {

    /** instrumentation scope 名称 */
    private static final String INSTRUMENTATION_SCOPE = "ai.workflow.manual";

    // ==================== Baggage Keys ====================

    /** OTel Baggage 键：调用来源 */
    public static final String KEY_CALL_SOURCE = "ai.call_source";
    /** OTel Baggage 键：来源 ID */
    public static final String KEY_SOURCE_ID = "ai.source_id";
    /** OTel Baggage 键：来源节点 ID */
    public static final String KEY_SOURCE_NODE_ID = "ai.source_node_id";
    /** OTel Baggage 键：操作人 ID */
    public static final String KEY_OPERATOR_ID = "ai.operator_id";
    /** OTel Baggage 键：组织 ID */
    public static final String KEY_ORG_ID = "ai.org_id";

    // ==================== Span Attribute Keys（用于记录，不用于读取） ====================

    /** OTel Span Attribute 键：调用来源 */
    public static final String ATTR_CALL_SOURCE = "ai.call_source";
    /** OTel Span Attribute 键：来源 ID */
    public static final String ATTR_SOURCE_ID = "ai.source_id";
    /** OTel Span Attribute 键：来源节点 ID */
    public static final String ATTR_SOURCE_NODE_ID = "ai.source_node_id";
    /** OTel Span Attribute 键：操作人 ID */
    public static final String ATTR_OPERATOR_ID = "ai.operator_id";
    /** OTel Span Attribute 键：组织 ID */
    public static final String ATTR_ORG_ID = "ai.org_id";

    private TraceUtil() {
    }

    /**
     * 获取 Tracer。每次调用时从 {@link GlobalOpenTelemetry} 获取，避免静态字段在类加载
     * 时提前锁定全局实例（导致后续 {@code GlobalOpenTelemetry.set} 抛异常）。
     */
    private static Tracer tracer() {
        return GlobalOpenTelemetry.getTracer(INSTRUMENTATION_SCOPE);
    }

    // ==================== Span 模板方法 ====================

    /**
     * 创建同步 Span 模板，自动处理异常状态并结束 Span。
     *
     * @param spanName span 名称
     * @param kind     span 类型
     * @param action   业务逻辑
     * @param <T>      返回值类型
     * @return 业务逻辑返回值
     */
    @SuppressWarnings("unchecked")
    public static <T, E extends Throwable> T span(String spanName, SpanKind kind, TraceAction<T> action) throws E {
        Span span = tracer().spanBuilder(spanName)
            .setSpanKind(kind)
            .startSpan();
        try (Scope scope = span.makeCurrent()) {
            return action.run(span);
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            throw (E) e;
        } finally {
            span.end();
        }
    }

    /**
     * 无返回值简化版。
     */
    public static void span(String spanName, SpanKind kind, TraceVoidAction action) {
        span(spanName, kind, s -> {
            action.run(s);
            return null;
        });
    }

    // ==================== Context 传递 ====================

    /**
     * 获取当前 OTel Context，用于异步线程传递。
     */
    public static Context getCurrentContext() {
        return Context.current();
    }

    /**
     * 在异步线程中恢复 OTel Context。
     *
     * @param context  父线程的 Context
     * @param supplier 业务逻辑
     * @param <T>      返回值类型
     * @return 业务逻辑返回值
     */
    public static <T> T runWithContext(Context context, Supplier<T> supplier) {
        try (Scope scope = context.makeCurrent()) {
            return supplier.get();
        }
    }

    /**
     * 在异步线程中恢复 OTel Context（无返回值）。
     */
    public static void runWithContext(Context context, Runnable runnable) {
        runWithContext(context, () -> {
            runnable.run();
            return null;
        });
    }

    // ==================== TraceId 获取 ====================

    /**
     * 从当前 Span 获取 traceId，无 active span 时返回 {@code null}。
     */
    public static String currentTraceId() {
        Span span = Span.current();
        if (span == null || !span.getSpanContext().isValid()) {
            return null;
        }
        return span.getSpanContext().getTraceId();
    }

    // ==================== 业务属性设置（同时设置到 Baggage 和 Span Attributes） ====================

    /**
     * 设置调用来源信息到 Baggage 和 Span Attributes。
     */
    public static void setCallSource(String callSource, Long sourceId) {
        setCallSource(callSource, sourceId, null);
    }

    /**
     * 设置调用来源信息（含节点）到 Baggage 和 Span Attributes。
     */
    public static void setCallSource(String callSource, Long sourceId, String sourceNodeId) {
        BaggageBuilder builder = Baggage.current().toBuilder();
        boolean updated = false;
        if (callSource != null) {
            builder.put(KEY_CALL_SOURCE, callSource);
            updated = true;
        }
        if (sourceId != null) {
            builder.put(KEY_SOURCE_ID, String.valueOf(sourceId));
            updated = true;
        }
        if (sourceNodeId != null) {
            builder.put(KEY_SOURCE_NODE_ID, sourceNodeId);
            updated = true;
        }
        if (updated) {
            builder.build().makeCurrent();
        }

        // 同时设置到 Span Attributes 用于记录
        Span span = Span.current();
        if (span != null && span.getSpanContext().isValid()) {
            if (callSource != null) span.setAttribute(ATTR_CALL_SOURCE, callSource);
            if (sourceId != null) span.setAttribute(ATTR_SOURCE_ID, sourceId);
            if (sourceNodeId != null) span.setAttribute(ATTR_SOURCE_NODE_ID, sourceNodeId);
        }
    }

    /**
     * 设置操作人信息到 Baggage 和 Span Attributes。
     */
    public static void setOperator(Long operatorId, Integer orgId) {
        BaggageBuilder builder = Baggage.current().toBuilder();
        boolean updated = false;
        if (operatorId != null) {
            builder.put(KEY_OPERATOR_ID, String.valueOf(operatorId));
            updated = true;
        }
        if (orgId != null) {
            builder.put(KEY_ORG_ID, String.valueOf(orgId));
            updated = true;
        }
        if (updated) {
            builder.build().makeCurrent();
        }

        // 同时设置到 Span Attributes 用于记录
        Span span = Span.current();
        if (span != null && span.getSpanContext().isValid()) {
            if (operatorId != null) span.setAttribute(ATTR_OPERATOR_ID, operatorId);
            if (orgId != null) span.setAttribute(ATTR_ORG_ID, orgId);
        }
    }

    /**
     * 一次性设置所有业务属性到 Baggage 和 Span Attributes。
     */
    public static void setAiAttributes(String callSource, Long sourceId, String sourceNodeId,
                                        Long operatorId, Integer orgId) {
        BaggageBuilder builder = Baggage.current().toBuilder();
        boolean updated = false;
        if (callSource != null) {
            builder.put(KEY_CALL_SOURCE, callSource);
            updated = true;
        }
        if (sourceId != null) {
            builder.put(KEY_SOURCE_ID, String.valueOf(sourceId));
            updated = true;
        }
        if (sourceNodeId != null) {
            builder.put(KEY_SOURCE_NODE_ID, sourceNodeId);
            updated = true;
        }
        if (operatorId != null) {
            builder.put(KEY_OPERATOR_ID, String.valueOf(operatorId));
            updated = true;
        }
        if (orgId != null) {
            builder.put(KEY_ORG_ID, String.valueOf(orgId));
            updated = true;
        }
        if (updated) {
            builder.build().makeCurrent();
        }

        // 同时设置到 Span Attributes 用于记录
        Span span = Span.current();
        if (span != null && span.getSpanContext().isValid()) {
            if (callSource != null) span.setAttribute(ATTR_CALL_SOURCE, callSource);
            if (sourceId != null) span.setAttribute(ATTR_SOURCE_ID, sourceId);
            if (sourceNodeId != null) span.setAttribute(ATTR_SOURCE_NODE_ID, sourceNodeId);
            if (operatorId != null) span.setAttribute(ATTR_OPERATOR_ID, operatorId);
            if (orgId != null) span.setAttribute(ATTR_ORG_ID, orgId);
        }
    }

    // ==================== 业务属性读取（从 Baggage 中读取） ====================

    /**
     * 从 Baggage 获取调用来源。
     */
    public static String getCallSource() {
        return Baggage.current().getEntryValue(KEY_CALL_SOURCE);
    }

    /**
     * 从 Baggage 获取来源 ID。
     */
    public static Long getSourceId() {
        String val = Baggage.current().getEntryValue(KEY_SOURCE_ID);
        if (val == null || val.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 从 Baggage 获取来源节点 ID。
     */
    public static String getSourceNodeId() {
        return Baggage.current().getEntryValue(KEY_SOURCE_NODE_ID);
    }

    /**
     * 从 Baggage 获取操作人 ID。
     */
    public static Long getOperatorId() {
        String val = Baggage.current().getEntryValue(KEY_OPERATOR_ID);
        if (val == null || val.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 从 Baggage 获取组织 ID。
     */
    public static Integer getOrgId() {
        String val = Baggage.current().getEntryValue(KEY_ORG_ID);
        if (val == null || val.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 检查当前是否有有效的 Span。
     */
    public static boolean hasActiveSpan() {
        Span span = Span.current();
        return span != null && span.getSpanContext().isValid();
    }

    // ==================== 辅助接口 ====================

    @FunctionalInterface
    public interface TraceAction<T> {
        T run(Span span) throws Exception;
    }

    @FunctionalInterface
    public interface TraceVoidAction {
        void run(Span span) throws Exception;
    }
}
