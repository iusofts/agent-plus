package com.iusofts.agentplus.ailog.service;

import com.iusofts.agentplus.ailog.entity.AiKnowledgeDocLog;
import com.iusofts.agentplus.ailog.entity.AiKnowledgeRetrievalLog;
import com.iusofts.agentplus.ailog.entity.AiLlmCallLog;
import com.iusofts.agentplus.ailog.entity.AiKnowledgeRetrievalLog.ChunkEntry;
import com.iusofts.agentplus.ailog.entity.AiLlmCallLog.MessageEntry;
import com.iusofts.agentplus.llm.dto.AiChatMessage;
import com.iusofts.agentplus.llm.dto.LlmModelConfigDTO;
import com.iusofts.agentplus.llm.dto.LlmModelDTO;
import com.iusofts.agentplus.llm.dto.ToolCall;
import com.iusofts.agentplus.llm.dto.ToolDefinition;
import com.iusofts.agentplus.knowledge.dto.EmbeddingModelDTO;
import com.iusofts.agentplus.knowledge.dto.KnowledgeChunk;
import com.iusofts.agentplus.knowledge.dto.KnowledgeRetrieveResult;
import com.iusofts.agentplus.llm.log.LlmLogRecorder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 默认 LLM 日志记录器实现（存储到数据库）。
 *
 * @author Ivan
 */
@Slf4j
@Service
public class DefaultLlmLogRecorder implements LlmLogRecorder {

    @Resource
    private AiLlmCallLogService llmCallLogService;

    @Resource
    private AiKnowledgeRetrievalLogService knowledgeRetrievalLogService;

    @Resource
    private AiKnowledgeDocLogService knowledgeDocLogService;

    @Override
    public LlmCallRecorder recordLlmCall() {
        return new DefaultLlmCallRecorder(llmCallLogService);
    }

    @Override
    public KnowledgeRetrievalRecorder recordKnowledgeRetrieval() {
        return new DefaultKnowledgeRetrievalRecorder(knowledgeRetrievalLogService);
    }

    @Override
    public KnowledgeDocRecorder recordKnowledgeDoc() {
        return new DefaultKnowledgeDocRecorder(knowledgeDocLogService);
    }

    /**
     * LLM 调用日志记录器实现。
     */
    private static class DefaultLlmCallRecorder implements LlmCallRecorder {

        private final AiLlmCallLogService service;
        private final AiLlmCallLog entity;
        private LocalDateTime startTime;

        public DefaultLlmCallRecorder(AiLlmCallLogService service) {
            this.service = service;
            this.entity = new AiLlmCallLog();
            this.startTime = LocalDateTime.now();
            this.entity.setStartTime(startTime);
            this.entity.setTraceId(LlmLogRecorder.generateTraceId());
            this.entity.setCallStatus(1);
        }

        @Override
        public LlmCallRecorder traceId(String traceId) {
            entity.setTraceId(traceId);
            return this;
        }

        @Override
        public LlmCallRecorder startTime(LocalDateTime startTime) {
            if (startTime != null) {
                this.startTime = startTime;
                entity.setStartTime(startTime);
            }
            return this;
        }

        @Override
        public LlmCallRecorder fromAgent(Long agentId) {
            entity.setCallSource("AGENT");
            entity.setSourceId(agentId);
            return this;
        }

        @Override
        public LlmCallRecorder fromChat(Long conversationId) {
            entity.setCallSource("CHAT");
            entity.setSourceId(conversationId);
            return this;
        }

        @Override
        public LlmCallRecorder fromFlow(Long flowId, String nodeId) {
            entity.setCallSource("FLOW");
            entity.setSourceId(flowId);
            entity.setSourceNodeId(nodeId);
            return this;
        }

        @Override
        public LlmCallRecorder fromApi() {
            entity.setCallSource("API");
            return this;
        }

        @Override
        public LlmCallRecorder source(String callSource, Long sourceId, String sourceNodeId) {
            entity.setCallSource(callSource);
            entity.setSourceId(sourceId);
            entity.setSourceNodeId(sourceNodeId);
            return this;
        }

        @Override
        public LlmCallRecorder model(LlmModelDTO modelDTO) {
            if (modelDTO != null) {
                entity.setModelId(modelDTO.getId());
                entity.setModelName(modelDTO.getModelName());
                entity.setModelProvider(modelDTO.getProvider());
            }
            return this;
        }

        @Override
        public LlmCallRecorder embeddingModel(EmbeddingModelDTO modelDTO) {
            if (modelDTO != null) {
                entity.setModelId(modelDTO.getId());
                entity.setModelName(modelDTO.getModelName());
                entity.setModelProvider(modelDTO.getProvider());
            }
            return this;
        }

        @Override
        public LlmCallRecorder config(LlmModelConfigDTO config) {
            if (config != null) {
                entity.setTemperature(config.getTemperature() != null ? BigDecimal.valueOf(config.getTemperature()) : null);
                entity.setMaxTokens(config.getMaxTokens());
            }
            return this;
        }

        @Override
        public LlmCallRecorder inputMessages(List<AiChatMessage> messages) {
            if (messages != null) {
                List<MessageEntry> entries = new ArrayList<>();
                int totalChars = 0;
                for (AiChatMessage msg : messages) {
                    MessageEntry entry = new MessageEntry();
                    entry.setRole(msg.getRole());
                    entry.setContent(msg.getContent());
                    entries.add(entry);
                    if (msg.getContent() != null) {
                        totalChars += msg.getContent().length();
                    }
                }
                entity.setInputMessages(entries);
                entity.setInputCharCount(totalChars);
            }
            return this;
        }

        @Override
        public LlmCallRecorder inputContent(String content) {
            entity.setInputContent(content);
            if (content != null) {
                entity.setInputCharCount(content.length());
            }
            return this;
        }

        @Override
        public LlmCallRecorder operator(Long userId, Integer orgId) {
            entity.setCreateBy(userId);
            entity.setOrgId(orgId);
            return this;
        }

        @Override
        public LlmCallRecorder toolDefinitions(List<ToolDefinition> toolDefinitions) {
            if (toolDefinitions != null && !toolDefinitions.isEmpty()) {
                entity.setToolDefinitions(toolDefinitions);
            }
            return this;
        }

        @Override
        public LlmCallRecorder toolCalls(List<ToolCall> toolCalls, String finishReason) {
            if (toolCalls != null && !toolCalls.isEmpty()) {
                entity.setToolCalls(toolCalls);
            }
            entity.setFinishReason(finishReason);
            return this;
        }

        @Override
        public <T> LlmCallRecorder execute(Supplier<T> action) {
            try {
                T result = action.get();
                success();
            } catch (Exception e) {
                error(null, e.getMessage());
                throw e;
            }
            return this;
        }

        @Override
        public LlmCallRecorder output(String content, Integer inputTokens, Integer outputTokens) {
            entity.setOutputContent(content);
            entity.setOutputCharCount(content != null ? content.length() : 0);
            entity.setInputTokens(inputTokens);
            entity.setOutputTokens(outputTokens);
            if (inputTokens != null || outputTokens != null) {
                entity.setTotalTokens((inputTokens != null ? inputTokens : 0) + (outputTokens != null ? outputTokens : 0));
            }
            return this;
        }

        @Override
        public LlmCallRecorder success() {
            entity.setCallStatus(1);
            return this;
        }

        @Override
        public LlmCallRecorder error(String errorCode, String errorMessage) {
            entity.setCallStatus(0);
            entity.setErrorCode(errorCode);
            entity.setErrorMessage(errorMessage);
            return this;
        }

        @Override
        public void record() {
            try {
                LocalDateTime endTime = LocalDateTime.now();
                entity.setEndTime(endTime);
                entity.setDuration((int) java.time.Duration.between(startTime, endTime).toMillis());
                entity.setCreateTime(LocalDateTime.now());
                entity.setTimeSign(LocalDate.now());
                entity.setHourSign(startTime.getHour());
                service.saveLog(entity);
            } catch (Exception e) {
                log.warn("记录 LLM 调用日志失败", e);
            }
        }
    }

    /**
     * 知识库检索日志记录器实现。
     */
    private static class DefaultKnowledgeRetrievalRecorder implements KnowledgeRetrievalRecorder {

        private final AiKnowledgeRetrievalLogService service;
        private final AiKnowledgeRetrievalLog entity;
        private LocalDateTime startTime;

        public DefaultKnowledgeRetrievalRecorder(AiKnowledgeRetrievalLogService service) {
            this.service = service;
            this.entity = new AiKnowledgeRetrievalLog();
            this.startTime = LocalDateTime.now();
            this.entity.setStartTime(startTime);
            this.entity.setTraceId(LlmLogRecorder.generateTraceId());
            this.entity.setCallStatus(1);
        }

        @Override
        public KnowledgeRetrievalRecorder traceId(String traceId) {
            entity.setTraceId(traceId);
            return this;
        }

        @Override
        public KnowledgeRetrievalRecorder startTime(LocalDateTime startTime) {
            if (startTime != null) {
                this.startTime = startTime;
                entity.setStartTime(startTime);
            }
            return this;
        }

        @Override
        public KnowledgeRetrievalRecorder fromAgent(Long agentId) {
            entity.setCallSource("AGENT");
            entity.setSourceId(agentId);
            return this;
        }

        @Override
        public KnowledgeRetrievalRecorder fromChat(Long conversationId) {
            entity.setCallSource("CHAT");
            entity.setSourceId(conversationId);
            return this;
        }

        @Override
        public KnowledgeRetrievalRecorder fromFlow(Long flowId, String nodeId) {
            entity.setCallSource("FLOW");
            entity.setSourceId(flowId);
            entity.setSourceNodeId(nodeId);
            return this;
        }

        @Override
        public KnowledgeRetrievalRecorder fromApi() {
            entity.setCallSource("API");
            return this;
        }

        @Override
        public KnowledgeRetrievalRecorder source(String callSource, Long sourceId, String sourceNodeId) {
            entity.setCallSource(callSource);
            entity.setSourceId(sourceId);
            entity.setSourceNodeId(sourceNodeId);
            return this;
        }

        @Override
        public KnowledgeRetrievalRecorder knowledgeBase(Long kbId, String kbName) {
            entity.setKnowledgeBaseId(kbId);
            entity.setKnowledgeBaseName(kbName);
            return this;
        }

        @Override
        public KnowledgeRetrievalRecorder query(String query) {
            entity.setQuery(query);
            entity.setQueryCharCount(query != null ? query.length() : 0);
            return this;
        }

        @Override
        public KnowledgeRetrievalRecorder topK(Integer topK) {
            entity.setTopK(topK);
            return this;
        }

        @Override
        public KnowledgeRetrievalRecorder operator(Long userId, Integer orgId) {
            entity.setCreateBy(userId);
            entity.setOrgId(orgId);
            return this;
        }

        @Override
        public <T> KnowledgeRetrievalRecorder execute(Supplier<List<String>> action) {
            try {
                List<String> chunks = action.get();
                retrievedChunks(chunks, null);
                success();
            } catch (Exception e) {
                error(e.getMessage());
                throw e;
            }
            return this;
        }

        @Override
        public KnowledgeRetrievalRecorder retrievedChunks(List<String> chunks, Integer embeddingTokens) {
            if (chunks != null) {
                entity.setRetrievedCount(chunks.size());
                List<ChunkEntry> entries = new ArrayList<>();
                for (String chunk : chunks) {
                    ChunkEntry entry = new ChunkEntry();
                    entry.setContent(truncate(chunk));
                    entries.add(entry);
                }
                entity.setRetrievedChunks(entries);
            }
            entity.setQueryEmbeddingTokens(embeddingTokens);
            return this;
        }

        @Override
        public KnowledgeRetrievalRecorder retrievedResult(KnowledgeRetrieveResult result) {
            if (result == null) {
                return this;
            }
            List<KnowledgeChunk> chunks = result.getChunks();
            if (chunks != null) {
                entity.setRetrievedCount(chunks.size());
                List<ChunkEntry> entries = new ArrayList<>();
                for (KnowledgeChunk chunk : chunks) {
                    ChunkEntry entry = new ChunkEntry();
                    entry.setChunkId(chunk.getChunkId());
                    entry.setContent(truncate(chunk.getContent()));
                    entry.setSimilarity(chunk.getScore());
                    entries.add(entry);
                }
                entity.setRetrievedChunks(entries);
            }
            entity.setQueryEmbeddingTokens(result.getEmbeddingTokens());
            return this;
        }

        private String truncate(String content) {
            return content != null && content.length() > 200 ? content.substring(0, 200) + "..." : content;
        }

        @Override
        public KnowledgeRetrievalRecorder success() {
            entity.setCallStatus(1);
            return this;
        }

        @Override
        public KnowledgeRetrievalRecorder error(String errorMessage) {
            entity.setCallStatus(0);
            entity.setErrorMessage(errorMessage);
            return this;
        }

        @Override
        public void record() {
            try {
                LocalDateTime endTime = LocalDateTime.now();
                entity.setEndTime(endTime);
                entity.setDuration((int) java.time.Duration.between(startTime, endTime).toMillis());
                entity.setCreateTime(LocalDateTime.now());
                entity.setTimeSign(LocalDate.now());
                entity.setHourSign(startTime.getHour());
                service.saveLog(entity);
            } catch (Exception e) {
                log.warn("记录知识库检索日志失败", e);
            }
        }
    }

    /**
     * 文档处理日志记录器实现。
     */
    private static class DefaultKnowledgeDocRecorder implements KnowledgeDocRecorder {

        private final AiKnowledgeDocLogService service;
        private final AiKnowledgeDocLog entity;
        private final LocalDateTime startTime;

        public DefaultKnowledgeDocRecorder(AiKnowledgeDocLogService service) {
            this.service = service;
            this.entity = new AiKnowledgeDocLog();
            this.startTime = LocalDateTime.now();
            this.entity.setStartTime(startTime);
            this.entity.setCallStatus(1);
        }

        @Override
        public KnowledgeDocRecorder knowledgeBase(Long kbId, String kbName) {
            entity.setKnowledgeBaseId(kbId);
            entity.setKnowledgeBaseName(kbName);
            return this;
        }

        @Override
        public KnowledgeDocRecorder document(Long docId, String docName) {
            entity.setDocId(docId);
            entity.setDocName(docName);
            return this;
        }

        @Override
        public KnowledgeDocRecorder add() {
            entity.setOperationType("ADD");
            return this;
        }

        @Override
        public KnowledgeDocRecorder update() {
            entity.setOperationType("UPDATE");
            return this;
        }

        @Override
        public KnowledgeDocRecorder delete() {
            entity.setOperationType("DELETE");
            return this;
        }

        @Override
        public KnowledgeDocRecorder operator(Long userId, Integer orgId) {
            entity.setCreateBy(userId);
            entity.setOrgId(orgId);
            return this;
        }

        @Override
        public <T> KnowledgeDocRecorder execute(Supplier<T> action) {
            try {
                T result = action.get();
                success();
            } catch (Exception e) {
                error(e.getMessage());
                throw e;
            }
            return this;
        }

        @Override
        public KnowledgeDocRecorder chunks(Integer chunkCount, Integer totalChars, Integer embeddingTokens) {
            entity.setChunkCount(chunkCount);
            entity.setTotalCharCount(totalChars);
            entity.setTotalEmbeddingTokens(embeddingTokens);
            return this;
        }

        @Override
        public KnowledgeDocRecorder success() {
            entity.setCallStatus(1);
            return this;
        }

        @Override
        public KnowledgeDocRecorder error(String errorMessage) {
            entity.setCallStatus(0);
            entity.setErrorMessage(errorMessage);
            return this;
        }

        @Override
        public void record() {
            try {
                LocalDateTime endTime = LocalDateTime.now();
                entity.setEndTime(endTime);
                entity.setDuration((int) java.time.Duration.between(startTime, endTime).toMillis());
                entity.setCreateTime(LocalDateTime.now());
                entity.setTimeSign(LocalDate.now());
                entity.setHourSign(startTime.getHour());
                service.saveLog(entity);
            } catch (Exception e) {
                log.warn("记录文档处理日志失败", e);
            }
        }
    }
}
