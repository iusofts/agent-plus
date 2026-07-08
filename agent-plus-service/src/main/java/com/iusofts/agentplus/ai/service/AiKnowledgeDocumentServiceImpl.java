package com.iusofts.agentplus.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iusofts.agentplus.ai.entity.AiKnowledgeBase;
import com.iusofts.agentplus.ai.entity.AiKnowledgeChunk;
import com.iusofts.agentplus.ai.entity.AiKnowledgeDocument;
import com.iusofts.agentplus.ai.interfaces.IAiKnowledgeDocumentService;
import com.iusofts.agentplus.ai.knowledge.KnowledgeIngestExecutor;
import com.iusofts.agentplus.ai.knowledge.RedisVectorStoreManager;
import com.iusofts.agentplus.ai.mapper.AiKnowledgeBaseMapper;
import com.iusofts.agentplus.ai.mapper.AiKnowledgeChunkMapper;
import com.iusofts.agentplus.ai.mapper.AiKnowledgeDocumentMapper;
import com.iusofts.agentplus.ai.vo.knowledge.AiKnowledgeDocumentAddReqVo;
import com.iusofts.agentplus.ai.vo.knowledge.AiKnowledgeDocumentBatchAddReqVo;
import com.iusofts.agentplus.ai.vo.knowledge.AiKnowledgeDocumentQueryPageReqVo;
import com.iusofts.agentplus.ai.vo.knowledge.AiKnowledgeDocumentVo;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.basic.page.PageResult;
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

    @Override
    public Long add(AiKnowledgeDocumentAddReqVo reqVo) {
        AiKnowledgeBase kb = requireKnowledgeBase(reqVo.getKnowledgeBaseId(), reqVo.getOrgId());
        AiKnowledgeDocument doc = ModelMapperUtil.strictMap(reqVo, AiKnowledgeDocument.class);
        doc.setId(idService.generateUid(UidTypeEnum.CHAT).longValue());
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
            doc.setId(idService.generateUid(UidTypeEnum.CHAT).longValue());
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

}
