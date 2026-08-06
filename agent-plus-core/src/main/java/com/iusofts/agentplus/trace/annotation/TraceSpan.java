package com.iusofts.agentplus.trace.annotation;

import io.opentelemetry.api.trace.SpanKind;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注需要开启 OpenTelemetry Span 的方法。
 *
 * <p>由 Spring AOP 切面（{@code TraceSpanAspect}）拦截，在方法执行前后创建并结束 Span，
 * 沿用 OTel 的 traceId / 父子关系 / 时间戳。仅对 Spring 托管 bean 的方法生效；
 * 引擎内部非 bean 的执行点（如节点执行器）请直接使用 {@code TraceUtil.span(...)}。</p>
 *
 * @author Ivan
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TraceSpan {

    /**
     * span 名称，支持硬编码，后续可扩展 SpEL 动态名称。
     */
    String name();

    /**
     * span 类型，默认 {@link SpanKind#INTERNAL}。
     */
    SpanKind kind() default SpanKind.INTERNAL;

    /**
     * span 标签，会自动设置为 {@code span.setAttribute(ATTR_LABEL, label)}。
     */
    String label() default "";
}
