# AI Trace 日志记录方案

> 基于 OpenTelemetry `opentelemetry-sdk` 手动 SDK 模式实现 AI 链路追踪 + 可配置采样率。
>
> **Last Updated**: 2026-08-10 ｜ **相关 Commits**: 见文末

## 概述

```
[业务埋点]                       [OTel SDK]                    [MySQL 落库 + 采样]
  TraceUtil.span(...)           SdkTracerProvider
  @TraceSpan ────►  Aspect ───►   │  BusinessAttrSpanProcessor  ◄── baggage 自动注入
                                  │   (onStart: baggage→attr)
                                  │
                                  ▼
                          BatchSpanProcessor (5s/批)
                                  │
                                  ▼
                          MySqlSpanExporter
                                  │
                          ┌───────┴───────┐
                          │ 采样过滤     │ ◄── ai_trace_sample_config
                          │ (per-trace + │     + yml 兜底
                          │  Redis 决策  │     + short-circuit
                          │  缓存)       │
                          └───────┬───────┘
                                  ▼
                          ai_trace_span / ai_trace_span_payload
                          (含 trialFlag、op/orgId 业务字段)
```

## 1. 架构

1. **依赖**：`opentelemetry-api` + `opentelemetry-sdk` + `redisson-spring-boot-starter`(经 `agent-plus-common` 透传,提供 Redis Pub/Sub 集群失效广播与 trace 决策缓存)
2. **启动时**由 `TraceAutoConfiguration`(引擎模块,自动装配)构建 `SdkTracerProvider` + `BusinessAttrSpanProcessor` + `BatchSpanProcessor`,注册到 `GlobalOpenTelemetry`
3. **业务 SpanExporter** `MySqlSpanExporter`(业务模块 agent-plus-service)在 export 时按 `ai_trace_sample_config` 配置做 per-trace 决策过滤,落库到 `ai_trace_span`(主表) + `ai_trace_span_payload`(载荷附表)
4. **业务埋点两种方式**:
   - Spring 托管 bean 的方法 → `@TraceSpan` 注解,由 `TraceSpanAspect`(AOP 切面)拦截
   - 引擎内部非 bean 的执行点 → 直接调用 `TraceUtil.span(...)` 模板
5. **traceId 流程反转**:`WorkflowEngine.execute` 在最外层开 root span,取 `span.getSpanContext().getTraceId()` 作 runId,经 `WorkflowExecutionResult.getRunId()` 回传给 service 落业务表(原来由 service 先生成 traceId 传入,现由引擎生成后回传)

> `TraceAutoConfiguration` 未找到 `SpanExporter` bean 时不注册导出处理器(span 不落库,tracer 仍可用),因此引擎在单元测试等无 DB 环境下也能正常运行。可通过 `agent-plus.trace.enabled=false` 关闭。

## 2. 关键类

### 2.1 核心模块

| 类 | 模块 | 职责 |
|---|---|---|
| `@TraceSpan` | agent-plus-core `trace.annotation` | 标注需开启 span 的 bean 方法,支持 `name` / `kind` / `label` |
| `TraceUtil` | agent-plus-core `trace` | span 模板、Context 传递、业务属性(Baggage + Span Attribute)存取、traceId 读取、setTrialFlag 等工具 |
| `TraceConstant` | agent-plus-core `trace.constants` | Baggage key 与 Span Attribute key 字面量统一管理(Baggage 与 Span Attribute 共用同一 key) |
| `CallSource` | agent-plus-core `trace.constants` | 调用来源枚举(CHAT / AGENT / FLOW / ...) |
| `BusinessAttrSpanProcessor` | agent-plus-engine `engine.trace` | onStart 钩子:从 parentContext.baggage 读 `ai.org_id` / `ai.operator_id` / `workflow.trial_flag` 回写到当前 span 的 attributes,**保证子 span 也有业务键** |
| `TraceSpanAspect` | agent-plus-engine `engine.trace` | `@TraceSpan` 的 AOP 切面,自动记录方法入参/返回值为载荷 |
| `TraceAutoConfiguration` | agent-plus-engine `engine.config` | 初始化 OTel SDK,注册 `BusinessAttrSpanProcessor`(先于 `BatchSpanProcessor`)+ `BatchSpanProcessor` |
| `TraceExporterProperties` | agent-plus-engine `engine.config` | `agent-plus.trace.exporter` 配置类(batchSize=512, scheduleDelay=5000, maxQueueSize=2048) |

### 2.2 业务模块(落库 + 采样)

| 类 | 模块 | 职责 |
|---|---|---|
| `MySqlSpanExporter` | agent-plus-service `ailog.exporter` | 拦截 Span 批量落库,前置 per-trace 采样过滤,载荷剥离 |
| `AiTraceSpan` / `AiTraceSpanPayload` | agent-plus-service `ailog.entity` | 落库实体(主表 + 载荷附表) |
| `AiTraceSampleService` | agent-plus-service `ailog.sample` | **per-trace 采样过滤** + **Redis 决策缓存** + **short-circuit 优化** |
| `IAiTraceSampleConfigService` | agent-plus-interface `ailog.interfaces` | 采样率配置服务接口,含 `resolveSampleRate` 与 `hasUserOrOrgOverride` |
| `AiTraceSampleConfigServiceImpl` | agent-plus-service `ailog.service` | 配置 CRUD + 优先级解析(用户 > 组织 > 全局 > yml)+ 内存缓存 + **Redis Pub/Sub 集群失效广播** |
| `TraceSampleProperties` | agent-plus-service `ailog.config` | `agent-plus.trace.sample` 配置类(`default-sample-rate=1.0000`, `cache-ttl-seconds=60`) |
| `AiTraceSampleConfigController` | agent-plus-web `web.ailog.controller` | 配置管理接口(`/bapi/ai/trace/sample-config`) |

## 3. `@TraceSpan` 注解

```java
package com.iusofts.agentplus.trace.annotation;

import io.opentelemetry.api.trace.SpanKind;
import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TraceSpan {
    /** span 名称,支持硬编码,后续可扩展 SpEL 动态名称。 */
    String name();
    /** span 类型,默认 INTERNAL。 */
    SpanKind kind() default SpanKind.INTERNAL;
    /** span 标签,会设置为 span.setAttribute("label", label)。 */
    String label() default "";
}
```

## 4. 业务属性传播(baggage 桥接)

### 4.1 问题

OTel 的 `Span.attributes` **不从 parent 继承**——子 span 的 `SpanData.getAttributes()` 只包含自己 `setAttribute(...)` 写过的字段。如果只在 root span 上 `setAttribute("ai.operator_id", ...)`:
- root span 自己:有值 ✓
- 所有 child span:没值 ✗ → MySqlSpanExporter 落库为 0,采样判断失准

### 4.2 解决方案:Baggage 传播 + SpanProcessor.onStart 桥接

```
WorkflowEngine.execute (root)
  └─ TraceUtil.setAiAttributes(...)  → 写 baggage: op/orgId
  └─ TraceUtil.setTrialFlag(...)     → 写 baggage: trialFlag
       │
       ▼
WorkflowGraphCompiler.wrapExecutor (node span)
  └─ BusinessAttrSpanProcessor.onStart  ← baggage 已从 root 继承
       └─ span.setAttribute(ai.org_id, ai.operator_id, workflow.trial_flag)
            │
            ▼
LLMNodeExecutor / KnowledgeNodeExecutor / @TraceSpan 子方法
  └─ BusinessAttrSpanProcessor.onStart  ← baggage 跨方法/跨线程/跨 span 自动传
       └─ span.setAttribute(...) 再次补全(子 span 也有)
            │
            ▼
MySqlSpanExporter.export → SpanData.getAttributes() 全部子 span 都有业务键
```

### 4.3 关键设计点

- **baggage 跨线程**:`BatchNodeExecutor` 用 `TraceUtil.getCurrentContext() + runWithContext` 传 Context,baggage 自动跟随
- **`@TraceSpan` 切面**:`TraceSpanAspect` 拦截后启动 span(此时 baggage 为空),方法体内调 `setOperator`/`setAiAttributes` 写 baggage(同时 setAttribute 当前 span attribute),后续子 span 通过 `BusinessAttrSpanProcessor.onStart` 自动补全。**TraceSpanAspect 不需要改**
- **试运行 root**:`AiFlowTrialServiceImpl` 的试运行 root span 用 `TraceUtil.setTrialFlag(YesNoEnums.YES.getCode())` 写 baggage + 当前 span attribute
- **trialFlag baggage key** = `workflow.trial_flag`(与 Span Attribute key 复用)

### 4.4 注册顺序

`BusinessAttrSpanProcessor` 必须**先于** `BatchSpanProcessor` 注册到 `SdkTracerProvider.builder()`(OTel `onStart` 触发顺序 = `addSpanProcessor` 注册顺序):

```java
providerBuilder
    .addSpanProcessor(new BusinessAttrSpanProcessor())  // 先
    .addSpanProcessor(BatchSpanProcessor.builder(exporter)...);  // 后
```

## 5. TraceUtil 能力概览

- **span 模板**:`span(name, kind, action)` / `span(name, kind, parentContext, action)`,自动 `makeCurrent`、异常记录 `StatusCode.ERROR` 与 `recordException`、`finally` 结束 span
- **Context 传递**:`getCurrentContext()` / `runWithContext(context, ...)`,供批处理等异步线程恢复父 Context,使并行迭代的 span 挂到正确 trace
- **业务属性**:
  - `setCallSource` / `setOperator` / `setAiAttributes` —— 同时写 Baggage(跨 span 传播)+ 当前 Span Attribute(落库)
  - `setTrialFlag(Integer)` —— 同上,trialFlag 专用
- **读取**:`getCallSource` / `getSourceId` / `getOperatorId` / `getOrgId` 等,从 Baggage 读
- **traceId**:`currentTraceId()` 从当前 span 取,无有效 span 返回 null

```java
Tracer tracer = GlobalOpenTelemetry.getTracer("ai.workflow.manual");
// 每次动态获取,避免类加载期锁定全局实例
```

## 6. 落库(主表 + 载荷附表)

### 6.1 表结构

- 主表 `ai_trace_span`:`trace_id`(32hex) / `span_id`(16hex) / `parent_span_id`、`span_name`、`span_kind`、`status`、`status_message`、`attributes`(json)、`start_time`/`end_time`(ms 精度)/`duration_ms`、`org_id`、`trial_flag`;索引 `idx_trace_id`、`idx_start_time`
- 附表 `ai_trace_span_payload`:`(trace_id, span_id)` 唯一,`input_payload` / `output_payload`(TEXT)

DDL 见 `docs/sql/agent-plus.sql` 第 470-505 行。

### 6.2 载荷拆分

入参/出参通过约定 attribute key 传递,`MySqlSpanExporter` 导出时剥离,写入 `ai_trace_span_payload`,主表不保留大字段:

| attribute key | 含义 |
|---|---|
| `ap.payload.input` | 入参 JSON |
| `ap.payload.output` | 出参 JSON |

`TraceSpanAspect` 自动把方法入参、返回值序列化为上述两个 key;引擎入口 `WorkflowEngine.initTraceSpan` 也手动写入整体入参/出参载荷。

### 6.3 Token 处理

**不在导出层汇总子级 tokens**——`MySqlSpanExporter` 落库时仅落 span 自身 `ai.tokens`(由 `LLMNodeExecutor` 等写入),父子级 tokens 聚合改由 `AiTraceQueryServiceImpl` 在查询时计算。理由:导出层汇总成本高且会污染主表,查询时聚合更灵活。

## 7. 采样率控制(ai_trace_sample_config)

### 7.1 设计目标

- **trace 完整**:整条 trace 共进退(root 决定),避免 trace 树断链、统计失真
- **跨批/跨实例一致**:Redis 决策缓存,跨 batch/跨节点复用
- **热路径零开销**:short-circuit——yml=1.0 且无 user/org 覆盖时直接放行
- **试运行必采**:trialFlag=1 的 trace 永远落库

### 7.2 配置表 ai_trace_sample_config

字段:`id` / `config_type`(1=全局 2=组织 3=用户)/ `target_id`(全局=0;组织=orgId;用户=userId)/ `sample_rate`(decimal(5,4) 0~1)/ `status`(0禁用 1启用)/ `remark` / `create_by` / `create_time` / `update_by` / `update_time` / `delete_flag`(0正常 1已删除)

唯一键:`uk_type_target (config_type, target_id, delete_flag)` —— 同一作用域未删除态唯一

DDL 见 `docs/sql/upgrade/upgrade_20260810_add_ai_trace_sample_config.sql`(含全局默认 1.0 初始化数据)。数据库:`biz`(与 `ai_trace_span` 同库)。

### 7.3 解析优先级

`AiTraceSampleConfigServiceImpl.resolveSampleRate(userId, orgId)`:

```
1) 用户级  (config_type=3, target_id=userId)  → status=1 才算
2) 组织级  (config_type=2, target_id=orgId)
3) 全局级  (config_type=1, target_id=0)
4) yml 兜底  (agent-plus.trace.sample.default-sample-rate)
```

内存缓存 key=`type:targetId`,带 TTL(`agent-plus.trace.sample.cache-ttl-seconds`,默认 60s,0=不缓存),DB 写入/删除/启停时同步 evict。

### 7.4 运行时落库过滤(per-trace)

落地点:`MySqlSpanExporter.export()` 入口 → `AiTraceSampleService.filter(spans)`。

```
filter(spans)
  │
  ├─[short-circuit] defaultRate>=1.0 && !hasUserOrOrgOverride()
  │                  → 直接 return spans                (热路径零开销)
  │
  └─[per-trace 模式]
       │
       ├─ 按 traceId 分组(LinkedHashMap 保序)
       │
       └─ 每组:
            │
            ├─[Redis 查缓存] ai:trace:sample:trace:{traceId} (5min TTL)
            │     ├─ hit → 直接用
            │     └─ miss → 找 root 决策 → set Redis
            │
            ├─[找 root]  parentSpanId 为空/ROOT_SPAN_ID(0000000000000000)
            │           找不到 fallback 到 group.get(0)(跨批场景)
            │
            ├─[决策规则]
            │     ├─ trialFlag=1 → 必采
            │     ├─ rate<=0    → 必丢
            │     ├─ rate>=1.0  → 必采
            │     └─ 中间比例   → ThreadLocalRandom.nextDouble() < rate
            │
            └─ 整组应用决策:全保或全丢
```

### 7.5 跨批/跨实例一致性

- **Redis 决策缓存** key=`ai:trace:sample:trace:{traceId}`,TTL=5min
- 首个批(通常含 root)写入决策,后续批(仅 child)查 Redis 命中
- 跨实例:A 实例决策后写 Redis,B 实例后续批命中 → 一致
- **Redisson 不可用降级**:决策仅单批内生效,跨批 fallback 到 `group.get(0)` 决策(同 op/orgId,语义等价)

### 7.6 Redis 集群配置缓存失效

- 通道:`ai:trace:sample-config:invalidate`(硬编码)
- 发布:`addConfig` / `updateConfig` / `deleteConfigByIds` / `changeStatus` 末尾 `publishInvalidate()`
- 订阅:`@PostConstruct subscribeInvalidateTopic()` 收到消息后调 `refreshCache()`(本地 cache.clear + 拉全量启用配置)
- 降级:Redisson 不可用时静默跳过,集群不一致由 `cache-tl-seconds=60` TTL 自然收敛

### 7.7 接口(`/bapi/ai/trace/sample-config`)

- `POST /list` 分页查询
- `GET /{id}` 详情
- `POST` 新增(`AiTraceSampleConfigVo`,内含 `@hidden currentUserId` 由 controller 从 `SessionUtil` 注入)
- `PUT` 修改
- `DELETE /{ids}` 软删除
- `POST /changeStatus?id=&status=` 启停
- `POST /refreshCache` 失效运行时缓存
- `GET /resolve?userId=&orgId=` 预览命中的生效采样率(给前端做回显)

## 8. yml 配置

```yaml
agent-plus:
  trace:
    enabled: true                 # OTel SDK 开关
    exporter:
      batch-size: 512             # 每批最大导出条数
      schedule-delay: 5000        # 定时导出间隔(毫秒)
      max-queue-size: 2048        # 队列最大容量
    sample:
      default-sample-rate: 1.0000 # yml 兜底采样率
      cache-ttl-seconds: 60       # 解析缓存 TTL(秒),0=不缓存
```

## 9. 关键约定

- **currentUserId 注入链路**:Controller 从 `SessionUtil.getUserId()` 读取 → 写入 `AiTraceSampleConfigVo.currentUserId`(hidden) → Service 落 `create_by` / `update_by`。**service 模块不能 import `com.iusofts.agentplus.web.common.util.SessionUtil`**,分层依赖
- **delete/changeStatus 没有 vo 参数**,故接口签名加 `Long operatorId` 参数
- **`ModelMapperUtil.map` 实际方法名是 `strictMap`**(STRICT 匹配策略)
- **唯一性约束**:新增/修改时校验同作用域 `(config_type, target_id)` 未删除态不可重复,改时排除自己
- **traceId 32hex**(OTel 128-bit),与历史 trial- 前缀 traceId 区分:`ai_flow_runtime.trial_flag` 字段区分试运行
- **跨服务 baggage 传播(未实现)**:当前仅进程内传播。跨服务需在 `OpenTelemetrySdk.builder()` 配 `TextMapPropagator.composite(W3CTraceContextPropagator, BaggagePropagator)` + HTTP client/server 拦截器

## 10. Commit 历史

| Commit | 说明 |
|---|---|
| `8a20c32` 采样率 | 基础表 `ai_trace_sample_config` + Controller + 优先级解析 + 内存缓存 |
| `77478b4` 节点试运行设置AI属性 | 试运行入口补 `setAiAttributes` |
| `ca73185` 子span业务属性自动注入 | 新增 `BusinessAttrSpanProcessor` + `TraceUtil.setTrialFlag` |
| `e981b9a` MySqlSpanExporter 接入采样 + Redis 集群失效广播 | `AiTraceSampleService.filter` per-span + Redis Pub/Sub 配置失效 |
| `90dc023` AI Trace 采样升级 per-trace + Redis 决策缓存 + short-circuit | per-trace 模式 + Redis 决策缓存 + `hasUserOrOrgOverride` short-circuit |
