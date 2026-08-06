package com.iusofts.agentplus.engine.trace;

import com.iusofts.agentplus.trace.annotation.TraceSpan;
import com.iusofts.agentplus.trace.constants.TraceConstant;
import com.alibaba.fastjson2.JSON;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link TraceSpan} 注解的 Spring AOP 切面。
 *
 * <p>拦截所有 {@code @TraceSpan} 标注的方法，在方法执行前后创建并结束 Span，
 * 自动记录方法入参和异常信息。仅对 Spring 托管 bean 生效。</p>
 *
 * @author Ivan
 * @since 2026-07-24
 */
@Aspect
@Component
public class TraceSpanAspect {

    private static final String INSTRUMENTATION_SCOPE = "ai-workflow-aop";

    private Tracer tracer() {
        return GlobalOpenTelemetry.getTracer(INSTRUMENTATION_SCOPE);
    }

    @Around("@annotation(traceSpan)")
    public Object aroundTraceSpan(ProceedingJoinPoint joinPoint, TraceSpan traceSpan) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String spanName = traceSpan.name();
        if (spanName == null || spanName.isEmpty()) {
            spanName = method.getDeclaringClass().getSimpleName() + "." + method.getName();
        }

        Span span = tracer().spanBuilder(spanName)
                .setSpanKind(traceSpan.kind())
                .startSpan();

        // 设置 label 属性
        String label = traceSpan.label();
        if (label != null && !label.isEmpty()) {
            span.setAttribute(TraceConstant.ATTR_LABEL, label);
        }

        // 记录方法入参为载荷（写入约定 payload key，由 exporter 落附表）
        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        Object[] paramValues = joinPoint.getArgs();
        if (paramValues != null && paramValues.length > 0) {
            try {
                Map<String, Object> inputMap = new LinkedHashMap<>();
                for (int i = 0; i < paramValues.length; i++) {
                    String name = (paramNames != null && i < paramNames.length) ? paramNames[i] : ("arg" + i);
                    inputMap.put(name, paramValues[i]);
                }
                span.setAttribute(TraceConstant.ATTR_PAYLOAD_INPUT, JSON.toJSONString(inputMap));
            } catch (Exception ignore) {
                // 序列化失败不影响主流程
            }
        }

        try (Scope scope = span.makeCurrent()) {
            Object result = joinPoint.proceed();
            // 记录返回值载荷
            if (result != null) {
                try {
                    span.setAttribute(TraceConstant.ATTR_PAYLOAD_OUTPUT, JSON.toJSONString(result));
                } catch (Exception ignore) {
                    // 序列化失败不影响主流程
                }
            }
            return result;
        } catch (Throwable e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            span.setAttribute(TraceConstant.ATTR_ERROR_TYPE, e.getClass().getSimpleName());
            throw e;
        } finally {
            span.end();
        }
    }

    private static String truncate(String text, int maxLen) {
        if (text == null) {
            return null;
        }
        return text.length() > maxLen ? text.substring(0, maxLen) + "..." : text;
    }
}