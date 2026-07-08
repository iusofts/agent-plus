package com.iusofts.agentplus.ai.knowledge;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iusofts.agentplus.ai.entity.AiKnowledgeBase;
import com.iusofts.agentplus.ai.entity.AiKnowledgeChunk;
import com.iusofts.agentplus.ai.entity.AiKnowledgeDocument;
import com.iusofts.agentplus.ai.mapper.AiKnowledgeBaseMapper;
import com.iusofts.agentplus.ai.mapper.AiKnowledgeChunkMapper;
import com.iusofts.agentplus.ai.mapper.AiKnowledgeDocumentMapper;
import com.iusofts.agentplus.basic.redis.RedisLock;
import com.iusofts.agentplus.id.service.IdService;
import com.iusofts.agentplus.id.service.IdService.UidTypeEnum;
import com.iusofts.agentplus.plugin.document.DocumentContentExtractor;
import com.iusofts.agentplus.plugin.document.TextChunker;
import com.iusofts.agentplus.plugin.vectorstore.KnowledgeProperties;
import com.iusofts.agentplus.plugin.vectorstore.RedisVectorStoreManager;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 知识库文档分块存储管线核心。
 *
 * <p>单文档处理流程:下载 -> Tika 解析 -> 分块 -> 向量化 -> 写 Redis 向量库 -> 落库
 * {@code ai_knowledge_chunk} -> 更新文档 status/chunkCount。失败置 status=3 并记录原因。</p>
 *
 * <p>用 Redisson 分布式锁按文档 id 去重,避免异步任务与定时补偿任务重复处理同一文档。</p>
 *
 * @author Ivan
 */
@Service
public class KnowledgeIngestionService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeIngestionService.class);

    /** 文档状态。 */
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_PROCESSING = 1;
    public static final int STATUS_COMPLETED = 2;
    public static final int STATUS_FAILED = 3;

    /** 处理锁 key 前缀。 */
    private static final String LOCK_PREFIX = "knowledge:ingest:doc:";
    /** 锁持有时长(秒),覆盖单文档处理耗时。 */
    private static final long LOCK_EXPIRE_SECONDS = 900;
    /** 单批向量化的分块数,控制单次 embedding 请求规模。 */
    private static final int EMBED_BATCH_SIZE = 20;
    /** error_message 字段最大长度保护。 */
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
    private EmbeddingModelProvider embeddingModelProvider;

    @Resource
    private RedisVectorStoreManager vectorStoreManager;

    @Resource
    private DocumentContentExtractor contentExtractor;

    @Resource
    private TextChunker textChunker;

    /**
     * 处理单个文档(幂等、带分布式锁)。供异步线程池与定时补偿共同调用。
     *
     * @param documentId 文档 id
     */
    public void process(Long documentId) {
        String lockKey = LOCK_PREFIX + documentId;
        if (!redisLock.tryLock(lockKey, LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS)) {
            log.info("文档正在被其他任务处理,跳过: documentId={}", documentId);
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
            log.warn("文档不存在,跳过处理: documentId={}", documentId);
            return;
        }
        if (doc.getStatus() != null && doc.getStatus() == STATUS_COMPLETED) {
            log.info("文档已完成,跳过: documentId={}", documentId);
            return;
        }
        AiKnowledgeBase kb = knowledgeBaseMapper.selectById(doc.getKnowledgeBaseId());
        if (kb == null) {
            markFailed(doc, "知识库不存在: id=" + doc.getKnowledgeBaseId());
            return;
        }

        // 置为处理中
        updateStatus(documentId, STATUS_PROCESSING, null);

        try {
            // 1. 下载 + 解析
            String text = contentExtractor.extract(doc.getDocUrl());
            if (text.isEmpty()) {
                // 清理旧数据后标记完成(0 分块)
                clearExistingChunks(kb, doc);
                finishDocument(documentId, 0);
                log.info("文档解析内容为空,标记完成(0 分块): documentId={}", documentId);
                return;
            }

            // 2. 分块
            List<String> chunkTexts = textChunker.split(text, kb.getChunkSize(), kb.getChunkOverlap());
            if (chunkTexts.isEmpty()) {
                clearExistingChunks(kb, doc);
                finishDocument(documentId, 0);
                return;
            }

            // 3. 重新处理时,先清理该文档旧的分块与向量(幂等)
            clearExistingChunks(kb, doc);

            // 4. 向量化并写入(向量库 + DB 分块表)
            EmbeddingModel embeddingModel = embeddingModelProvider.provide(kb.getEmbeddingModelId());
            int stored = embedAndStore(kb, doc, chunkTexts, embeddingModel);

            // 5. 标记完成
            finishDocument(documentId, stored);
            log.info("文档处理完成: documentId={}, chunkCount={}", documentId, stored);
        } catch (Exception e) {
            log.error("文档处理失败: documentId={}", documentId, e);
            markFailed(doc, e.getMessage());
        }
    }

    /**
     * 分批向量化并写入向量库与分块表。
     *
     * @return 实际写入的分块数
     */
    private int embedAndStore(AiKnowledgeBase kb, AiKnowledgeDocument doc,
                              List<String> chunkTexts, EmbeddingModel embeddingModel) {
        String collection = kb.getCollectionName();
        int total = chunkTexts.size();
        int sortOrder = 0;

        for (int from = 0; from < total; from += EMBED_BATCH_SIZE) {
            int to = Math.min(from + EMBED_BATCH_SIZE, total);
            List<String> batch = chunkTexts.subList(from, to);

            List<TextSegment> segments = new ArrayList<>(batch.size());
            List<String> vectorIds = new ArrayList<>(batch.size());
            List<AiKnowledgeChunk> chunkRows = new ArrayList<>(batch.size());

            for (String content : batch) {
                String vectorId = idService.generateUid(UidTypeEnum.CHAT).toString() + "-" + sortOrder;
                TextSegment segment = TextSegment.from(content);
                segments.add(segment);
                vectorIds.add(vectorId);

                AiKnowledgeChunk chunk = new AiKnowledgeChunk();
                chunk.setId(idService.generateUid(UidTypeEnum.CHAT).longValue());
                chunk.setKnowledgeBaseId(kb.getId());
                chunk.setDocumentId(doc.getId());
                chunk.setVectorId(vectorId);
                chunk.setContent(content);
                chunk.setSortOrder(sortOrder);
                chunk.setCreateBy(doc.getCreateBy());
                chunk.setOrgId(doc.getOrgId());
                chunkRows.add(chunk);
                sortOrder++;
            }

            // 向量化
            Response<List<Embedding>> response = embeddingModel.embedAll(segments);
            List<Embedding> embeddings = response.content();

            // 写向量库
            vectorStoreManager.addAll(collection, vectorIds, embeddings, segments);
            // 写分块表
            for (AiKnowledgeChunk row : chunkRows) {
                chunkMapper.insert(row);
            }
        }
        return total;
    }

    /**
     * 清理某文档已有的分块记录与对应向量(重新处理/删除前调用)。
     */
    public void clearExistingChunks(AiKnowledgeBase kb, AiKnowledgeDocument doc) {
        LambdaQueryWrapper<AiKnowledgeChunk> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AiKnowledgeChunk::getDocumentId, doc.getId());
        List<AiKnowledgeChunk> existing = chunkMapper.selectList(wrapper);
        if (existing.isEmpty()) {
            return;
        }
        List<String> vectorIds = existing.stream()
                .map(AiKnowledgeChunk::getVectorId)
                .filter(v -> v != null && !v.isEmpty())
                .toList();
        try {
            vectorStoreManager.removeAll(kb.getCollectionName(), vectorIds);
        } catch (Exception e) {
            // 向量删除失败不应阻断,记录日志(避免残留时可后续清理)
            log.warn("清理旧向量失败: documentId={}, collection={}", doc.getId(), kb.getCollectionName(), e);
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
        updateStatus(doc.getId(), STATUS_FAILED, reason == null ? "未知错误" : reason);
    }

    private String truncate(String s) {
        if (s == null) {
            return null;
        }
        return s.length() > ERROR_MSG_MAX ? s.substring(0, ERROR_MSG_MAX) : s;
    }
}
