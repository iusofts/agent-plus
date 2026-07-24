package com.iusofts.agentplus.library.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iusofts.agentplus.library.entity.AiKnowledgeBase;
import com.iusofts.agentplus.library.entity.AiKnowledgeChunk;
import com.iusofts.agentplus.library.entity.AiKnowledgeDocument;
import com.iusofts.agentplus.library.interfaces.IAiKnowledgeDocumentService;
import com.iusofts.agentplus.library.knowledge.KnowledgeIngestExecutor;
import com.iusofts.agentplus.library.knowledge.KnowledgeIngestionService;
import com.iusofts.agentplus.ailog.dto.AiTraceContext;
import com.iusofts.agentplus.llm.log.LlmLogRecorder;
import com.iusofts.agentplus.plugin.vectorstore.KnowledgeMetadata;
import com.iusofts.agentplus.plugin.vectorstore.KnowledgeStoreService;
import com.iusofts.agentplus.plugin.vectorstore.RedisVectorStoreManager;
import com.iusofts.agentplus.library.mapper.AiKnowledgeBaseMapper;
import com.iusofts.agentplus.library.mapper.AiKnowledgeChunkMapper;
import com.iusofts.agentplus.library.mapper.AiKnowledgeDocumentMapper;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeDocumentAddReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeDocumentBatchAddReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeDocumentQueryPageReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeDocumentStatusReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeDocumentVo;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.basic.web.vo.page.PageResult;
import com.iusofts.agentplus.basic.utils.ModelMapperUtil;
import com.iusofts.agentplus.basic.utils.StringUtils;
import com.iusofts.agentplus.common.vo.IdReqVo;
import com.iusofts.agentplus.id.service.IdService;
import com.iusofts.agentplus.id.service.IdService.UidTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * AI知识库文档 服务实现类
 * </p>
 *
 * <p>新增时仅登记文档元数据(OSS url + 文件名),status=0(待处理),随后在事务提交后
 * 异步提交到有界线程池执行「下载->解析->分块->向量化->落库」管线。</p>
 *
 * @author Ivan
 * @since 2026-07-08
 */
@Service
public class AiKnowledgeDocumentServiceImpl extends ServiceImpl<AiKnowledgeDocumentMapper, AiKnowledgeDocument>
        implements IAiKnowledgeDocumentService {

    /** 文档状态:待处理。 */
    private static final int STATUS_PENDING = 0;

    @Resource
    private IdService idService;

    @Resource
    private AiKnowledgeBaseMapper aiKnowledgeBaseMapper;

    @Resource
    private AiKnowledgeChunkMapper aiKnowledgeChunkMapper;

    @Resource
    private KnowledgeIngestExecutor ingestExecutor;

    @Resource
    private RedisVectorStoreManager vectorStoreManager;

    @Resource
    private KnowledgeStoreService knowledgeStoreService;

    @Resource
    private LlmLogRecorder llmLogRecorder;

    @Override
    public Long add(AiKnowledgeDocumentAddReqVo reqVo) {
        AiKnowledgeBase kb = requireKnowledgeBase(reqVo.getKnowledgeBaseId(), reqVo.getOrgId());
        AiKnowledgeDocument doc = ModelMapperUtil.strictMap(reqVo, AiKnowledgeDocument.class);
        doc.setId(idService.generateUid(UidTypeEnum.KNOWLEDGE_DOCUMENT).longValue());
        doc.setStatus(STATUS_PENDING);
        doc.setChunkCount(0);
        doc.setOrgId(kb.getOrgId());
        doc.setCreateBy(reqVo.getOperatorId());
        super.save(doc);
        submitAfterCommit(doc.getId());
        return doc.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAdd(AiKnowledgeDocumentBatchAddReqVo reqVo) {
        AiKnowledgeBase kb = requireKnowledgeBase(reqVo.getKnowledgeBaseId(), reqVo.getOrgId());
        List<AiKnowledgeDocument> docs = new ArrayList<>();
        for (AiKnowledgeDocumentBatchAddReqVo.DocItem item : reqVo.getDocuments()) {
            if (item == null || StringUtils.isBlank(item.getDocUrl()) || StringUtils.isBlank(item.getName())) {
                throw new SystemBusinessException("文档名称与URL不能为空");
            }
            AiKnowledgeDocument doc = new AiKnowledgeDocument();
            doc.setId(idService.generateUid(UidTypeEnum.KNOWLEDGE_DOCUMENT).longValue());
            doc.setKnowledgeBaseId(reqVo.getKnowledgeBaseId());
            doc.setName(item.getName());
            doc.setDocType(item.getDocType());
            doc.setDocUrl(item.getDocUrl());
            doc.setStatus(STATUS_PENDING);
            doc.setChunkCount(0);
            doc.setOrgId(kb.getOrgId());
            doc.setCreateBy(reqVo.getOperatorId());
            docs.add(doc);
        }
        super.saveBatch(docs);
        for (AiKnowledgeDocument doc : docs) {
            submitAfterCommit(doc.getId());
        }
    }

    @Override
    public PageResult<AiKnowledgeDocumentVo> queryPage(AiKnowledgeDocumentQueryPageReqVo reqVo) {
        PageResult<AiKnowledgeDocumentVo> pageResult = new PageResult<>();
        LambdaQueryWrapper<AiKnowledgeDocument> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AiKnowledgeDocument::getKnowledgeBaseId, reqVo.getKnowledgeBaseId());
        if (reqVo.getOrgId() != null) {
            wrapper.eq(AiKnowledgeDocument::getOrgId, reqVo.getOrgId());
        }
        if (StringUtils.isNotBlank(reqVo.getName())) {
            wrapper.like(AiKnowledgeDocument::getName, reqVo.getName());
        }
        if (reqVo.getStatus() != null) {
            wrapper.eq(AiKnowledgeDocument::getStatus, reqVo.getStatus());
        }
        wrapper.orderByDesc(AiKnowledgeDocument::getId);
        Page<AiKnowledgeDocument> pageParam = new Page<>(reqVo.getCurrentPage(), reqVo.getPageSize());
        IPage<AiKnowledgeDocument> page = super.page(pageParam, wrapper);
        List<AiKnowledgeDocumentVo> voList = page.getRecords().stream()
                .map(item -> ModelMapperUtil.strictMap(item, AiKnowledgeDocumentVo.class))
                .toList();
        pageResult.setDataList(voList);
        pageResult.setTotalCount(page.getTotal());
        return pageResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(IdReqVo reqVo) {
        AiKnowledgeDocument doc = super.getById(reqVo.getId());
        if (doc == null) {
            throw new SystemBusinessException("文档不存在");
        }
        if (reqVo.getOrgId() != null && !reqVo.getOrgId().equals(doc.getOrgId())) {
            throw new SystemBusinessException("操作权限获取失败！");
        }
        // 先删除向量库中的向量,再删除分块记录,最后删文档
        AiKnowledgeBase kb = aiKnowledgeBaseMapper.selectById(doc.getKnowledgeBaseId());
        LambdaQueryWrapper<AiKnowledgeChunk> chunkWrapper = Wrappers.lambdaQuery();
        chunkWrapper.eq(AiKnowledgeChunk::getDocumentId, reqVo.getId());
        if (kb != null) {
            List<AiKnowledgeChunk> chunks = aiKnowledgeChunkMapper.selectList(chunkWrapper);
            List<String> vectorIds = chunks.stream()
                    .map(AiKnowledgeChunk::getVectorId)
                    .filter(v -> v != null && !v.isEmpty())
                    .toList();
            try {
                vectorStoreManager.removeAll(kb.getCollectionName(), vectorIds);
            } catch (Exception e) {
                throw new SystemBusinessException("删除向量数据失败:" + e.getMessage());
            }
        }
        aiKnowledgeChunkMapper.delete(chunkWrapper);
        super.removeById(reqVo.getId());
    }

    @Override
    public AiKnowledgeDocumentVo getById(IdReqVo reqVo) {
        AiKnowledgeDocument doc = super.getById(reqVo.getId());
        if (doc == null) {
            throw new SystemBusinessException("文档不存在");
        }
        if (reqVo.getOrgId() != null && !reqVo.getOrgId().equals(doc.getOrgId())) {
            throw new SystemBusinessException("操作权限获取失败！");
        }
        return ModelMapperUtil.strictMap(doc, AiKnowledgeDocumentVo.class);
    }

    /**
     * 文档状态变更。仅支持「可用」与「已禁用/已归档」之间切换:
     * <ul>
     *   <li>可用 -> 已禁用/已归档:停用文档下所有分块并删除其向量,RAG 检索不再命中;</li>
     *   <li>已禁用/已归档 -> 可用:重新启用所有分块并用 DB 保存的内容重建向量。</li>
     * </ul>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(AiKnowledgeDocumentStatusReqVo reqVo) {
        int target = reqVo.getStatus() == null ? -1 : reqVo.getStatus();
        if (target != KnowledgeIngestionService.STATUS_COMPLETED
                && target != KnowledgeIngestionService.STATUS_DISABLED
                && target != KnowledgeIngestionService.STATUS_ARCHIVED) {
            throw new SystemBusinessException("目标状态只能是可用、已禁用或已归档");
        }
        AiKnowledgeDocument doc = super.getById(reqVo.getId());
        if (doc == null) {
            throw new SystemBusinessException("文档不存在");
        }
        if (reqVo.getOrgId() != null && !reqVo.getOrgId().equals(doc.getOrgId())) {
            throw new SystemBusinessException("操作权限获取失败！");
        }
        int current = doc.getStatus() == null ? -1 : doc.getStatus();
        // 只有已就绪的文档(可用/已禁用/已归档)才允许手动切换
        if (current != KnowledgeIngestionService.STATUS_COMPLETED
                && current != KnowledgeIngestionService.STATUS_DISABLED
                && current != KnowledgeIngestionService.STATUS_ARCHIVED) {
            throw new SystemBusinessException("当前文档状态不支持该操作");
        }
        if (current == target) {
            return;
        }

        AiKnowledgeBase kb = aiKnowledgeBaseMapper.selectById(doc.getKnowledgeBaseId());
        if (kb == null) {
            throw new SystemBusinessException("知识库不存在");
        }

        List<AiKnowledgeChunk> chunks = aiKnowledgeChunkMapper.selectList(
                Wrappers.<AiKnowledgeChunk>lambdaQuery().eq(AiKnowledgeChunk::getDocumentId, doc.getId()));

        boolean enable = target == KnowledgeIngestionService.STATUS_COMPLETED;
        if (enable) {
            enableChunks(kb, doc, chunks, reqVo.getOperatorId(), reqVo.getOrgId());
        } else {
            disableChunks(kb, chunks);
        }

        AiKnowledgeDocument update = new AiKnowledgeDocument();
        update.setId(doc.getId());
        update.setStatus(target);
        update.setUpdateBy(reqVo.getOperatorId());
        super.updateById(update);
    }

    /**
     * 启用文档下所有分块:置 status=1,并用 DB 保存的内容重建向量。
     */
    private void enableChunks(AiKnowledgeBase kb, AiKnowledgeDocument doc, List<AiKnowledgeChunk> chunks,
                              Long operatorId, Integer orgId) {
        if (chunks.isEmpty()) {
            return;
        }
        List<String> vectorIds = new ArrayList<>();
        List<String> contents = new ArrayList<>();
        List<Map<String, Object>> metadatas = new ArrayList<>();
        for (AiKnowledgeChunk c : chunks) {
            if (StringUtils.isNotBlank(c.getVectorId())) {
                vectorIds.add(c.getVectorId());
                contents.add(c.getContent());
                metadatas.add(KnowledgeMetadata.build(c.getId(), doc.getId(), doc.getName(), doc.getDocUrl()));
            }
        }
        if (!vectorIds.isEmpty()) {
            int embeddingTokens;
            AiTraceContext ctx = AiTraceContext.builder()
                    .traceId(LlmLogRecorder.generateTraceId())
                    .operatorId(operatorId)
                    .orgId(orgId)
                    .build();
            try {
                embeddingTokens = knowledgeStoreService.batchEmbedAndStore(
                        kb.getCollectionName(), vectorIds, contents, metadatas,
                        kb.getEmbeddingModelId(), kb.getId(), ctx);
            } catch (Exception e) {
                recordDocLogSafely(kb, doc, operatorId, orgId, null, null, null, e.getMessage());
                throw new SystemBusinessException("重建向量数据失败:" + e.getMessage());
            }
            int totalCharCount = contents.stream().mapToInt(c -> c == null ? 0 : c.length()).sum();
            recordDocLogSafely(kb, doc, operatorId, orgId, vectorIds.size(), totalCharCount,
                    embeddingTokens, null);
        }
        updateChunkStatus(chunks, KnowledgeIngestionService.CHUNK_STATUS_ENABLED);
    }

    /**
     * 停用文档下所有分块:置 status=0,并从向量库删除其向量。
     */
    private void disableChunks(AiKnowledgeBase kb, List<AiKnowledgeChunk> chunks) {
        if (chunks.isEmpty()) {
            return;
        }
        List<String> vectorIds = chunks.stream()
                .map(AiKnowledgeChunk::getVectorId)
                .filter(StringUtils::isNotBlank)
                .toList();
        try {
            vectorStoreManager.removeAll(kb.getCollectionName(), vectorIds);
        } catch (Exception e) {
            throw new SystemBusinessException("删除向量数据失败:" + e.getMessage());
        }
        updateChunkStatus(chunks, KnowledgeIngestionService.CHUNK_STATUS_DISABLED);
    }

    private void updateChunkStatus(List<AiKnowledgeChunk> chunks, int status) {
        for (AiKnowledgeChunk c : chunks) {
            AiKnowledgeChunk update = new AiKnowledgeChunk();
            update.setId(c.getId());
            update.setStatus(status);
            aiKnowledgeChunkMapper.updateById(update);
        }
    }

    /**
     * 在当前事务提交后再提交异步处理任务,避免异步线程读不到尚未提交的文档记录。
     * 无事务时直接提交。
     */
    private void submitAfterCommit(Long documentId) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    ingestExecutor.submit(documentId);
                }
            });
        } else {
            ingestExecutor.submit(documentId);
        }
    }

    private AiKnowledgeBase requireKnowledgeBase(Long knowledgeBaseId, Integer orgId) {
        AiKnowledgeBase kb = aiKnowledgeBaseMapper.selectById(knowledgeBaseId);
        if (kb == null) {
            throw new SystemBusinessException("知识库不存在");
        }
        if (orgId != null && !orgId.equals(kb.getOrgId())) {
            throw new SystemBusinessException("操作权限获取失败！");
        }
        return kb;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rebuildVector(IdReqVo reqVo) {
        AiKnowledgeDocument doc = super.getById(reqVo.getId());
        if (doc == null) {
            throw new SystemBusinessException("文档不存在");
        }
        if (reqVo.getOrgId() != null && !reqVo.getOrgId().equals(doc.getOrgId())) {
            throw new SystemBusinessException("操作权限获取失败！");
        }
        int current = doc.getStatus() == null ? -1 : doc.getStatus();
        // 只有已就绪的文档才允许重建向量
        if (current != KnowledgeIngestionService.STATUS_COMPLETED
                && current != KnowledgeIngestionService.STATUS_DISABLED
                && current != KnowledgeIngestionService.STATUS_ARCHIVED) {
            throw new SystemBusinessException("当前文档状态不支持该操作");
        }

        AiKnowledgeBase kb = aiKnowledgeBaseMapper.selectById(doc.getKnowledgeBaseId());
        if (kb == null) {
            throw new SystemBusinessException("知识库不存在");
        }

        List<AiKnowledgeChunk> chunks = aiKnowledgeChunkMapper.selectList(
                Wrappers.<AiKnowledgeChunk>lambdaQuery().eq(AiKnowledgeChunk::getDocumentId, doc.getId()));

        if (chunks.isEmpty()) {
            return;
        }

        // 重建向量
        List<String> vectorIds = new ArrayList<>();
        List<String> contents = new ArrayList<>();
        List<Map<String, Object>> metadatas = new ArrayList<>();
        for (AiKnowledgeChunk c : chunks) {
            if (StringUtils.isNotBlank(c.getVectorId())) {
                vectorIds.add(c.getVectorId());
                contents.add(c.getContent());
                metadatas.add(KnowledgeMetadata.build(c.getId(), doc.getId(), doc.getName(), doc.getDocUrl()));
            }
        }
        if (!vectorIds.isEmpty()) {
            int embeddingTokens;
            AiTraceContext ctx = AiTraceContext.builder()
                    .traceId(LlmLogRecorder.generateTraceId())
                    .operatorId(reqVo.getOperatorId())
                    .orgId(reqVo.getOrgId())
                    .build();
            try {
                embeddingTokens = knowledgeStoreService.batchEmbedAndStore(
                        kb.getCollectionName(), vectorIds, contents, metadatas,
                        kb.getEmbeddingModelId(), kb.getId(), ctx);
            } catch (Exception e) {
                recordDocLogSafely(kb, doc, reqVo.getOperatorId(), reqVo.getOrgId(),
                        null, null, null, e.getMessage());
                throw new SystemBusinessException("重建向量数据失败:" + e.getMessage());
            }
            int totalCharCount = contents.stream().mapToInt(c -> c == null ? 0 : c.length()).sum();
            recordDocLogSafely(kb, doc, reqVo.getOperatorId(), reqVo.getOrgId(),
                    vectorIds.size(), totalCharCount, embeddingTokens, null);
        }

        // 更新文档更新时间
        AiKnowledgeDocument update = new AiKnowledgeDocument();
        update.setId(doc.getId());
        update.setUpdateBy(reqVo.getOperatorId());
        super.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cascadeStatusByKnowledgeBase(Long knowledgeBaseId, boolean enable, Long operatorId) {
        AiKnowledgeBase kb = aiKnowledgeBaseMapper.selectById(knowledgeBaseId);
        if (kb == null) {
            throw new SystemBusinessException("知识库不存在");
        }
        // 启用时恢复「已禁用」文档,停用时处理「可用」文档;归档/失败/处理中的文档不联动
        int fromStatus = enable ? KnowledgeIngestionService.STATUS_DISABLED
                : KnowledgeIngestionService.STATUS_COMPLETED;
        int toStatus = enable ? KnowledgeIngestionService.STATUS_COMPLETED
                : KnowledgeIngestionService.STATUS_DISABLED;

        List<AiKnowledgeDocument> docs = super.list(
                Wrappers.<AiKnowledgeDocument>lambdaQuery()
                        .eq(AiKnowledgeDocument::getKnowledgeBaseId, knowledgeBaseId)
                        .eq(AiKnowledgeDocument::getStatus, fromStatus));

        for (AiKnowledgeDocument doc : docs) {
            List<AiKnowledgeChunk> chunks = aiKnowledgeChunkMapper.selectList(
                    Wrappers.<AiKnowledgeChunk>lambdaQuery().eq(AiKnowledgeChunk::getDocumentId, doc.getId()));
            if (enable) {
                enableChunks(kb, doc, chunks, operatorId, kb.getOrgId());
            } else {
                disableChunks(kb, chunks);
            }
            AiKnowledgeDocument update = new AiKnowledgeDocument();
            update.setId(doc.getId());
            update.setStatus(toStatus);
            update.setUpdateBy(operatorId);
            super.updateById(update);
        }
    }

    /**
     * 文档粒度的向量重建日志。errorMessage 非空表示失败;为空表示成功。
     * 日志记录失败不影响主流程。
     */
    private void recordDocLogSafely(AiKnowledgeBase kb, AiKnowledgeDocument doc,
                                     Long operatorId, Integer orgId,
                                     Integer chunkCount, Integer totalChars,
                                     Integer embeddingTokens, String errorMessage) {
        try {
            LlmLogRecorder.KnowledgeDocRecorder recorder = llmLogRecorder.recordKnowledgeDoc()
                    .knowledgeBase(kb == null ? null : kb.getId(), kb == null ? null : kb.getName())
                    .document(doc == null ? null : doc.getId(), doc == null ? null : doc.getName())
                    .update()
                    .operator(operatorId, orgId);
            if (errorMessage != null) {
                recorder.error(errorMessage);
            } else {
                recorder.chunks(chunkCount, totalChars, embeddingTokens).success();
            }
            recorder.record();
        } catch (Exception logEx) {
            // 日志失败不影响主流程
        }
    }

}
