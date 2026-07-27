# 工作流执行引擎 (agent-plus-engine)

> 位于 `agent-plus-engine` 模块,消费 `agent-plus-core` 中的 `com.iusofts.agentplus.aiflow.vo.workflow.*` DTO,提供一个**基于 [langgraph4j](https://github.com/bsorrentino/langgraph4j) 的 StateGraph 驱动**执行引擎,并与 [LangChain4j](https://docs.langchain4j.dev/) 集成用于 LLM 节点调用。

---

## 1. 设计目标

- **无侵入**: 直接消费前端 React Flow 序列化过来的 `Workflow` JSON,无需额外中间模型。
- **借力开源**: DAG 编排、拓扑执行、条件分支、子图组合等重活交给 langgraph4j 的 `StateGraph`,引擎只负责节点语义与前端 JSON 的适配。
- **可扩展**: 节点执行器、模型工厂、知识库检索均为可插拔接口,业务侧仅需实现少量 Bean。
- **与 LangChain4j 对齐**: LLM 节点直接接受 `dev.langchain4j.model.chat.ChatModel`,便于复用 OpenAI / Ollama / DashScope 等社区实现。

---

## 2. 支持的节点类型

对应 `com.iusofts.agentplus.aiflow.vo.workflow.Node#type`:

| 类型         | Node.type    | 数据类                       | 语义                                       |
|------------|--------------|--------------------------|------------------------------------------|
| 开始         | `Start`      | `StartNodeData`          | 装配全局输入,回填 `defaultValue`                  |
| 结束         | `End`        | `EndNodeData`            | 采集上游输出组装最终结果                             |
| 大模型        | `LLM`        | `LLMNodeData`            | 走 LangChain4j `ChatModel` 调用,支持工具调用循环与会话历史 |
| 知识库检索      | `Knowledge`  | `KnowledgeNodeData`      | 委托 `KnowledgeRetriever`                  |
| 工具         | `Tool`       | `ToolNodeData`           | 委托 `ToolRegistry` 执行工具(需注入 ToolRegistry + ToolQueryProvider) |
| 条件分支       | `Condition`  | `ConditionNodeData`      | 命中分支通过 langgraph4j `addConditionalEdges` 剪枝 |
| 变量聚合       | `Aggregator` | `AggregatorNodeData`     | 输出分组: list / map / first                 |
| 批处理        | `Batch`      | `BatchNodeData`          | 通过 `parentNode` 归属子节点,子图预编译为独立 `StateGraph`,主图迭代时并行调用 |

---

## 3. 架构总览

```
                    ┌────────────────────────┐
                    │      WorkflowEngine    │
                    └───────────┬────────────┘
                                │ builder / autoConfig
              ┌─────────────────┼─────────────────┐
              ▼                 ▼                 ▼
     ┌──────────────┐  ┌──────────────┐  ┌────────────────┐
     │ ChatModel    │  │ Knowledge    │  │ Node executors │
     │ Provider     │  │ Retriever    │  │ registry       │
     │ (LangChain4j)│  │ (business)   │  │ (Start/LLM/...) │
     └──────────────┘  └──────────────┘  └────────────────┘

Workflow(JSON) ──▶ WorkflowGraphCompiler ──▶ CompiledGraph(main) + CompiledGraph(batchSub)*
                                                    │
                                          ┌─────────▼──────────────┐
        ExecutionContext ◀────────────────┤ langgraph4j runtime    ├──▶ WorkflowExecutionResult
                                          │ (StateGraph.invoke)    │
                                          └────────────────────────┘
```

**关键组件路径 (`com.iusofts.agentplus.engine.*`):**

```
engine
├── WorkflowEngine              // 门面 + Builder(execute(WorkflowExecuteRequest))
├── WorkflowExecuteRequest      // 执行入参(workflow/config/inputs + flowId/operatorId/orgId/trialFlag/flowName)
├── WorkflowExecutionResult     // 执行结果(runId/output/nodeOutputs/nodeStatus/nodeTimings)
├── graph
│   ├── WorkflowState           // langgraph4j AgentState 子类,承载 ExecutionContextTracker 引用
│   ├── ExecutionContextTracker // 按 runId 静态追踪原始 ctx,规避 langgraph4j 克隆状态问题
│   └── WorkflowGraphCompiler   // Workflow → langgraph4j StateGraph(主图 + 批处理子图)
├── context
│   ├── ExecutionContext        // 全局输入/环境变量/节点输出/状态/时间/批处理子图注册表/OTel root context
│   ├── NodeOutput              // 单节点产物 + chosenBranch(供条件路由读取)
│   ├── NodeTiming              // 单节点起止时间与耗时(供落库)
│   └── NodeExecutionStatus     // PENDING/RUNNING/SUCCESS/SKIPPED/FAILED
├── executor
│   ├── NodeExecutor            // SPI 接口
│   ├── NodeExecutorRegistry    // type → executor
│   └── impl/*                  // 8 类内置执行器(Start/End/LLM/Knowledge/Tool/Condition/Aggregator/Batch)
├── llm
│   ├── ChatModelProvider       // 业务侧提供 LangChain4j ChatModel + chat(AiChatRequest)
│   └── DefaultChatModelProvider// 千问/豆包兜底实现
├── knowledge
│   ├── KnowledgeRetriever      // 业务侧提供向量检索(单/多知识库)
│   └── NoopKnowledgeRetriever  // 默认空实现
├── tool
│   └── ToolRegistry            // 工具注册与执行
├── history
│   └── HistoryMessageProvider  // 可选:加载会话历史消息
├── trace
│   └── TraceSpanAspect         // @TraceSpan 注解的 AOP 切面(OTel span)
├── util
│   └── ParamResolver           // paramMapKey / {{node.name}} 解析
├── exception/WorkflowExecutionException
└── config
    ├── WorkflowEngineAutoConfiguration  // Spring Boot 自动装配
    └── TraceAutoConfiguration           // OpenTelemetry SDK 初始化
```

> 链路追踪:执行入口 `WorkflowEngine.execute` 在最外层开 OTel root span,其 `traceId` 即本次执行的 `runId`;各节点执行器统一 `TraceUtil.setAiAttributes(...)` 写业务属性,`chat`/落库由业务侧完成。详见 [`AI Trace日志纪录方案.md`](AI Trace日志纪录方案.md)。

---

## 4. 执行时序

1. **编译阶段** — `WorkflowGraphCompiler.compile(workflow)`
    - 校验节点 id 唯一、边 source/target 合法
    - 按 `Node.parentNode` 识别批处理子节点集合,单独编译成独立的 `CompiledGraph<WorkflowState>`
    - 主图剔除 batch-internal 节点与 4 类约定 handle 的边(见 §6.5)后交给 langgraph4j 的 `StateGraph`
    - 每个 workflow 节点 → `StateGraph.addNode(id, AsyncNodeAction)`,动作内部委托给对应 `NodeExecutor`
    - 出边分派:
        - `Condition` 节点 → `addConditionalEdges(id, edgeAction, branchMap)`;`edgeAction` 从 `NodeOutput.chosenBranch` 读取并映射到目标节点
        - 其它节点 → 逐条 `addEdge(source, target)`
        - 无出边节点 → `addEdge(id, END)`
    - `StateGraph.compile()` 完成后,langgraph4j 自身会对环、悬挂节点等做静态校验

2. **运行时** — `WorkflowEngine.execute(WorkflowExecuteRequest)`
    - 最外层开 OTel root span,以其 `traceId` 作为 `runId`(SDK 未初始化时回退请求里的 `runId`)
    - `new ExecutionContext(runId, config, inputs, flowId, operatorId, orgId)`:`envVars` 由 `WorkflowConfig.envVars` 的 `defaultValue` 初始化;`globalInputs` 复制传入的入参;并记录 root context 供节点 span 挂靠
    - 编译后的批处理子图逐个注册进 `ExecutionContext.batchSubGraphs`,供 `BatchNodeExecutor` 后续调用
    - ctx 注册进 `ExecutionContextTracker`(按 runId),`mainGraph.invoke(Map.of("ctx", tracker))` 交由 langgraph4j 驱动;节点动作通过 tracker 拿到**原始** ctx(规避 langgraph4j 克隆状态导致副本丢失的问题,见 [`../issues/2026-07-07-ctx.md`](../issues/2026-07-07-ctx.md))
        - 更新 `RUNNING` → `wrapExecutor` 记录 `NodeTiming` 起止时间 → 调用 `NodeExecutor.execute` → 写 `NodeOutput` → 更新 `SUCCESS/FAILED`
        - `NodeAction` 返回空 map,状态合并只发生在 ctx 内部(原地并发写),不需要额外 Channel Reducer
    - `finally` 中 `ExecutionContextTracker.removeRun(runId)` 清理,避免内存泄漏
    - 未被路由到的节点保持 `PENDING`,执行结束后统一置为 `SKIPPED` 并写入空输出

3. **结果汇总** — 遍历所有 `type=End` 且状态为 `SUCCESS` 的节点,合并 `outputParams` 为最终 `output`

---

## 5. 参数映射与占位符

`InputParam.paramMapKey` 是引擎的核心数据流描述:

```java
input.paramMapKey.nodeId  // 引用哪个上游节点
input.paramMapKey.name    // 引用该节点哪个 output 名
```

`ParamResolver` 提供两个入口:

- 结构化: `resolveInputs(List<InputParam>, ctx)` → `Map<paramName, value>`
- 字符串占位: `renderTemplate("...{{node1.text}}...", ctx)`

**特殊 nodeId 别名:**

| nodeId              | 含义                          |
|---------------------|-----------------------------|
| `env`               | `WorkflowConfig.envVars`    |
| `inputs` / `start`  | 传入 `execute(...)` 的全局输入     |
| 其它任意值               | 上游节点的 `NodeOutput.outputs` |

---

## 6. 条件分支剪枝

- `ConditionNodeExecutor` 顺序评估 `conditions`(每个含 `logic=and/or` + 若干 rules)
- 命中的 `condition.id` 通过 `NodeOutput.chosenBranch` 上报,写回 ctx
- 编译阶段 `WorkflowGraphCompiler` 已把该条件节点转成 `addConditionalEdges`,分支 key 取自 `Edge.sourceHandle`(兼容 `condition:<id>` 前缀),运行期由 langgraph4j 的 EdgeAction 读取 `chosenBranch` 选择下游
- 全部落空时 `chosenBranch = "else"`,约定 `sourceHandle == "else"` 的边为兜底分支;若没有兜底边则回落到 `END`

**支持的 operator**(不区分大小写,推荐使用符号 / 蛇形写法):

| 运算符             | 含义     | 备注                                                       |
|-----------------|--------|----------------------------------------------------------|
| `==` / `eq`     | 等于     | 数字按数值比较,布尔按 `Boolean.parseBoolean`,其它按字符串                 |
| `!=` / `ne`     | 不等于    | 同 `==` 取反                                                |
| `>` / `gt`      | 大于     | 双方按数值解析;解析失败退化为字符串字典序                                    |
| `>=` / `gte`    | 大于等于   | 同上                                                       |
| `<` / `lt`      | 小于     | 同上                                                       |
| `<=` / `lte`    | 小于等于   | 同上                                                       |
| `contains`      | 包含     | 字符串→子串;`Collection`/数组→元素相等;`Map`→包含 key                 |
| `not_contains`  | 不包含    | 同 `contains` 取反                                          |
| `len_gt`        | 长度大于   | 长度定义:`CharSequence.length`、`Collection.size`、`Map.size`、数组 length |
| `len_gte`       | 长度大于等于 | 同上                                                       |
| `len_lt`        | 长度小于   | 同上                                                       |
| `len_lte`       | 长度小于等于 | 同上                                                       |
| `is_empty`      | 为空     | `null` / 空字符串 / 空集合 / 空 Map / 空数组 均视为空                    |
| `not_empty`     | 不为空    | 同 `is_empty` 取反                                          |

**变量类型 → 可用运算符**(前端建议限制):

| 类型      | 支持的运算符                                                                                                          |
|---------|-----------------------------------------------------------------------------------------------------------------|
| string  | `==`, `!=`, `contains`, `not_contains`, `len_gt`, `len_gte`, `len_lt`, `len_lte`, `is_empty`, `not_empty`       |
| number  | `==`, `!=`, `>`, `<`, `>=`, `<=`                                                                                |
| boolean | `==`, `!=`                                                                                                      |
| array   | `contains`, `not_contains`, `len_gt`, `len_gte`, `len_lt`, `len_lte`, `is_empty`, `not_empty`                   |
| object  | `is_empty`, `not_empty`                                                                                         |

---

## 6.5 批处理子图

Batch 节点作为可视化"容器",通过两条通道识别子图:

- **子节点归属**: `node.parentNode == batchId` 的所有节点组成迭代子图
- **边分类**: 通过 batch 节点上的 4 个约定 handle 划分边角色

| handle | 位置 | 语义 |
|--------|------|------|
| `batch-external-target` | 入边 `targetHandle` | 主图上游 → batch(数据入口) |
| `batch-internal-source` | 出边 `sourceHandle` | batch → 子图起点(迭代进入) |
| `batch-internal-target` | 入边 `targetHandle` | 子图终点 → batch(迭代回流) |
| `batch-external-source` | 出边 `sourceHandle` | batch → 主图下游(迭代完成) |

`WorkflowGraphCompiler` 在编译主图时会剔除 `batch-internal-source` / `batch-internal-target` 边以及所有 batch-internal 节点;同一批 batch 子节点会被单独打包成一个独立 `StateGraph`,以 `batch-internal-source` 的 target 作为入口,内部普通边照常连接,尾节点(loop-back 的 source)由 langgraph4j 默认走向 `END`。这样主图与每个 batch 子图都是天然的 DAG,不会误报环。

**执行流程**:

1. 从 `inputParams` 首项解析出集合 `items`
2. `BatchNodeExecutor` 从 `ExecutionContext.getBatchSubGraph(batchId)` 拿到预编译的 `CompiledGraph<WorkflowState>`
3. 对每个 `item[i]` 用 `ExecutionContext.newScope("<batchId>#<i>")` 生成隔离作用域,并把 `{item, index, items}` + inputParams 结果写入该作用域下的 `<batchId>` 输出
4. 用 `Executors.newFixedThreadPool(maxParallel)` 并行触发子图迭代;每次迭代都是一次 `compiledSubGraph.invoke(Map.of("ctx", scopedCtx))`
5. 迭代结束后从子作用域中收集每个 sub-node 的 outputs 聚合为 `Map<nodeId, outputs>`
6. 单个 iteration 抛异常不中断其它 iteration,失败位置在 `results` 中为 `null`,`failed` 计数递增

**子作用域可读变量**(子节点用 `{{<batchId>.item}}` / `{{<batchId>.index}}` 引用):

| key | 说明 |
|-----|------|
| `item` | 当前迭代的元素 |
| `index` | 当前迭代下标 |
| `items` | 完整集合(便于聚合类子节点使用) |
| ... | `inputParams` 解析后的所有键(直接透传) |

**主作用域输出**(下游节点引用):

- **未配置 `outputParams`**:输出默认键 `output`,类型 `List<Map<String, Map<String, Object>>>` —— 每个 item 对应一个 `{subNodeId: outputs}`,失败位为 null。
- **配置了 `outputParams`**:按每个 outputParam 的 `name` 聚合,值为 `List<Object>`(逐 item 取 `paramMapKey` 指向的子节点输出,失败位为 null)。

无论是否配置,都附带统计字段:

| key | 类型 | 说明 |
|-----|------|------|
| `total` | `Integer` | items 数量 |
| `success` | `Integer` | 成功完成的迭代数 |
| `failed` | `Integer` | `total - success` |

**关键行为**:

- `maxParallel` 未配置或 ≤ 0 → 默认并发数 4;传 1 则严格串行
- 子作用域读取时,batch 之外的上游节点仍然可见(读取回退到父作用域),但**不能**跨迭代引用彼此的数据
- 暂不支持嵌套 batch,编译期即报错
- batch 上若未挂任何子节点,则退化为旧行为,仅输出 `output`(=items)/ `total` / `success` / `failed`

---

## 7. 与 LangChain4j 集成

`LLMNodeExecutor` 通过 `ChatModelProvider` 完成模型调用。接口有两个方法:`provide(LLMNodeData)` 返回 LangChain4j `ChatModel`(用于自建调用),`chat(AiChatRequest)` 直接完成一次对话(内部处理工具调用循环并统一落 `ai_llm_call_log`)。生产实现见 agent-plus-plugin 的 `AiModelChatModelProvider`(`@Primary`,按 `modelId` 查库路由):

```java
@Component
public class DashScopeChatModelProvider implements ChatModelProvider {

    private final ModelService modelService;                       // 你的模型元数据服务

    @Override
    public ChatModel provide(LLMNodeData nodeData) {
        // 1. 根据 nodeData.getModelId() 查询模型元数据 (baseUrl, apiKey, modelName)
        var meta = modelService.getById(nodeData.getModelId());

        // 2. 构造 LangChain4j 的 OpenAI 兼容渠道(DashScope 提供 OpenAI 兼容端点)
        return OpenAiChatModel.builder()
                .baseUrl(meta.getBaseUrl())
                .apiKey(meta.getApiKey())
                .modelName(meta.getModelName())
                .temperature(nodeData.getTemperature())
                .timeout(Duration.ofSeconds(
                        nodeData.getTimeout() == null ? 60 : nodeData.getTimeout()))
                .build();
    }
}
```

引擎侧的调用链:

```
LLMNodeData
  ├─ systemPrompt   → 经 ParamResolver.renderTemplate 渲染 {{...}} 占位
  ├─ userPrompt     → 同样渲染;为空时由 inputParams 兜底拼装
  ├─ inputParams    → 经 ParamResolver.resolveInputs 装配为 userMessage 内容
  ├─ toolIds        → 绑定工具,模型请求时进入多轮工具调用循环(最多 MAX_TOOL_ITERATIONS 轮)
  ├─ enableHistory  → 开启后经 HistoryMessageProvider 加载最近 N 轮会话历史
  └─ errorHandling  → throw(默认) / custom(用 customErrorContent) / continue(空串)
```

**输出映射策略**(始终包含默认输出 `text` / `reasoningContent` / `usage`):

- 单个自定义 `outputParam` → 尝试将模型文本按 JSON 解析,失败则原样写入该字段
- 多个自定义 `outputParam` → 尝试将模型文本按 JSON 解析,按参数名取值填充(不覆盖默认输出)
- 无自定义 `outputParam` → 仅默认输出

**推荐**: 多字段结构化输出时,系统提示词里让模型返回严格 JSON。

---

## 8. 使用示例

### 8.1 Spring Boot 场景

引入依赖(内部模块,已在父 pom 管控版本):

```xml
<dependency>
    <groupId>com.iusofts</groupId>
    <artifactId>agent-plus-engine</artifactId>
</dependency>
```

自动装配已就绪 —— 只要容器中存在 `ChatModelProvider` Bean,`WorkflowEngine` 会自动创建:

```java
@RestController
@RequiredArgsConstructor
public class WorkflowRunController {

    private final WorkflowEngine engine;

    @PostMapping("/workflow/run")
    public WorkflowExecutionResult run(@RequestBody RunRequest req) {
        WorkflowExecuteRequest exec = WorkflowExecuteRequest.builder()
                .workflow(req.getWorkflow())
                .config(req.getConfig())
                .inputs(req.getInputs())
                .flowId(req.getFlowId())        // 可选,用于链路/日志关联
                .operatorId(req.getOperatorId())
                .orgId(req.getOrgId())
                .flowName(req.getFlowName())    // 可选,作为 root span 的 label
                .build();
        return engine.execute(exec);
    }
}
```

### 8.2 手工组装(单元测试 / 命令行)

```java
WorkflowEngine engine = WorkflowEngine.builder()
        .chatModelProvider(new DashScopeChatModelProvider(modelService))
        .knowledgeRetriever(new MyMilvusRetriever(vectorStore))
        .registerExecutor(new MyCustomHttpNodeExecutor())   // 追加自定义节点
        .build();

Workflow wf = objectMapper.readValue(json, Workflow.class);
Map<String, Object> inputs = Map.of("question", "帮我写一封请假邮件");

WorkflowExecutionResult result = engine.execute(
        WorkflowExecuteRequest.simple(wf, wf.getConfig(), inputs, "请假邮件流程"));
System.out.println(result.getRunId());           // 即 OTel traceId
System.out.println(result.getOutput());          // End 节点合并结果
System.out.println(result.getNodeStatus());      // 每个节点的执行态
System.out.println(result.getNodeTimings());     // 每个节点起止时间/耗时
```

### 8.3 自定义节点执行器

只需实现 `NodeExecutor`:

```java
public class HttpCallNodeExecutor implements NodeExecutor {
    @Override public FlowNodeType type() { return FlowNodeType.HTTP; }   // 若需新增枚举可另行扩展
    @Override public NodeOutput execute(Node node, ExecutionContext ctx) {
        // 1. 从 ctx 读取输入
        // 2. 发起 HTTP
        // 3. 返回 new NodeOutput(node.getId(), Map.of("response", body))
    }
}
```

然后 `builder().registerExecutor(new HttpCallNodeExecutor())`,并在前端节点 `type` 上填对应值,编译器会自动为该节点注册一个 `AsyncNodeAction`。

---

## 9. 扩展点一览

| 扩展点                       | 何时实现                                   |
|---------------------------|----------------------------------------|
| `ChatModelProvider`       | 必须。对接你的模型元数据表 + LangChain4j 渠道         |
| `KnowledgeRetriever`      | 使用了 Knowledge 节点时。默认 Noop 返回空          |
| `ToolRegistry` + `ToolQueryProvider` | 使用了 Tool 节点或 LLM 工具调用时,注册工具并按 toolId 查元数据 |
| `HistoryMessageProvider`  | LLM 节点需加载会话历史(`enableHistory`)时         |
| `NodeExecutor` + register | 需要新增节点类型时(HTTP / SQL / Function 调用 等) |
| `SpanExporter`            | 需要 span 落库时(agent-plus-service 提供 `MySqlSpanExporter`) |
| `ParamResolver` 占位规则      | 需要更复杂的表达式(如 SpEL)时 fork 或包装             |

---

## 10. 局限与后续演进

- **并行调度**: langgraph4j 在同一节点有多条出边时天然支持并行分支;当前节点动作内部通过共享的 `ExecutionContext`(`ConcurrentHashMap` 承载状态)读写,若引入复杂的并行合并语义可切到 langgraph4j 的 `Channel<Reducer>` 定义原生状态槽。
- **中断/恢复**: langgraph4j 自身提供 `CompileConfig` + `Checkpointer` 机制,当前尚未接入;可对接 Redis / DB 存 `StateSnapshot` 实现断点续跑。
- **流式输出**: `LLMNodeExecutor` 现走同步 `chat`;流式 SSE 场景可注入 `StreamingChatModel` 并使用 langgraph4j 的 `CompiledGraph.stream(...)` 拿到 `AsyncGenerator<NodeOutput<State>>` 逐节点回推。
- **可视化**: langgraph4j 的 `CompiledGraph.getGraph(Type)` 支持导出 Mermaid / PlantUML,可直接接到运维面板做流程图渲染。

---

## 11. 目录索引

- 前端节点模型: `agent-plus-core/src/main/java/com/iusofts/agentplus/aiflow/vo/workflow/**`
- 引擎实现: `agent-plus-engine/src/main/java/com/iusofts/agentplus/engine/**`
- 链路追踪工具: `agent-plus-core/src/main/java/com/iusofts/agentplus/trace/**`
- 生产模型/向量库实现: `agent-plus-plugin/src/main/java/com/iusofts/agentplus/plugin/**`
- 自动装配: `agent-plus-engine/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`(`WorkflowEngineAutoConfiguration` + `TraceAutoConfiguration`)
- 依赖版本: `pom.xml` → `langgraph4j.version` / `langchain4j.version`
- 链路追踪方案: [`AI Trace日志纪录方案.md`](AI Trace日志纪录方案.md)
- 测试流程图与 JSON 示例: [`../test/测试流程图.md`](../test/测试流程图.md)
