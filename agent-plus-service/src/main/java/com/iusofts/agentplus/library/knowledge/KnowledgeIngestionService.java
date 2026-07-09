package com.iusofts.agentplus.library.knowledge;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iusofts.agentplus.library.entity.AiKnowledgeBase;
import com.iusofts.agentplus.library.entity.AiKnowledgeChunk;
import com.iusofts.agentplus.library.entity.AiKnowledgeDocument;
import com.iusofts.agentplus.library.mapper.AiKnowledgeBaseMapper;
import com.iusofts.agentplus.library.mapper.AiKnowledgeChunkMapper;
import com.iusofts.agentplus.library.mapper.AiKnowledgeDocumentMapper;
import com.iusofts.agentplus.basic.redis.RedisLock;
import com.iusofts.agentplus.id.service.IdService;
import com.iusofts.agentplus.id.service.IdService.UidTypeEnum;
import com.iusofts.agentplus.llm.log.LlmLogRecorder;
import com.iusofts.agentplus.plugin.document.DocumentContentExtractor;
import com.iusofts.agentplus.plugin.document.TextChunker;
import com.iusofts.agentplus.plugin.vectorstore.KnowledgeStoreService;
import com.iusofts.agentplus.plugin.vectorstore.RedisVectorStoreManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 知识库文档分块存储管线核心。
 *
 * @author Ivan
 */
@Slf4j
@Service
public class KnowledgeIngestionService {

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_PROCESSING = 1;
    public static final int STATUS_COMPLETED = 2;
    public static final int STATUS_FAILED = 3;
    public static final int STATUS_DISABLED = 4;
    public static final int STATUS_ARCHIVED = 5;
    public static final int CHUNK_STATUS_DISABLED = 0;
    public static final int CHUNK_STATUS_ENABLED = 1;
    private static final String LOCK_PREFIX = "knowledge:ingest:doc:";
    private static final long LOCK_EXPIRE_SECONDS = 900;
    private static final int ERROR_MSG_MAX = 1000;

    @Resource
    private AiKnowledgeDocumentMapper documentMapper;
    @Resource
    private AiKnowledgeBaseMapper knowledgeBaseMapper;
    @Resource
    private AiKnowledgeChunkMapper chunkMapper;
    @Resource
    private IdService idService;
    @Resource
    private RedisLock redisLock;
    @Resource
    private RedisVectorStoreManager vectorStoreManager;
    @Resource
    private DocumentContentExtractor contentExtractor;
    @Resource
    private TextChunker textChunker;
    @Resource
    private KnowledgeStoreService knowledgeStoreService;
    @Resource
    private LlmLogRecorder llmLogRecorder;

    public void process(Long documentId) {
        String lockKey = LOCK_PREFIX + documentId;
        if (!redisLock.tryLock(lockKey, LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS)) {
            log.info("文档正在被其他任务处理，跳过：documentId={}", documentId);
            return;
        }
        try {
            doProcess(documentId);
        } finally {
            redisLock.releaseLock(lockKey);
        }
    }

    private void doProcess(Long documentId) {
        AiKnowledgeDocument doc = documentMapper.selectById(documentId);
        if (doc == null) {
            log.warn("文档不存在，跳过处理：documentId={}", documentId);
            return;
        }
        if (doc.getStatus() != null && doc.getStatus() == STATUS_COMPLETED) {
            log.info("文档已完成，跳过：documentId={}", documentId);
            return;
        }
        AiKnowledgeBase kb = knowledgeBaseMapper.selectById(doc.getKnowledgeBaseId());
        if (kb == null) {
            markFailed(doc, "知识库不存在：id=" + doc.getKnowledgeBaseId());
            return;
        }

        updateStatus(documentId, STATUS_PROCESSING, null);

        try {
            String text = contentExtractor.extract(doc.getDocUrl());
            int totalCharCount = text != null ? text.length() : 0;
            if (text.isEmpty()) {
                clearExistingChunks(kb, doc);
                finishDocument(documentId, 0);
                llmLogRecorder.recordKnowledgeDoc()
                    .knowledgeBase(kb.getId(), kb.getName())
                    .document(doc.getId(), doc.getName())
                    .add()
                    .operator(doc.getCreateBy(), doc.getOrgId())
                    .chunks(0, 0, 0)
                    .success()
                    .record();
                log.info("文档解析内容为空，标记完成（0分块）：documentId={}", documentId);
                return;
            }

            List<String> chunkTexts = textChunker.split(text, kb.getChunkSize(), kb.getChunkOverlap());
            if (chunkTexts.isEmpty()) {
                clearExistingChunks(kb, doc);
                finishDocument(documentId, 0);
                llmLogRecorder.recordKnowledgeDoc()
                    .knowledgeBase(kb.getId(), kb.getName())
                    .document(doc.getId(), doc.getName())
                    .add()
                    .operator(doc.getCreateBy(), doc.getOrgId())
                    .chunks(0, totalCharCount, 0)
                    .success()
                    .record();
                return;
            }

            clearExistingChunks(kb, doc);

            List<AiKnowledgeChunk> chunkRows = buildChunkRows(kb, doc, chunkTexts);
            List<String> chunkTextsForStore = chunkRows.stream().map(AiKnowledgeChunk::getContent).toList();
            List<String> vectorIds = chunkRows.stream().map(AiKnowledgeChunk::getVectorId).toList();

            knowledgeStoreService.batchEmbedAndStore(kb.getCollectionName(), vectorIds, chunkTextsForStore, kb.getEmbeddingModelId());

            saveChunkRows(chunkRows);

            finishDocument(documentId, chunkTexts.size());

            llmLogRecorder.recordKnowledgeDoc()
                .knowledgeBase(kb.getId(), kb.getName())
                .document(doc.getId(), doc.getName())
                .add()
                .operator(doc.getCreateBy(), doc.getOrgId())
                .chunks(chunkTexts.size(), totalCharCount, 0)
                .success()
                .record();

            log.info("文档处理完成：documentId={}，chunkCount={}", documentId, chunkTexts.size());
        } catch (Exception e) {
            log.error("文档处理失败：documentId={}", documentId, e);
            markFailed(doc, e.getMessage());

            try {
                llmLogRecorder.recordKnowledgeDoc()
                    .knowledgeBase(kb.getId(), kb.getName())
                    .document(doc.getId(), doc.getName())
                    .add()
                    .operator(doc.getCreateBy(), doc.getOrgId())
                    .error(e.getMessage())
                    .record();
            } catch (Exception logEx) {
                log.warn("记录文档处理日志失败", logEx);
            }
        }
    }

    private List<AiKnowledgeChunk> buildChunkRows(AiKnowledgeBase kb, AiKnowledgeDocument doc, List<String> chunkTexts) {
        List<AiKnowledgeChunk> result = new ArrayList<>();
        int sortOrder = 0;
        for (String content : chunkTexts) {
            String vectorId = idService.generateUid(UidTypeEnum.CHAT) + "-" + sortOrder;
            AiKnowledgeChunk chunk = new AiKnowledgeChunk();
            chunk.setId(idService.generateUid(UidTypeEnum.CHAT).longValue());
            chunk.setKnowledgeBaseId(kb.getId());
            chunk.setDocumentId(doc.getId());
            chunk.setVectorId(vectorId);
            chunk.setContent(content);
            chunk.setSortOrder(sortOrder);
            chunk.setStatus(CHUNK_STATUS_ENABLED);
            chunk.setCreateBy(doc.getCreateBy());
            chunk.setOrgId(doc.getOrgId());
            result.add(chunk);
            sortOrder++;
        }
        return result;
    }

    private void saveChunkRows(List<AiKnowledgeChunk> chunkRows) {
        for (AiKnowledgeChunk chunk : chunkRows) {
            chunkMapper.insert(chunk);
        }
    }

    public void clearExistingChunks(AiKnowledgeBase kb, AiKnowledgeDocument doc) {
        LambdaQueryWrapper<AiKnowledgeChunk> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AiKnowledgeChunk::getDocumentId, doc.getId());
        List<AiKnowledgeChunk> existing = chunkMapper.selectList(wrapper);
        if (existing.isEmpty()) {
            return;
        }
        List<String> vectorIds = existing.stream().map(AiKnowledgeChunk::getVectorId).filter(v -> v != null && !v.isEmpty()).toList();
        try {
            vectorStoreManager.removeAll(kb.getCollectionName(), vectorIds);
        } catch (Exception e) {
            log.warn("清理旧向量失败：documentId={}，collection={}", doc.getId(), kb.getCollectionName(), e);
        }
        chunkMapper.delete(wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public void finishDocument(Long documentId, int chunkCount) {
        AiKnowledgeDocument update = new AiKnowledgeDocument();
        update.setId(documentId);
        update.setStatus(STATUS_COMPLETED);
        update.setChunkCount(chunkCount);
        update.setErrorMessage("");
        documentMapper.updateById(update);
    }

    private void updateStatus(Long documentId, int status, String errorMessage) {
        AiKnowledgeDocument update = new AiKnowledgeDocument();
        update.setId(documentId);
        update.setStatus(status);
        if (errorMessage != null) {
            update.setErrorMessage(truncate(errorMessage));
        }
        documentMapper.updateById(update);
    }

    private void markFailed(AiKnowledgeDocument doc, String reason) {
        updateStatus(doc.getId(), STATUS_FAILED, reason);
    }

    private String truncate(String s) {
        if (s == null) {
            return null;
        }
        return s.length() > ERROR_MSG_MAX ? s.substring(0, ERROR_MSG_MAX) : s;
    }
}
