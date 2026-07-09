# LLM 调用日志系统使用指南

## 一、表结构说明

| 表名 | 说明 |
|-----|------|
| `ai_llm_call_log` | 大模型调用日志（对话、工作流等） |
| `ai_knowledge_retrieval_log` | 知识库检索日志（query 向量化 + 召回） |
| `ai_knowledge_doc_log` | 文档处理日志（文档入库 embedding） |

## 二、使用方式

### 2.1 记录 LLM 调用

```java
// 1. 注入 Service 和 import 工具类
import com.iusofts.agentplus.chat.util.LlmLogRecorder;
import com.iusofts.agentplus.chat.service.AiLlmCallLogService;
import com.iusofts.agentplus.chat.service.AiKnowledgeRetrievalLogService;
import com.iusofts.agentplus.chat.service.AiKnowledgeDocLogService;
@Resource
private AiLlmCallLogService llmCallLogService;
@Resource
private AiKnowledgeRetrievalLogService retrievalLogService;
@Resource
private AiKnowledgeDocLogService docLogService;

// 2. 使用 Builder 记录
String traceId = LlmLogRecorder.generateTraceId();

AiLlmCallLog log = LlmLogRecorder.llmCall()
    .traceId(traceId)
    .fromAgent(agentId)              // 或者 .fromChat(), .fromFlow(), .fromApi()
    .model(modelDTO)
    .config(config)
    .inputMessages(messages)
    .tokens(inputTokens, outputTokens)
    .output(responseContent)
    .success()                         // 或者 .error(errorCode, errorMsg)
    .operator(userId, orgId)
    .build();

llmCallLogService.saveLog(log);
```

### 2.2 记录知识库检索

```java
@Resource
private IAiKnowledgeRetrievalLogService retrievalLogService;

AiKnowledgeRetrievalLog log = LlmLogRecorder.knowledgeRetrieval()
    .traceId(traceId)                  // 可以和 LLM 调用共享同一个 traceId
    .fromAgent(agentId)
    .knowledgeBase(kbId, kbName)
    .query(userQuery)
    .embeddingTokens(embeddingTokens)
    .topK(topK)
    .retrievedChunks(chunkIds, contents, similarities)
    .success()
    .operator(userId, orgId)
    .build();

retrievalLogService.saveLog(log);
```

### 2.3 记录文档处理

```java
@Resource
private IAiKnowledgeDocLogService docLogService;

AiKnowledgeDocLog log = LlmLogRecorder.knowledgeDoc()
    .knowledgeBase(kbId, kbName)
    .document(docId, docName)
    .add()                              // 或者 .update(), .delete()
    .chunks(chunkCount, totalChars, embeddingTokens)
    .success()
    .operator(userId, orgId)
    .build();

docLogService.saveLog(log);
```

## 三、集成示例

### 3.1 在 AiServiceImpl 中集成

```java
// 在 call() 或 chat() 方法中
String traceId = LlmLogRecorder.generateTraceId();

// 先记录知识库检索（如果有）
if (hasKnowledgeBase) {
    AiKnowledgeRetrievalLog retrievalLog = LlmLogRecorder.knowledgeRetrieval()
        .traceId(traceId)
        .fromAgent(agentId)
        // ... 其他设置
        .build();
    retrievalLogService.save(retrievalLog);
}

// 再记录 LLM 调用
AiLlmCallLog llmLog = LlmLogRecorder.llmCall()
    .traceId(traceId)
    .fromAgent(agentId)
    // ... 其他设置
    .build();
llmCallLogService.save(llmLog);
```

### 3.2 在工作流 LLM 节点中集成

```java
// 在 LLMNodeExecutor 中
AiLlmCallLog log = LlmLogRecorder.llmCall()
    .fromFlow(flowId, nodeId)
    // ... 其他设置
    .build();
llmCallLogService.saveLog(log);
```

## 四、监控查询示例

```sql
-- 按模型统计调用量和 token 消耗
SELECT model_name, COUNT(*), SUM(input_tokens), SUM(output_tokens), SUM(total_tokens)
FROM ai_llm_call_log
WHERE time_sign >= '2026-07-01'
GROUP BY model_name;

-- 按知识库统计检索次数
SELECT knowledge_base_name, COUNT(*)
FROM ai_knowledge_retrieval_log
WHERE time_sign >= '2026-07-01'
GROUP BY knowledge_base_name;

-- 链路追踪：用同一个 traceId 查询完整调用链
SELECT * FROM ai_llm_call_log WHERE trace_id = 'xxx'
UNION ALL
SELECT * FROM ai_knowledge_retrieval_log WHERE trace_id = 'xxx';
```
