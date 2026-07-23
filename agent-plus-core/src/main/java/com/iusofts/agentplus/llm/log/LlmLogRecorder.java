package com.iusofts.agentplus.llm.log;

import com.iusofts.agentplus.llm.dto.ChatMessage;
import com.iusofts.agentplus.llm.dto.LlmModelConfigDTO;
import com.iusofts.agentplus.llm.dto.LlmModelDTO;
import com.iusofts.agentplus.llm.dto.ToolCall;
import com.iusofts.agentplus.llm.dto.ToolDefinition;
import com.iusofts.agentplus.knowledge.dto.EmbeddingModelDTO;
import com.iusofts.agentplus.knowledge.dto.KnowledgeRetrieveResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * LLM 日志统一记录门面。
 * <p>
 * 调用者通过这个门面记录日志，不关心底层实现（数据库/MQ/...）。
 * <p>
 * 使用方式：
 * <pre>
 * // 1. 记录 LLM 调用
 * llmLogRecorder.recordLlmCall()
 *     .traceId(traceId)
 *     .fromAgent(agentId)
 *     .model(modelDTO)
 *     .inputMessages(messages)
 *     .execute(() -> aiChatService.chat(...))
 *     .operator(userId, orgId)
 *     .record();
 *
 * // 2. 记录知识库检索
 * llmLogRecorder.recordKnowledgeRetrieval()
 *     .traceId(traceId)
 *     .fromAgent(agentId)
 *     .knowledgeBase(kbId, kbName)
 *     .query(query)
 *     .execute(() -> knowledgeRetriever.retrieve(...))
 *     .operator(userId, orgId)
 *     .record();
 * </pre>
 *
 * @author Ivan
 */
public interface LlmLogRecorder {

    /**
     * 生成链路追踪 ID。
     */
    static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 开始记录 LLM 调用日志。
     */
    LlmCallRecorder recordLlmCall();

    /**
     * 开始记录知识库检索日志。
     */
    KnowledgeRetrievalRecorder recordKnowledgeRetrieval();

    /**
     * 开始记录文档处理日志。
     */
    KnowledgeDocRecorder recordKnowledgeDoc();

    /**
     * LLM 调用日志记录器。
     */
    interface LlmCallRecorder {

        LlmCallRecorder traceId(String traceId);

        /**
         * 覆盖调用开始时间。默认取 {@code recordLlmCall()} 被调用的时刻，
         * 但当 LLM 先调用、日志后补记时，应显式传入调用真正开始的时刻以正确统计耗时。
         */
        LlmCallRecorder startTime(LocalDateTime startTime);

        LlmCallRecorder fromAgent(Long agentId);

        LlmCallRecorder fromChat(Long conversationId);

        LlmCallRecorder fromFlow(Long flowId, String nodeId);

        LlmCallRecorder fromApi();

        /**
         * 自定义来源三元组。用于内置枚举无法覆盖的来源，
         * 如 embedding 向量化：{@code EMBED_RETRIEVE}（检索）/ {@code EMBED_INDEX}（索引）。
         *
         * @param callSource   调用来源
         * @param sourceId     来源 ID（如知识库 ID）
         * @param sourceNodeId 来源节点 ID，可空
         */
        LlmCallRecorder source(String callSource, Long sourceId, String sourceNodeId);

        LlmCallRecorder model(LlmModelDTO modelDTO);

        /**
         * 使用嵌入模型配置填充模型信息。
         */
        LlmCallRecorder embeddingModel(EmbeddingModelDTO modelDTO);

        LlmCallRecorder config(LlmModelConfigDTO config);

        LlmCallRecorder inputMessages(List<ChatMessage> messages);

        /**
         * 手动设置输入字符数（不走 inputMessages 时使用）。
         */
        LlmCallRecorder inputCharCount(Integer charCount);

        /**
         * 记录下发给模型的工具规格列表。
         */
        LlmCallRecorder toolDefinitions(List<ToolDefinition> toolDefinitions);

        LlmCallRecorder operator(Long userId, Integer orgId);

        /**
         * 执行调用并自动记录成功/失败状态。
         */
        <T> LlmCallRecorder execute(Supplier<T> action);

        /**
         * 手动设置输出和 Token 信息。
         */
        LlmCallRecorder output(String content, Integer inputTokens, Integer outputTokens);

        /**
         * 记录模型返回的工具调用列表与结束原因。
         */
        LlmCallRecorder toolCalls(List<ToolCall> toolCalls, String finishReason);

        /**
         * 标记为成功。
         */
        LlmCallRecorder success();

        /**
         * 标记为失败。
         */
        LlmCallRecorder error(String errorCode, String errorMessage);

        /**
         * 提交记录。
         */
        void record();
    }

    /**
     * 知识库检索日志记录器。
     */
    interface KnowledgeRetrievalRecorder {

        KnowledgeRetrievalRecorder traceId(String traceId);

        /**
         * 覆盖检索开始时间。默认取 {@code recordKnowledgeRetrieval()} 被调用的时刻，
         * 但当检索先执行、日志后补记时，应显式传入检索真正开始的时刻以正确统计耗时。
         */
        KnowledgeRetrievalRecorder startTime(LocalDateTime startTime);

        KnowledgeRetrievalRecorder fromAgent(Long agentId);

        KnowledgeRetrievalRecorder fromChat(Long conversationId);

        KnowledgeRetrievalRecorder fromFlow(Long flowId, String nodeId);

        KnowledgeRetrievalRecorder fromApi();

        KnowledgeRetrievalRecorder knowledgeBase(Long kbId, String kbName);

        KnowledgeRetrievalRecorder query(String query);

        KnowledgeRetrievalRecorder topK(Integer topK);

        KnowledgeRetrievalRecorder operator(Long userId, Integer orgId);

        /**
         * 执行检索并自动记录成功/失败状态。
         */
        <T> KnowledgeRetrievalRecorder execute(Supplier<List<String>> action);

        /**
         * 手动设置召回结果。
         */
        KnowledgeRetrievalRecorder retrievedChunks(List<String> chunks, Integer embeddingTokens);

        /**
         * 直接使用检索结果记录召回详情（chunkId/相似度/向量化token）。
         */
        KnowledgeRetrievalRecorder retrievedResult(KnowledgeRetrieveResult result);

        /**
         * 标记为成功。
         */
        KnowledgeRetrievalRecorder success();

        /**
         * 标记为失败。
         */
        KnowledgeRetrievalRecorder error(String errorMessage);

        /**
         * 提交记录。
         */
        void record();
    }

    /**
     * 文档处理日志记录器。
     */
    interface KnowledgeDocRecorder {

        KnowledgeDocRecorder knowledgeBase(Long kbId, String kbName);

        KnowledgeDocRecorder document(Long docId, String docName);

        KnowledgeDocRecorder add();

        KnowledgeDocRecorder update();

        KnowledgeDocRecorder delete();

        KnowledgeDocRecorder operator(Long userId, Integer orgId);

        /**
         * 执行处理并自动记录成功/失败状态。
         */
        <T> KnowledgeDocRecorder execute(Supplier<T> action);

        /**
         * 手动设置处理结果。
         */
        KnowledgeDocRecorder chunks(Integer chunkCount, Integer totalChars, Integer embeddingTokens);

        /**
         * 标记为成功。
         */
        KnowledgeDocRecorder success();

        /**
         * 标记为失败。
         */
        KnowledgeDocRecorder error(String errorMessage);

        /**
         * 提交记录。
         */
        void record();
    }
}
