# 项目功能实现总结

## 已完成的功能

### 1. LLM 模型支持

#### 支持的模型
- **千问（Qwen）** - 阿里云
  - 模型：qwen-turbo, qwen-plus, qwen-max 等
  - API 兼容 OpenAI 格式
  
- **豆包（Doubao）** - 火山引擎
  - 支持自定义模型端点
  - API 兼容 OpenAI 格式

#### 实现文件
- `DefaultChatModelProvider.java` - 通用模型提供者
- 配置项：
  - `dashscope.api-key` - 千问 API Key
  - `doubao.api-key` - 豆包 API Key
  - `doubao.base-url` - 豆包 API 地址

---

### 2. 知识库管理

#### 数据库实体
- `AiKnowledgeBase` - 知识库
- `AiKnowledgeDocument` - 文档
- `AiKnowledgeChunk` - 文档分块

#### 向量库支持
- **Chroma** - 默认支持
  - LangChain4j 集成
  - 配置项：
    - `chroma.enabled` - 是否启用
    - `chroma.url` - Chroma 地址

#### 实现文件
- `ChromaKnowledgeRetriever.java` - Chroma 检索器
- 支持嵌入模型：text-embedding-v3（千问）

---

### 3. 工作流引擎优化

#### 修复的问题
- **批处理子图上下文问题** - 修复了子图执行时上下文丢失的问题
- **ExecutionContextTracker** - 更新为支持作用域键
- **模板变量解析** - 支持 `{{nodeId.param}}` 和 `{{param}}` 两种格式

#### 配置优化
- `WorkflowEngineAutoConfiguration` - 自动配置更新
- 支持通过 Spring 注入 ChatModelProvider 和 KnowledgeRetriever

---

### 4. 测试流程设计

#### 测试工作流
- `workflow-test-sample.json` - 简单问答工作流
- `WorkflowQuickTest.java` - 快速测试类

#### 测试文档
- `TEST_GUIDE.md` - 完整测试指南

---

## 快速开始

### 1. 配置 API Key

在 `application-dev.yml` 中添加：

```yaml
dashscope:
  api-key: your-dashscope-api-key
```

或通过环境变量：
```bash
export DASHSCOPE_API_KEY=your-api-key
```

### 2. 启动应用

```bash
cd agent-plus-web
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 3. 运行测试

```bash
cd agent-plus-engine
mvn test -Dtest=WorkflowQuickTest
```

---

## 项目结构

```
agent-plus/
├── agent-plus-common/      # 公共模块
├── agent-plus-core/        # 核心抽象和 VO
├── agent-plus-id/          # ID 生成
├── agent-plus-engine/      # 工作流引擎
│   ├── src/main/java/
│   │   ├── config/         # Spring 配置
│   │   ├── executor/       # 节点执行器
│   │   ├── graph/          # 图编译和状态
│   │   ├── knowledge/      # 知识库检索
│   │   └── llm/            # LLM 模型
│   └── src/test/           # 测试
├── agent-plus-interface/   # 接口定义
├── agent-plus-plugin/      # 插件（预留）
├── agent-plus-service/     # 业务服务
│   └── src/main/java/
│       └── ai/entity/      # 知识库实体
└── agent-plus-web/         # Web 应用
```

---

## 下一步建议

1. **知识库管理 API** - 实现文档上传、分块、存储的完整 API
2. **模型管理** - 支持在界面上配置多个模型
3. **更多向量库** - 支持 Milvus, Weaviate, PGVector 等
4. **工作流版本管理** - 完整的工作流版本控制
5. **性能优化** - 批处理的并行优化，缓存机制

---

## 技术栈

- **Spring Boot 3.2**
- **LangChain4j 1.17.1** - AI 能力
- **LangGraph4j 1.8.20** - 工作流编排
- **MyBatis-Plus** - 数据库操作
- **MySQL** - 数据存储
- **Chroma** - 向量存储（可选）
