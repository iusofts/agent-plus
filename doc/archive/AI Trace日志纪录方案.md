# AI Trace日志纪录方案

opentelemetry-sdk 手动 SDK 模式

### 架构

1. 依赖：api + sdk
2. @PostConstruct 启动时初始化 OTel SDK
3. 自定义 SpanExporter，拦截所有完成的 Span，批量写入MySQL(需要纪录完整的入参和出参)
4. 业务代码依旧：`@TraceSpan` + `Span.current()` 标准写法
5. TraceId、父子关系、时间戳沿用 OTel 成熟实现，不用自己造

```sql
import io.opentelemetry.api.trace.SpanKind;
import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TraceSpan {
    // span名称，支持硬编码，后续可扩展SpEL动态名称
    String name();
    SpanKind kind() default SpanKind.INTERNAL;
}
```

```sql
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

public class TraceUtil {

    private static final Tracer TRACER = GlobalOpenTelemetry.getTracer("ai-workflow-manual");

    /**
     * 创建同步Span模板
     * @param spanName span名称
     * @param action 执行业务逻辑
     */
    public static <T> T span(String spanName, SpanKind kind, TraceAction<T> action) {
        Span span = TRACER.spanBuilder(spanName)
                .setSpanKind(kind)
                .startSpan();
        try (Scope scope = span.makeCurrent()) {
            return action.run(span);
        } catch (Throwable e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * 无返回值简化版
     */
    public static void span(String spanName, SpanKind kind, TraceVoidAction action) {
        span(spanName, kind, s -> {
            action.run(s);
            return null;
        });
    }

    @FunctionalInterface
    public interface TraceAction<T> {
        T run(Span span) throws Exception;
    }

    @FunctionalInterface
    public interface TraceVoidAction {
        void run(Span span) throws Exception;
    }

    /**
     * 获取当前上下文，用于异步线程传递
     */
    public static Context getCurrentContext() {
        return Context.current();
    }

    /**
     * 异步线程恢复上下文
     */
    public static <T> T runWithContext(Context context, java.util.function.Supplier<T> supplier) {
        try (Scope scope = context.makeCurrent()) {
            return supplier.get();
        }
    }
}
```

工作流使用示例 仅供参考 

```sql
@Service
public class AiWorkflowEngine {

    @TraceSpan(name = "ai.workflow.instance.run", kind = SpanKind.INTERNAL)
    public void runInstance(WorkflowInstance instance) {
        // 【根Span，整条链路起点】
        Span currentSpan = Span.current();
        currentSpan.setAttribute("workflow.instanceId", instance.getInstanceId());

        // 保存 instanceId <-> traceId 映射关系
        String traceId = currentSpan.getSpanContext().getTraceId();
        saveRelation(instance.getInstanceId(), traceId);
        
        // MDC填充日志标识
        MDC.put("traceId", traceId);
        MDC.put("workflowInstanceId", instance.getInstanceId());

        executeRag(instance);
        executeLlmChat(instance);
        executeToolCall(instance);
    }

    @TraceSpan(name = "workflow.step.rag_retrieve")
    private List<Document> executeRag(WorkflowInstance instance) {
        List<Document> docs = vectorStore.search(instance.getQuery());
        Span.current().setAttribute("rag.doc_count", docs.size());
        return docs;
    }

    @TraceSpan(name = "workflow.step.llm_chat")
    private ChatResponse executeLlmChat(WorkflowInstance instance) {
        ChatResponse resp = chatModel.generate(...);
        Span.current().setAttribute("gen_ai.input_tokens", resp.tokenUsage().inputTokenCount());
        return resp;
    }
}
```