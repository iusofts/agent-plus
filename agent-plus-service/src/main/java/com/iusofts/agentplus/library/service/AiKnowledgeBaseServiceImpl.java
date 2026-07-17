package com.iusofts.agentplus.library.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iusofts.agentplus.library.entity.AiKnowledgeBase;
import com.iusofts.agentplus.library.entity.AiKnowledgeChunk;
import com.iusofts.agentplus.library.entity.AiKnowledgeDocument;
import com.iusofts.agentplus.library.interfaces.IAiKnowledgeBaseService;
import com.iusofts.agentplus.library.interfaces.IAiKnowledgeDocumentService;
import com.iusofts.agentplus.library.knowledge.KnowledgeIngestionService;
import com.iusofts.agentplus.library.mapper.AiKnowledgeBaseMapper;
import com.iusofts.agentplus.library.mapper.AiKnowledgeChunkMapper;
import com.iusofts.agentplus.library.mapper.AiKnowledgeDocumentMapper;
import com.iusofts.agentplus.llm.log.LlmLogRecorder;
import com.iusofts.agentplus.plugin.vectorstore.KnowledgeMetadata;
import com.iusofts.agentplus.plugin.vectorstore.KnowledgeStoreService;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeBaseAddReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeBaseDetailVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeBaseEditReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeBaseQueryPageReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeBaseStatusReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeBaseVo;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * AI知识库 服务实现类
 * </p>
 *
 * @author Ivan
 * @since 2026-07-08
 */
@Service
public class AiKnowledgeBaseServiceImpl extends ServiceImpl<AiKnowledgeBaseMapper, AiKnowledgeBase>
        implements IAiKnowledgeBaseService {

    @Resource
    private IdService idService;

    @Resource
    private AiKnowledgeDocumentMapper aiKnowledgeDocumentMapper;

    @Resource
    private AiKnowledgeChunkMapper aiKnowledgeChunkMapper;

    @Resource
    private KnowledgeStoreService knowledgeStoreService;

    @Resource
    private IAiKnowledgeDocumentService aiKnowledgeDocumentService;

    @Resource
    private LlmLogRecorder llmLogRecorder;

    @Override
    public Long add(AiKnowledgeBaseAddReqVo reqVo) {
        AiKnowledgeBase entity = ModelMapperUtil.strictMap(reqVo, AiKnowledgeBase.class);
        Integer uid = idService.generateUid(UidTypeEnum.KNOWLEDGE_BASE);
        Long id = uid.longValue();
        entity.setId(id);
        // 向量库集合名称按知识库ID约定生成,供后续向量化管线使用
        entity.setCollectionName("kb_" + id);
        entity.setCreateBy(reqVo.getOperatorId());
        super.save(entity);
        return id;
    }

    @Override
    public void edit(AiKnowledgeBaseEditReqVo reqVo) {
        checkDataPermission(reqVo.getId(), reqVo.getOrgId());
        AiKnowledgeBase entity = ModelMapperUtil.strictMap(reqVo, AiKnowledgeBase.class);
        entity.setUpdateBy(reqVo.getOperatorId());
        super.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(AiKnowledgeBaseStatusReqVo reqVo) {
        checkDataPermission(reqVo.getId(), reqVo.getOrgId());
        AiKnowledgeBase entity = super.getById(reqVo.getId());
        if (entity == null) {
            throw new SystemBusinessException("知识库不存在");
        }
        Integer target = reqVo.getStatus();
        if (target == null || (target != 0 && target != 1)) {
            throw new SystemBusinessException("目标状态只能是启用或禁用");
        }
        // 状态未变化时直接返回,避免无意义的文档联动
        if (target.equals(entity.getStatus())) {
            return;
        }
        AiKnowledgeBase updateEntity = new AiKnowledgeBase();
        updateEntity.setId(reqVo.getId());
        updateEntity.setStatus(target);
        updateEntity.setUpdateBy(reqVo.getOperatorId());
        super.updateById(updateEntity);
        // 联动知识库下文档的启停:停用删向量、启用重建向量
        aiKnowledgeDocumentService.cascadeStatusByKnowledgeBase(
                reqVo.getId(), target == 1, reqVo.getOperatorId());
    }

    @Override
    public PageResult<AiKnowledgeBaseVo> queryPage(AiKnowledgeBaseQueryPageReqVo reqVo) {
        PageResult<AiKnowledgeBaseVo> pageResult = new PageResult<>();
        LambdaQueryWrapper<AiKnowledgeBase> wrapper = Wrappers.lambdaQuery();
        if (reqVo.getOrgId() != null) {
            wrapper.eq(AiKnowledgeBase::getOrgId, reqVo.getOrgId());
        }
        if (StringUtils.isNotBlank(reqVo.getName())) {
            wrapper.like(AiKnowledgeBase::getName, reqVo.getName());
        }
        wrapper.orderByDesc(AiKnowledgeBase::getId);
        Page<AiKnowledgeBase> pageParam = new Page<>(reqVo.getCurrentPage(), reqVo.getPageSize());
        IPage<AiKnowledgeBase> page = super.page(pageParam, wrapper);
        List<AiKnowledgeBaseVo> voList = page.getRecords().stream()
                .map(item -> ModelMapperUtil.strictMap(item, AiKnowledgeBaseVo.class))
                .toList();
        pageResult.setDataList(voList);
        pageResult.setTotalCount(page.getTotal());
        return pageResult;
    }

    @Override
    public List<AiKnowledgeBaseVo> queryAll(Integer orgId) {
        LambdaQueryWrapper<AiKnowledgeBase> wrapper = Wrappers.lambdaQuery();
        if (orgId != null) {
            wrapper.eq(AiKnowledgeBase::getOrgId, orgId);
        }
        wrapper.orderByDesc(AiKnowledgeBase::getId);
        List<AiKnowledgeBase> list = super.list(wrapper);
        return list.stream()
                .map(item -> ModelMapperUtil.strictMap(item, AiKnowledgeBaseVo.class))
                .toList();
    }

    @Override
    public void remove(IdReqVo reqVo) {
        checkDataPermission(reqVo.getId(), reqVo.getOrgId());
        AiKnowledgeBase entity = super.getById(reqVo.getId());
        if (entity == null) {
            throw new SystemBusinessException("知识库不存在");
        }
        // 存在关联文档时不允许直接删除
        LambdaQueryWrapper<AiKnowledgeDocument> docWrapper = Wrappers.lambdaQuery();
        docWrapper.eq(AiKnowledgeDocument::getKnowledgeBaseId, reqVo.getId());
        Long docCount = aiKnowledgeDocumentMapper.selectCount(docWrapper);
        if (docCount != null && docCount > 0) {
            throw new SystemBusinessException("当前知识库下存在文档,请先删除文档");
        }
        super.removeById(reqVo.getId());
    }

    @Override
    public AiKnowledgeBaseDetailVo getById(IdReqVo reqVo) {
        checkDataPermission(reqVo.getId(), reqVo.getOrgId());
        AiKnowledgeBase entity = super.getById(reqVo.getId());
        if (entity == null) {
            throw new SystemBusinessException("知识库不存在");
        }
        return ModelMapperUtil.strictMap(entity, AiKnowledgeBaseDetailVo.class);
    }

    private void checkDataPermission(Long id, Integer orgId) {
        LambdaQueryWrapper<AiKnowledgeBase> checkWrapper = Wrappers.lambdaQuery();
        checkWrapper.eq(AiKnowledgeBase::getId, id);
        if (orgId != null) {
            checkWrapper.eq(AiKnowledgeBase::getOrgId, orgId);
        }
        long count = super.count(checkWrapper);
        if (count == 0) {
            throw new SystemBusinessException("操作权限获取失败！");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rebuildAllVectors(IdReqVo reqVo) {
        AiKnowledgeBase kb = super.getById(reqVo.getId());
        if (kb == null) {
            throw new SystemBusinessException("知识库不存在");
        }
        if (reqVo.getOrgId() != null && !reqVo.getOrgId().equals(kb.getOrgId())) {
            throw new SystemBusinessException("操作权限获取失败！");
        }

        // 获取知识库下所有已完成/已禁用/已归档的文档
        List<AiKnowledgeDocument> docs = aiKnowledgeDocumentMapper.selectList(
                Wrappers.<AiKnowledgeDocument>lambdaQuery()
                        .eq(AiKnowledgeDocument::getKnowledgeBaseId, kb.getId())
                        .in(AiKnowledgeDocument::getStatus,
                            KnowledgeIngestionService.STATUS_COMPLETED,
                            KnowledgeIngestionService.STATUS_DISABLED,
                            KnowledgeIngestionService.STATUS_ARCHIVED));

        for (AiKnowledgeDocument doc : docs) {
            List<AiKnowledgeChunk> chunks = aiKnowledgeChunkMapper.selectList(
                    Wrappers.<AiKnowledgeChunk>lambdaQuery().eq(AiKnowledgeChunk::getDocumentId, doc.getId()));

            if (chunks.isEmpty()) {
                continue;
            }

            // 为每个文档重建向量
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
                int embeddingTokens = 0;
                try {
                    embeddingTokens = knowledgeStoreService.batchEmbedAndStore(
                            kb.getCollectionName(), vectorIds, contents, metadatas, kb.getEmbeddingModelId());
                } catch (Exception e) {
                    try {
                        llmLogRecorder.recordKnowledgeDoc()
                                .knowledgeBase(kb.getId(), kb.getName())
                                .document(doc.getId(), doc.getName())
                                .update()
                                .operator(reqVo.getOperatorId(), reqVo.getOrgId())
                                .error(e.getMessage())
                                .record();
                    } catch (Exception logEx) {
                        // 忽略日志记录异常,不影响主流程抛出
                    }
                    throw new SystemBusinessException("重建文档[" + doc.getName() + "]向量失败:" + e.getMessage());
                }
                int totalCharCount = contents.stream().mapToInt(c -> c == null ? 0 : c.length()).sum();
                try {
                    llmLogRecorder.recordKnowledgeDoc()
                            .knowledgeBase(kb.getId(), kb.getName())
                            .document(doc.getId(), doc.getName())
                            .update()
                            .operator(reqVo.getOperatorId(), reqVo.getOrgId())
                            .chunks(vectorIds.size(), totalCharCount, embeddingTokens)
                            .success()
                            .record();
                } catch (Exception logEx) {
                    // 忽略日志记录异常
                }
            }

            // 更新文档更新时间
            AiKnowledgeDocument updateDoc = new AiKnowledgeDocument();
            updateDoc.setId(doc.getId());
            updateDoc.setUpdateBy(reqVo.getOperatorId());
            aiKnowledgeDocumentMapper.updateById(updateDoc);
        }

        // 更新知识库更新时间
        AiKnowledgeBase updateKb = new AiKnowledgeBase();
        updateKb.setId(kb.getId());
        updateKb.setUpdateBy(reqVo.getOperatorId());
        super.updateById(updateKb);
    }

    @Override
    public List<AiKnowledgeBaseVo> listByIds(List<Long> knowledgeBaseIds) {
        if (knowledgeBaseIds == null || knowledgeBaseIds.isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<AiKnowledgeBase> wrapper = Wrappers.lambdaQuery();
        wrapper.in(AiKnowledgeBase::getId, knowledgeBaseIds);
        wrapper.select(AiKnowledgeBase::getId, AiKnowledgeBase::getName, AiKnowledgeBase::getDescription,
                AiKnowledgeBase::getIcon, AiKnowledgeBase::getEmbeddingModelId, AiKnowledgeBase::getChunkSize,
                AiKnowledgeBase::getChunkOverlap, AiKnowledgeBase::getStatus, AiKnowledgeBase::getCreateTime,
                AiKnowledgeBase::getUpdateTime);
        List<AiKnowledgeBase> list = super.list(wrapper);
        return list.stream()
                .map(item -> ModelMapperUtil.strictMap(item, AiKnowledgeBaseVo.class))
                .toList();
    }

}
