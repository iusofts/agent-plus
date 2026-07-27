# 项目功能实现总结

## 已完成的功能

### 1. LLM 模型支持

#### 支持的模型
- **千问（Qwen）** - 阿里云 DashScope（OpenAI 兼容端点）
- **豆包（Doubao）** - 火山引擎（OpenAI 兼容端点）
- 生产环境按数据库 `ai_model` 表动态路由模型渠道，支持任意 OpenAI 兼容供应商。

#### 实现方式
- `ChatModelProvider`（agent-plus-engine `engine.llm`）- 模型提供者接口。
- `DefaultChatModelProvider` - **兜底实现**：未接入模型表时，按 `qwen`/`doubao` 配置选一个渠道，按 temperature 缓存实例。配置项：
  - `dashscope.api-key` / 千问相关（`QwenProperties`）
  - `doubao.api-key` / `doubao.base-url` 等（`DoubaoProperties`）
- `AiModelChatModelProvider`（agent-plus-plugin `plugin.llm`，`@Primary`）- **主实现**：按 `LLMNodeData.modelId` 查 `ai_model` 表路由渠道，统一落 `ai_llm_call_log` 日志，并在 `@TraceSpan("llm.chat")` 内记录 token。
- `LlmModelFactory` - 根据模型元数据构造 LangChain4j `ChatModel`。

---

### 2. 知识库管理

#### 数据库实体（agent-plus-service `library.entity`）
- `AiKnowledgeBase` - 知识库
- `AiKnowledgeDocument` - 文档
- `AiKnowledgeChunk` - 文档分块
- `AiModel` - 模型元数据（含 embedding 模型）

#### 向量库支持
- **Redis** - 当前实现（`langchain4j-community-redis`）
  - `RedisVectorStoreManager` / `RedisKnowledgeRetriever` / `KnowledgeStoreService`（agent-plus-plugin `plugin.vectorstore`）
  - 连接复用 `spring.data.redis.*`；管线配置见 `KnowledgeProperties`（`knowledge.*`：向量维度、索引前缀 `kb:`、文档处理线程池、超时补偿）
- 嵌入模型：千问 `text-embedding-v3`（1024 维），由 `EmbeddingModelFactory` 构造。

#### 文档摄取
- `KnowledgeIngestionService` / `KnowledgeIngestExecutor`（异步线程池 + 有界队列背压）
- `DocumentContentExtractor` 抽取文本，`TextChunker` 分块。
- 定时任务 `KnowledgeDocScheduled` 对处理超时的文档重新入队。

#### Web 接口（agent-plus-web `library.controller`）
- 知识库：`AiKnowledgeBaseController`
- 文档：`AiKnowledgeDocumentController`（增删改查 + 状态变更）
- 文档分块：`AiKnowledgeChunkController`（手动新增/编辑/启停/删除 + 分页查询）

---

### 3. 工作流引擎（agent-plus-engine）

基于 langgraph4j 的 `StateGraph` 驱动，详见 [`workflow-engine.md`](workflow-engine.md)。

#### 支持的节点类型（`FlowNodeType`）
`Start` / `End` / `LLM` / `Knowledge` / `Tool` / `Condition` / `Batch` / `Aggregator`。

#### 关键点
- **上下文隔离修复**：langgraph4j 克隆状态导致 ctx 副本问题，通过 `ExecutionContextTracker`（按 runId 追踪原始 ctx）解决，详见 [`../issues/2026-07-07-ctx.md`](../issues/2026-07-07-ctx.md)。
- **批处理子图**：batch 子节点通过 `parentNode` 归属，预编译为独立子图并行迭代。
- **模板变量解析**：`ParamResolver` 支持 `{{nodeId.param}}` 与 `{{param}}` 两种格式。
- **工具调用**：LLM 节点支持绑定 `toolIds`，多轮工具调用循环（`MAX_TOOL_ITERATIONS`）。
- **会话历史**：LLM 节点可开启 `enableHistory`，通过 `HistoryMessageProvider` 加载最近 N 轮。
- **可观测**：引擎层统一 OTel 埋点（span + token + 节点起止时间），业务侧无需手动补记。

#### 自动装配
- `WorkflowEngineAutoConfiguration` - 只需容器中存在 `ChatModelProvider` bean 即自动创建 `WorkflowEngine`；`KnowledgeRetriever` / `ToolRegistry` / `HistoryMessageProvider` 按需注入。
- `TraceAutoConfiguration` - 初始化 OTel SDK。

---

### 4. AI 链路追踪与日志

- OpenTelemetry 手动 SDK 模式，span 落 `ai_trace_span` + `ai_trace_span_payload`，详见 [`AI Trace日志纪录方案.md`](AI Trace日志纪录方案.md)。
- LLM 调用日志落 `ai_llm_call_log`，知识库检索/文档日志落 `ai_knowledge_retrieval_log` / `ai_knowledge_doc_log`。
- 用量统计 `AiUsageStatServiceImpl`，查询接口 `AiTraceController` / `AiUsageStatController`。

---

### 5. 测试

引擎模块内置端到端冒烟用例（Mock 模型/检索器，无需真实 API Key）：
- `WorkflowSmokeTest` - 简单问答链路（`workflow-sample.json`）
- `WorkflowBatchSmokeTest` - 批处理链路（`workflow-batch-sample.json`）

流程图与工作流 JSON 示例见 [`../test/测试流程图.md`](../test/测试流程图.md)。

---

## 快速开始

### 1. 配置 API Key

在 `application-dev.yml` 中配置千问（或接入 `ai_model` 表动态路由）：

```yaml
dashscope:
  api-key: your-dashscope-api-key
```

### 2. 启动应用

```bash
cd agent-plus-web
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 3. 运行冒烟测试

```bash
mvn -pl agent-plus-engine -am test -Dtest=WorkflowSmokeTest
```

---

## 项目结构

```
agent-plus/
├── agent-plus-common/      # 公共模块
├── agent-plus-core/        # 核心抽象、VO、Trace 工具、DTO
│   └── aiflow/vo/workflow/ # 前端 React Flow 序列化的工作流模型
├── agent-plus-id/          # ID 生成
├── agent-plus-interface/   # 接口定义
├── agent-plus-engine/      # 工作流引擎
│   └── engine/
│       ├── config/         # Spring 自动装配（引擎 + Trace）
│       ├── context/        # ExecutionContext / NodeOutput / NodeTiming / 状态
│       ├── executor/impl/  # 8 类节点执行器
│       ├── graph/          # 图编译、WorkflowState、ExecutionContextTracker
│       ├── knowledge/      # KnowledgeRetriever 接口 + Noop 默认
│       ├── llm/            # ChatModelProvider 接口 + 兜底实现
│       ├── tool/           # ToolRegistry
│       └── trace/          # @TraceSpan AOP 切面
├── agent-plus-plugin/      # 可插拔实现
│   └── plugin/
│       ├── llm/            # AiModelChatModelProvider（主实现）
│       ├── vectorstore/    # Redis 向量库集成
│       ├── document/       # 文档抽取、分块
│       └── tool/           # 内置工具（计算器、当前时间）
├── agent-plus-service/     # 业务服务
│   └── ai*、library、chat、aiflow、ailog、aistat ...
└── agent-plus-web/         # Web 应用（控制器、配置、定时任务）
```

---

## 下一步建议

1. **更多向量库** - 支持 Milvus / Weaviate / PGVector 等。
2. **工作流版本管理** - `ai_flow_version` 已有雏形，可完善版本对比/回滚。
3. **流式输出** - LLM 节点接入 `StreamingChatModel` + langgraph4j `stream(...)`。
4. **中断/恢复** - 接入 langgraph4j `Checkpointer` 实现断点续跑。
5. **性能优化** - 批处理并行调优、模型实例缓存策略。

---

## 技术栈

- **Spring Boot 3.2**
- **LangChain4j 1.17.2**（community 1.17.2-beta27） - AI 能力
- **LangGraph4j 1.8.20** - 工作流编排
- **OpenTelemetry SDK** - 链路追踪
- **MyBatis-Plus** - 数据库操作
- **MySQL** - 数据存储
- **Redis** - 向量存储 + 缓存
