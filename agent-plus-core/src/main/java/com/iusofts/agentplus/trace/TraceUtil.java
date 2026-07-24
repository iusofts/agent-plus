package com.iusofts.agentplus.trace;

import io.opentelemetry.api.GlobalOpenTelemetry;
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
 * <p>提供同步 Span 模板、上下文传递等能力，供引擎非 Spring bean 层级（如
 * 节点执行器）手动使用。Spring bean 层请使用 {@code @TraceSpan} 注解。</p>
 *
 * <p>使用示例：
 * <pre>{@code
 * // 同步 Span 模板
 * String result = TraceUtil.span("workflow.execute", SpanKind.INTERNAL, span -> {
 *     span.setAttribute("workflow.instanceId", instanceId);
 *     return doBusinessLogic();
 * });
 *
 * // 异步跨线程传递
 * Context parentCtx = TraceUtil.getCurrentContext();
 * CompletableFuture.runAsync(() -> {
 *     TraceUtil.runWithContext(parentCtx, () -> {
 *         // 此处的 Span.current() 与父线程一致
 *         doAsyncWork();
 *     });
 * });
 * }</pre>
 *
 * @author Ivan
 */
public final class TraceUtil {

    /**
     * instrumentation scope 名称。
     */
    private static final String INSTRUMENTATION_SCOPE = "ai-workflow-manual";

    private TraceUtil() {
    }

    /**
     * 获取 Tracer。每次调用时从 {@link GlobalOpenTelemetry} 获取，避免静态字段在类加载
     * 时提前锁定全局实例（导致后续 {@code GlobalOpenTelemetry.set} 抛异常）。
     */
    private static Tracer tracer() {
        return GlobalOpenTelemetry.getTracer(INSTRUMENTATION_SCOPE);
    }

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

    @FunctionalInterface
    public interface TraceAction<T> {
        T run(Span span) throws Exception;
    }

    @FunctionalInterface
    public interface TraceVoidAction {
        void run(Span span) throws Exception;
    }
}