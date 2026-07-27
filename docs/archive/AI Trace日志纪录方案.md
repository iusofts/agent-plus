# AI Trace 日志记录方案

基于 OpenTelemetry `opentelemetry-sdk` 手动 SDK 模式实现 AI 链路追踪，方案已落地。

## 1. 架构

1. 依赖：`opentelemetry-api` + `opentelemetry-sdk`。
2. 启动时由 `TraceAutoConfiguration`（引擎模块，自动装配）构建 `SdkTracerProvider` + `BatchSpanProcessor`，并注册到 `GlobalOpenTelemetry`。
3. 自定义 `SpanExporter`（`MySqlSpanExporter`，业务模块 agent-plus-service）拦截所有完成的 Span，批量写入 MySQL。span 结构落主表 `ai_trace_span`，大字段入参/出参落附表 `ai_trace_span_payload`。
4. 业务代码两种埋点方式：
   - Spring 托管 bean 的方法 → `@TraceSpan` 注解，由 `TraceSpanAspect`（AOP 切面）拦截；
   - 引擎内部非 bean 的执行点 → 直接调用 `TraceUtil.span(...)`。
5. TraceId、父子关系、时间戳沿用 OTel 成熟实现，不自己造；`traceId` 直接作为一次工作流执行的 `runId`，回传给调用方落业务表。

> `TraceAutoConfiguration` 未找到 `SpanExporter` bean 时不注册导出处理器（span 不落库，tracer 仍可用），因此引擎在单元测试等无 DB 环境下也能正常运行。可通过 `agent-plus.trace.enabled=false` 关闭。

## 2. 关键类

| 类 | 模块 | 职责 |
|---|---|---|
| `@TraceSpan` | agent-plus-core `trace.annotation` | 标注需开启 span 的 bean 方法，支持 `name` / `kind` / `label` |
| `TraceUtil` | agent-plus-core `trace` | span 模板、Context 传递、业务属性（Baggage + Span Attribute）存取、traceId 读取 |
| `TraceSpanAspect` | agent-plus-engine `engine.trace` | `@TraceSpan` 的 AOP 切面，自动记录方法入参/返回值为载荷 |
| `TraceAutoConfiguration` | agent-plus-engine `engine.config` | 初始化 OTel SDK + BatchSpanProcessor |
| `MySqlSpanExporter` | agent-plus-service `ailog.exporter` | 拦截 Span 批量落库，剥离载荷、汇总子孙 tokens |

## 3. `@TraceSpan` 注解

```java
package com.iusofts.agentplus.trace.annotation;

import io.opentelemetry.api.trace.SpanKind;
import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TraceSpan {
    /** span 名称，支持硬编码，后续可扩展 SpEL 动态名称。 */
    String name();
    /** span 类型，默认 INTERNAL。 */
    SpanKind kind() default SpanKind.INTERNAL;
    /** span 标签，会设置为 span.setAttribute("label", label)。 */
    String label() default "";
}
```

## 4. `TraceUtil` 能力概览

- **span 模板**：`span(name, kind, action)` / `span(name, kind, parentContext, action)`，自动 `makeCurrent`、异常记录 `StatusCode.ERROR` 与 `recordException`、`finally` 结束 span。
- **Context 传递**：`getCurrentContext()` / `runWithContext(context, ...)`，供批处理等异步线程恢复父 Context，使并行迭代的 span 挂到正确 trace。
- **业务属性**：`setCallSource` / `setOperator` / `setAiAttributes` 同时写入 OTel Baggage（供跨方法读取）与当前 Span Attribute（供落库）；读取用 `getCallSource` / `getSourceId` / `getOperatorId` / `getOrgId` 等。
- **traceId**：`currentTraceId()` 从当前 span 取，无有效 span 返回 null。
- **属性键常量**：Baggage key（`ai.call_source` 等）与 Span Attribute key（`label`、`ai.tokens`、`ai.model_provider`、`ai.model_name` 等）集中定义。

```java
Tracer tracer = GlobalOpenTelemetry.getTracer("ai.workflow.manual"); // 每次动态获取，避免类加载期锁定全局实例
```

## 5. 载荷（大字段）落附表

入参/出参通过约定 attribute key 传递，`MySqlSpanExporter` 导出时剥离，写入 `ai_trace_span_payload`，主表不保留大字段：

| attribute key | 含义 |
|---|---|
| `ap.payload.input` | 入参 JSON |
| `ap.payload.output` | 出参 JSON |

`TraceSpanAspect` 会自动把方法入参、返回值序列化为上述两个 key；引擎入口 `WorkflowEngine.execute` 也手动写入整体入参/出参载荷。

## 6. Token 汇总

`MySqlSpanExporter` 落库时：span 自身带 `ai.tokens` 则直接用；否则递归汇总其所有子孙 span 的原始 tokens 回填到该 span，便于按链路查看总消耗。LLM 调用侧（`AiModelChatModelProvider.chat`）在 `@TraceSpan("llm.chat")` 内写入本次调用的 `ai.tokens`。

## 7. 工作流使用示例

```java
// 引擎入口：root span 作为整条链路起点，其 traceId 即 runId
public WorkflowExecutionResult execute(WorkflowExecuteRequest request) {
    return TraceUtil.span("workflow.execute", SpanKind.INTERNAL, Context.root(), span -> {
        String runId = span.getSpanContext().isValid()
                ? span.getSpanContext().getTraceId()
                : request.getRunId();
        span.setAttribute("workflow.runId", runId);
        span.setAttribute("ap.payload.input", JSON.toJSONString(request.getInputs()));
        // ... 驱动主图，节点执行器内部通过 TraceUtil.setAiAttributes(...) 写业务属性
        return result;
    });
}
```

```java
// LLM 提供者：bean 方法用注解即可，切面自动建 span、记录入参/出参
@TraceSpan(name = "llm.chat", kind = SpanKind.INTERNAL)
public AiChatResponse chat(AiChatRequest request) {
    TraceUtil.setLabel(modelDTO.getModelName());
    TraceUtil.setSpanAttribute(TraceUtil.ATTR_MODEL_PROVIDER, modelDTO.getProvider());
    AiChatResponse response = doChat(request);
    TraceUtil.setSpanAttribute(TraceUtil.ATTR_TOKENS, response.getTotalTokens());
    return response;
}
```

节点执行器（`LLMNodeExecutor` / `KnowledgeNodeExecutor` / `ToolNodeExecutor`）在调用前统一 `TraceUtil.setAiAttributes("FLOW", flowId, nodeId, operatorId, orgId)`，把调用来源写入 Baggage + Span Attribute。

## 8. 表结构

见 `doc/sql/increment/2026-07-24-ai_trace_span.sql`（主表）与 `doc/sql/increment/2026-07-24-ai_trace_span_payload.sql`（载荷附表）。要点：

- 主表 `ai_trace_span`：`trace_id`(32hex) / `span_id`(16hex) / `parent_span_id`、`span_name`、`span_kind`、`status`、`attributes`(json)、`start_time`/`end_time`(ms 精度)/`duration_ms`、`org_id`、`trial_flag`；索引 `idx_trace_id`、`idx_start_time`。
- 附表 `ai_trace_span_payload`：`(trace_id, span_id)` 唯一，`input_payload` / `output_payload`(TEXT)。
