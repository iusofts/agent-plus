# 工作流执行引擎 (agent-plus-engine)

> 位于 `agent-plus-engine` 模块,消费 `agent-plus-core` 中的 `com.iusofts.agentplus.aiflow.vo.workflow.*` DTO,提供一个**轻量化、可嵌入**的 DAG 驱动执行引擎,并与 [LangChain4j](https://docs.langchain4j.dev/) 集成用于 LLM 节点调用。

---

## 1. 设计目标

- **无侵入**: 直接消费前端 React Flow 序列化过来的 `Workflow` JSON,无需额外中间模型。
- **轻量**: 单进程内运行,不依赖工作流引擎中间件(如 Camunda / Flowable);核心仅拓扑排序 + 分支剪枝。
- **可扩展**: 节点执行器、模型工厂、知识库检索均为可插拔接口,业务侧仅需实现少量 Bean。
- **与 LangChain4j 对齐**: LLM 节点直接接受 `dev.langchain4j.model.chat.ChatModel`,便于复用 OpenAI / Ollama / DashScope 等社区实现。

---

## 2. 支持的节点类型

对应 `com.iusofts.agentplus.aiflow.vo.workflow.Node#type`:

| 类型         | Node.type    | 数据类                       | 语义                                       |
|------------|--------------|--------------------------|------------------------------------------|
| 开始         | `Start`      | `StartNodeData`          | 装配全局输入,回填 `defaultValue`                  |
| 结束         | `End`        | `EndNodeData`            | 采集上游输出组装最终结果                             |
| 大模型        | `LLM`        | `LLMNodeData`            | 走 LangChain4j `ChatModel` 调用             |
| 知识库检索      | `Knowledge`  | `KnowledgeNodeData`      | 委托 `KnowledgeRetriever`                  |
| 条件分支       | `Condition`  | `ConditionNodeData`      | 命中分支通过 `Edge.sourceHandle` 剪枝            |
| 变量聚合       | `Aggregator` | `AggregatorNodeData`     | 输出分组: list / map / first                 |
| 批处理        | `Batch`      | `BatchNodeData`          | 通过 `parentNode` 归属子节点,子图并行迭代,聚合 `results/total/success/failed` |

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

Workflow(JSON) ──▶ DagBuilder ──▶ DagGraph ──┐
                                              │
                                    ┌─────────▼────────────┐
        ExecutionContext ◀──────────┤ TopologicalScheduler ├──▶ WorkflowExecutionResult
                                    └──────────────────────┘
```

**关键组件路径 (`com.iusofts.agentplus.engine.*`):**

```
engine
├── WorkflowEngine              // 门面 + Builder
├── WorkflowExecutionResult     // 执行结果
├── dag
│   ├── DagBuilder              // Workflow → DagGraph,环检测
│   ├── DagGraph                // 节点/边只读结构
│   └── TopologicalScheduler    // Kahn 算法 + 分支剪枝
├── context
│   ├── ExecutionContext        // 全局输入/环境变量/节点输出/状态
│   ├── NodeOutput              // 单节点产物 + chosenBranch
│   └── NodeExecutionStatus     // PENDING/RUNNING/SUCCESS/SKIPPED/FAILED
├── executor
│   ├── NodeExecutor            // SPI 接口
│   ├── NodeExecutorRegistry    // type → executor
│   └── impl/*                  // 7 类内置执行器
├── llm
│   └── ChatModelProvider       // 业务侧提供 LangChain4j ChatModel
├── knowledge
│   ├── KnowledgeRetriever      // 业务侧提供向量检索
│   └── NoopKnowledgeRetriever  // 默认空实现
├── util
│   └── ParamResolver           // paramMapKey / {{node.name}} 解析
├── exception/WorkflowExecutionException
└── config/WorkflowEngineAutoConfiguration  // Spring Boot 自动装配
```

---

## 4. 执行时序

1. **编译阶段** — `DagBuilder.build(workflow)`
    - 校验节点 id 唯一、边 source/target 合法
    - DFS 三色标记检测环,发现环立即抛 `WorkflowExecutionException`
    - 计算 `startNodeIds`(无 incoming 或 `type == Start`)

2. **上下文初始化** — `new ExecutionContext(runId, config, inputs)`
    - `envVars` 由 `WorkflowConfig.envVars` 的 `defaultValue` 初始化
    - `globalInputs` 复制传入的入参,支持 `defaultValue` 回填

3. **拓扑调度** — `TopologicalScheduler.run(ctx)`,基于 Kahn 算法的变体:
    - 为每个节点维护 `pending`(剩余待决 incoming 边)与 `hasLive`(是否至少有一条 incoming 被激活)
    - 从 `startNodeIds` 起步,`hasLive` 置 true
    - 每次弹出节点:
        - 若 `hasLive == false` → 标记为 `SKIPPED`,继续向下游传递剪枝信号
        - 否则调用对应 `NodeExecutor`,写入 `NodeOutput`
    - **条件节点特殊处理**: 执行结果携带 `chosenBranch`,只有 `Edge.sourceHandle` 匹配的下游边保持 alive,其它边视为 dead

4. **结果汇总** — 遍历所有 `type=End` 且状态为 `SUCCESS` 的节点,合并 `outputParams` 为最终 `output`

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
- 命中的 `condition.id` 通过 `NodeOutput.chosenBranch` 上报
- 调度器把 `Edge.sourceHandle` 匹配 chosen 的边视为 alive,其余剪掉
- 全部落空时 `chosenBranch = "else"`,约定 `sourceHandle == "else"` 的边为兜底分支
- 前端 `sourceHandle` 若采用 `condition:<condition.id>` 前缀写法,调度器自动脱前缀匹配,与裸 id 写法兼容

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

数据结构上"起终点都连回 batch 自己"呈环,`DagBuilder` 在环检测阶段自动剔除 `batch-internal-target` 边,故不会误报"存在环"。

**执行流程**:

1. 从 `inputParams` 首项解析出集合 `items`
2. 对每个 `item[i]` 用 `ExecutionContext.newScope("<batchId>#<i>")` 生成隔离作用域,并把 `{item, index, items}` + inputParams 结果写入该作用域下的 `<batchId>` 输出
3. 用 `Executors.newFixedThreadPool(maxParallel)` 并行触发子图迭代;子图内部复用同一个 `TopologicalScheduler`
4. 迭代结束后从子作用域中收集每个 sub-node 的 outputs 聚合为 `Map<nodeId, outputs>`
5. 单个 iteration 抛异常不中断其它 iteration,失败位置在 `results` 中为 `null`,`failed` 计数递增

**子作用域可读变量**(子节点用 `{{<batchId>.item}}` / `{{<batchId>.index}}` 引用):

| key | 说明 |
|-----|------|
| `item` | 当前迭代的元素 |
| `index` | 当前迭代下标 |
| `items` | 完整集合(便于聚合类子节点使用) |
| ... | `inputParams` 解析后的所有键(直接透传) |

**主作用域输出**(下游节点通过 `{{<batchId>.results}}` 等引用):

| key | 类型 | 说明 |
|-----|------|------|
| `results` | `List<Map<String, Map<String, Object>>>` | 每个 item 对应一个 `{subNodeId: outputs}`,失败位为 null |
| `total` | `Integer` | items 数量 |
| `success` | `Integer` | 成功完成的迭代数 |
| `failed` | `Integer` | `total - success` |

**关键行为**:

- `maxParallel` 未配置或 ≤ 0 → 默认并发数 4;传 1 则严格串行
- 子作用域读取时,batch 之外的上游节点仍然可见(读取回退到父作用域),但**不能**跨迭代引用彼此的数据
- 暂不支持嵌套 batch,构建期即报错
- batch 上若未挂任何子节点,则退化为旧行为,仅输出 `items` / `size`

---

## 7. 与 LangChain4j 集成

`LLMNodeExecutor` 直接依赖 `dev.langchain4j.model.chat.ChatModel`。业务侧只需实现 `ChatModelProvider`:

```java
@Component
public class DashScopeChatModelProvider implements ChatModelProvider {

    private final ModelService modelService;                       // 你的模型元数据服务

    @Override
    public ChatModel provide(LLMNodeData nodeData) {
        // 1. 根据 nodeData.getModel() 查询模型元数据 (baseUrl, apiKey, modelName)
        var meta = modelService.getById(nodeData.getModel());

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
  ├─ inputParams    → 经 ParamResolver.resolveInputs 装配为 userMessage 内容
  ├─ retryCount     → 引擎在 provider 之外做重试兜底
  └─ errorHandling  → throw / custom / continue
```

**输出映射策略**:

- 单个 `outputParam` → 完整模型文本写入该字段
- 多个 `outputParam` → 尝试将模型文本按 JSON 解析,失败则退化为写入首字段
- 无 `outputParam` → 兜底 `text`

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
        return engine.execute(req.getWorkflow(), req.getConfig(), req.getInputs());
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

WorkflowExecutionResult result = engine.execute(wf, wf.getConfig(), inputs);
System.out.println(result.getOutput());          // End 节点合并结果
System.out.println(result.getNodeStatus());      // 每个节点的执行态
```

### 8.3 自定义节点执行器

只需实现 `NodeExecutor`:

```java
public class HttpCallNodeExecutor implements NodeExecutor {
    @Override public String type() { return "HttpCall"; }
    @Override public NodeOutput execute(Node node, ExecutionContext ctx) {
        // 1. 从 ctx 读取输入
        // 2. 发起 HTTP
        // 3. 返回 new NodeOutput(node.getId(), Map.of("response", body))
    }
}
```

然后 `builder().registerExecutor(new HttpCallNodeExecutor())`,并在前端节点 `type` 上填 `HttpCall`,DAG 会自动接入。

---

## 9. 扩展点一览

| 扩展点                       | 何时实现                                   |
|---------------------------|----------------------------------------|
| `ChatModelProvider`       | 必须。对接你的模型元数据表 + LangChain4j 渠道         |
| `KnowledgeRetriever`      | 使用了 Knowledge 节点时。默认 Noop 返回空          |
| `NodeExecutor` + register | 需要新增节点类型时(HTTP / SQL / Function 调用 等) |
| `ParamResolver` 占位规则      | 需要更复杂的表达式(如 SpEL)时 fork 或包装             |

---

## 10. 局限与后续演进

- **并行调度**: 当前 `TopologicalScheduler` 单线程串行;分支并行可基于 `CompletableFuture` 扩展,状态表已线程安全。
- **Batch 子图迭代**: 当前实现仅暴露 `items/size`,不递归 `parentNode` 子图。若需真正的 fan-out,可实现 `SubgraphRunner` 并注入到 `BatchNodeExecutor`。
- **中断/恢复**: 尚未持久化 `ExecutionContext`。可对接 Redis 存 `NodeOutput` 快照实现断点续跑。
- **流式输出**: `LLMNodeExecutor` 现走同步 `chat`;流式 SSE 场景可注入 `StreamingChatModel` 并扩展 `NodeExecutor` 返回 `Flux<NodeOutput>` 语义。

---

## 11. 目录索引

- 前端节点模型: `agent-plus-core/src/main/java/com/iusofts/agentplus/aiflow/vo/workflow/**`
- 引擎实现: `agent-plus-engine/src/main/java/com/iusofts/agentplus/engine/**`
- 自动装配: `agent-plus-engine/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
