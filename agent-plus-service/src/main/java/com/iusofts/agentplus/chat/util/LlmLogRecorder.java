package com.iusofts.agentplus.chat.util;

import com.iusofts.agentplus.chat.entity.AiKnowledgeDocLog;
import com.iusofts.agentplus.chat.entity.AiKnowledgeRetrievalLog;
import com.iusofts.agentplus.chat.entity.AiLlmCallLog;
import com.iusofts.agentplus.llm.dto.ChatMessage;
import com.iusofts.agentplus.llm.dto.LlmModelConfigDTO;
import com.iusofts.agentplus.llm.dto.LlmModelDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * AI调用日志记录工具
 * 使用 Builder 模式链式调用
 *
 * @author Ivan
 * @since 2026-07-09
 */
public class LlmLogRecorder {

    /**
     * 生成 traceId
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * LLM 调用日志 Builder
     */
    public static LlmCallLogBuilder llmCall() {
        return new LlmCallLogBuilder();
    }

    /**
     * 知识库检索日志 Builder
     */
    public static KnowledgeRetrievalLogBuilder knowledgeRetrieval() {
        return new KnowledgeRetrievalLogBuilder();
    }

    /**
     * 文档处理日志 Builder
     */
    public static KnowledgeDocLogBuilder knowledgeDoc() {
        return new KnowledgeDocLogBuilder();
    }

    public static class LlmCallLogBuilder {
        private final AiLlmCallLog log = new AiLlmCallLog();
        private final LocalDateTime startTime = LocalDateTime.now();

        public LlmCallLogBuilder() {
            log.setTraceId(generateTraceId());
            log.setStartTime(startTime);
            log.setTimeSign(LocalDate.now());
            log.setCallStatus(1);
        }

        public LlmCallLogBuilder traceId(String traceId) {
            log.setTraceId(traceId);
            return this;
        }

        public LlmCallLogBuilder fromAgent(Long agentId) {
            log.setCallSource("AGENT");
            log.setSourceId(agentId);
            return this;
        }

        public LlmCallLogBuilder fromChat(Long conversationId) {
            log.setCallSource("CHAT");
            log.setSourceId(conversationId);
            return this;
        }

        public LlmCallLogBuilder fromFlow(Long flowId, String nodeId) {
            log.setCallSource("FLOW");
            log.setSourceId(flowId);
            log.setSourceNodeId(nodeId);
            return this;
        }

        public LlmCallLogBuilder fromApi() {
            log.setCallSource("API");
            return this;
        }

        public LlmCallLogBuilder business(Integer businessType, Long businessId) {
            log.setBusinessType(businessType);
            log.setBusinessId(businessId);
            return this;
        }

        public LlmCallLogBuilder model(LlmModelDTO modelDTO) {
            if (modelDTO != null) {
                log.setModelId(modelDTO.getId());
                log.setModelName(modelDTO.getModelName());
                log.setModelProvider(modelDTO.getProvider());
            }
            return this;
        }

        public LlmCallLogBuilder config(LlmModelConfigDTO config) {
            if (config != null) {
                log.setTemperature(config.getTemperature() != null ? BigDecimal.valueOf(config.getTemperature()) : null);
                log.setMaxTokens(config.getMaxTokens());
            }
            return this;
        }

        public LlmCallLogBuilder inputMessages(List<ChatMessage> messages) {
            if (messages != null) {
                List<AiLlmCallLog.MessageEntry> entries = new ArrayList<>();
                int totalChars = 0;
                for (ChatMessage msg : messages) {
                    AiLlmCallLog.MessageEntry entry = new AiLlmCallLog.MessageEntry();
                    entry.setRole(msg.getRole());
                    entry.setContent(msg.getContent());
                    entries.add(entry);
                    if (msg.getContent() != null) {
                        totalChars += msg.getContent().length();
                    }
                }
                log.setInputMessages(entries);
                log.setInputCharCount(totalChars);
            }
            return this;
        }

        public LlmCallLogBuilder tokens(Integer inputTokens, Integer outputTokens) {
            log.setInputTokens(inputTokens);
            log.setOutputTokens(outputTokens);
            if (inputTokens != null || outputTokens != null) {
                log.setTotalTokens((inputTokens != null ? inputTokens : 0) + (outputTokens != null ? outputTokens : 0));
            }
            return this;
        }

        public LlmCallLogBuilder output(String content) {
            log.setOutputContent(content);
            log.setOutputCharCount(content != null ? content.length() : 0);
            return this;
        }

        public LlmCallLogBuilder success() {
            log.setCallStatus(1);
            return this;
        }

        public LlmCallLogBuilder error(String errorCode, String errorMessage) {
            log.setCallStatus(0);
            log.setErrorCode(errorCode);
            log.setErrorMessage(errorMessage);
            return this;
        }

        public LlmCallLogBuilder operator(Long userId, Integer orgId) {
            log.setCreateBy(userId);
            log.setOrgId(orgId);
            return this;
        }

        public AiLlmCallLog build() {
            LocalDateTime endTime = LocalDateTime.now();
            log.setEndTime(endTime);
            log.setDuration((int) (java.time.Duration.between(startTime, endTime).toMillis()));
            log.setCreateTime(LocalDateTime.now());
            return log;
        }
    }

    public static class KnowledgeRetrievalLogBuilder {
        private final AiKnowledgeRetrievalLog log = new AiKnowledgeRetrievalLog();
        private final LocalDateTime startTime = LocalDateTime.now();

        public KnowledgeRetrievalLogBuilder() {
            log.setTraceId(generateTraceId());
            log.setStartTime(startTime);
            log.setTimeSign(LocalDate.now());
            log.setCallStatus(1);
        }

        public KnowledgeRetrievalLogBuilder traceId(String traceId) {
            log.setTraceId(traceId);
            return this;
        }

        public KnowledgeRetrievalLogBuilder fromAgent(Long agentId) {
            log.setCallSource("AGENT");
            log.setSourceId(agentId);
            return this;
        }

        public KnowledgeRetrievalLogBuilder fromChat(Long conversationId) {
            log.setCallSource("CHAT");
            log.setSourceId(conversationId);
            return this;
        }

        public KnowledgeRetrievalLogBuilder fromFlow(Long flowId) {
            log.setCallSource("FLOW");
            log.setSourceId(flowId);
            return this;
        }

        public KnowledgeRetrievalLogBuilder knowledgeBase(Long kbId, String kbName) {
            log.setKnowledgeBaseId(kbId);
            log.setKnowledgeBaseName(kbName);
            return this;
        }

        public KnowledgeRetrievalLogBuilder query(String query) {
            log.setQuery(query);
            log.setQueryCharCount(query != null ? query.length() : 0);
            return this;
        }

        public KnowledgeRetrievalLogBuilder embeddingTokens(Integer tokens) {
            log.setQueryEmbeddingTokens(tokens);
            return this;
        }

        public KnowledgeRetrievalLogBuilder topK(Integer topK) {
            log.setTopK(topK);
            return this;
        }

        public KnowledgeRetrievalLogBuilder retrievedChunks(List<Long> chunkIds, List<String> contents, List<Double> similarities) {
            if (chunkIds != null) {
                List<AiKnowledgeRetrievalLog.ChunkEntry> entries = new ArrayList<>();
                for (int i = 0; i < chunkIds.size(); i++) {
                    AiKnowledgeRetrievalLog.ChunkEntry entry = new AiKnowledgeRetrievalLog.ChunkEntry();
                    entry.setChunkId(chunkIds.get(i));
                    if (contents != null && i < contents.size()) {
                        String content = contents.get(i);
                        entry.setContent(content != null && content.length() > 200 ? content.substring(0, 200) + "..." : content);
                    }
                    if (similarities != null && i < similarities.size()) {
                        entry.setSimilarity(similarities.get(i));
                    }
                    entries.add(entry);
                }
                log.setRetrievedChunks(entries);
                log.setRetrievedCount(chunkIds.size());
            }
            return this;
        }

        public KnowledgeRetrievalLogBuilder success() {
            log.setCallStatus(1);
            return this;
        }

        public KnowledgeRetrievalLogBuilder error(String errorMessage) {
            log.setCallStatus(0);
            log.setErrorMessage(errorMessage);
            return this;
        }

        public KnowledgeRetrievalLogBuilder operator(Long userId, Integer orgId) {
            log.setCreateBy(userId);
            log.setOrgId(orgId);
            return this;
        }

        public AiKnowledgeRetrievalLog build() {
            LocalDateTime endTime = LocalDateTime.now();
            log.setEndTime(endTime);
            log.setDuration((int) (java.time.Duration.between(startTime, endTime).toMillis()));
            log.setCreateTime(LocalDateTime.now());
            return log;
        }
    }

    public static class KnowledgeDocLogBuilder {
        private final AiKnowledgeDocLog log = new AiKnowledgeDocLog();
        private final LocalDateTime startTime = LocalDateTime.now();

        public KnowledgeDocLogBuilder() {
            log.setStartTime(startTime);
            log.setTimeSign(LocalDate.now());
            log.setCallStatus(1);
        }

        public KnowledgeDocLogBuilder knowledgeBase(Long kbId, String kbName) {
            log.setKnowledgeBaseId(kbId);
            log.setKnowledgeBaseName(kbName);
            return this;
        }

        public KnowledgeDocLogBuilder document(Long docId, String docName) {
            log.setDocId(docId);
            log.setDocName(docName);
            return this;
        }

        public KnowledgeDocLogBuilder add() {
            log.setOperationType("ADD");
            return this;
        }

        public KnowledgeDocLogBuilder update() {
            log.setOperationType("UPDATE");
            return this;
        }

        public KnowledgeDocLogBuilder delete() {
            log.setOperationType("DELETE");
            return this;
        }

        public KnowledgeDocLogBuilder chunks(int chunkCount, int totalChars, int embeddingTokens) {
            log.setChunkCount(chunkCount);
            log.setTotalCharCount(totalChars);
            log.setTotalEmbeddingTokens(embeddingTokens);
            return this;
        }

        public KnowledgeDocLogBuilder success() {
            log.setCallStatus(1);
            return this;
        }

        public KnowledgeDocLogBuilder error(String errorMessage) {
            log.setCallStatus(0);
            log.setErrorMessage(errorMessage);
            return this;
        }

        public KnowledgeDocLogBuilder operator(Long userId, Integer orgId) {
            log.setCreateBy(userId);
            log.setOrgId(orgId);
            return this;
        }

        public AiKnowledgeDocLog build() {
            LocalDateTime endTime = LocalDateTime.now();
            log.setEndTime(endTime);
            log.setDuration((int) (java.time.Duration.between(startTime, endTime).toMillis()));
            log.setCreateTime(LocalDateTime.now());
            return log;
        }
    }
}
