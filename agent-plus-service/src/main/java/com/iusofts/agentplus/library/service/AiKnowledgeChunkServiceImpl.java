package com.iusofts.agentplus.library.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iusofts.agentplus.library.entity.AiKnowledgeBase;
import com.iusofts.agentplus.library.entity.AiKnowledgeChunk;
import com.iusofts.agentplus.library.entity.AiKnowledgeDocument;
import com.iusofts.agentplus.library.interfaces.IAiKnowledgeChunkService;
import com.iusofts.agentplus.library.knowledge.KnowledgeIngestionService;
import com.iusofts.agentplus.library.mapper.AiKnowledgeBaseMapper;
import com.iusofts.agentplus.library.mapper.AiKnowledgeChunkMapper;
import com.iusofts.agentplus.library.mapper.AiKnowledgeDocumentMapper;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeChunkAddReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeChunkEditReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeChunkQueryPageReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeChunkStatusReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeChunkVo;
import com.iusofts.agentplus.plugin.vectorstore.KnowledgeStoreService;
import com.iusofts.agentplus.plugin.vectorstore.RedisVectorStoreManager;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.basic.web.vo.page.PageResult;
import com.iusofts.agentplus.basic.utils.ModelMapperUtil;
import com.iusofts.agentplus.basic.utils.StringUtils;
import com.iusofts.agentplus.common.vo.IdReqVo;
import com.iusofts.agentplus.id.service.IdService;
import com.iusofts.agentplus.id.service.IdService.UidTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * AI知识库文档分块 服务实现类
 * </p>
 *
 * <p>提供分块的查询与管理能力。编辑分块内容时,以相同 {@code vectorId} 重新向量化并覆盖
 * 向量库中的向量,保证 DB 分块与向量库一致;删除分块时同步清理向量并递减文档 chunkCount。</p>
 *
 * @author Ivan
 * @since 2026-07-09
 */
@Slf4j
@Service
public class AiKnowledgeChunkServiceImpl extends ServiceImpl<AiKnowledgeChunkMapper, AiKnowledgeChunk>
        implements IAiKnowledgeChunkService {

    @Resource
    private AiKnowledgeBaseMapper aiKnowledgeBaseMapper;

    @Resource
    private AiKnowledgeDocumentMapper aiKnowledgeDocumentMapper;

    @Resource
    private RedisVectorStoreManager vectorStoreManager;

    @Resource
    private KnowledgeStoreService knowledgeStoreService;

    @Resource
    private IdService idService;

    @Override
    public PageResult<AiKnowledgeChunkVo> queryPage(AiKnowledgeChunkQueryPageReqVo reqVo) {
        PageResult<AiKnowledgeChunkVo> pageResult = new PageResult<>();
        LambdaQueryWrapper<AiKnowledgeChunk> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AiKnowledgeChunk::getDocumentId, reqVo.getDocumentId());
        if (reqVo.getOrgId() != null) {
            wrapper.eq(AiKnowledgeChunk::getOrgId, reqVo.getOrgId());
        }
        if (StringUtils.isNotBlank(reqVo.getKeyword())) {
            wrapper.like(AiKnowledgeChunk::getContent, reqVo.getKeyword());
        }
        if (reqVo.getStatus() != null) {
            wrapper.eq(AiKnowledgeChunk::getStatus, reqVo.getStatus());
        }
        wrapper.orderByAsc(AiKnowledgeChunk::getSortOrder);
        Page<AiKnowledgeChunk> pageParam = new Page<>(reqVo.getCurrentPage(), reqVo.getPageSize());
        IPage<AiKnowledgeChunk> page = super.page(pageParam, wrapper);
        List<AiKnowledgeChunkVo> voList = page.getRecords().stream()
                .map(item -> ModelMapperUtil.strictMap(item, AiKnowledgeChunkVo.class))
                .toList();
        pageResult.setDataList(voList);
        pageResult.setTotalCount(page.getTotal());
        return pageResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(AiKnowledgeChunkAddReqVo reqVo) {
        AiKnowledgeDocument doc = aiKnowledgeDocumentMapper.selectById(reqVo.getDocumentId());
        if (doc == null) {
            throw new SystemBusinessException("文档不存在");
        }
        if (reqVo.getOrgId() != null && !reqVo.getOrgId().equals(doc.getOrgId())) {
            throw new SystemBusinessException("操作权限获取失败！");
        }
        AiKnowledgeBase kb = aiKnowledgeBaseMapper.selectById(doc.getKnowledgeBaseId());
        if (kb == null) {
            throw new SystemBusinessException("知识库不存在");
        }

        int sortOrder = nextSortOrder(doc.getId());
        String vectorId = idService.generateUid(UidTypeEnum.CHAT).toString() + "-" + sortOrder;

        Long chunkId = idService.generateUid(UidTypeEnum.CHAT).longValue();
        AiKnowledgeChunk chunk = new AiKnowledgeChunk();
        chunk.setId(chunkId);
        chunk.setKnowledgeBaseId(kb.getId());
        chunk.setDocumentId(doc.getId());
        chunk.setVectorId(vectorId);
        chunk.setContent(reqVo.getContent());
        chunk.setSortOrder(sortOrder);
        chunk.setStatus(KnowledgeIngestionService.CHUNK_STATUS_ENABLED);
        chunk.setCreateBy(reqVo.getOperatorId());
        chunk.setOrgId(doc.getOrgId());

        // 先向量化写入向量库,再落库
        try {
            List<Map<String, Object>> metadatas = List.of(buildChunkMetadata(chunkId, doc));
            knowledgeStoreService.batchEmbedAndStore(
                    kb.getCollectionName(),
                    List.of(vectorId),
                    List.of(reqVo.getContent()),
                    metadatas,
                    kb.getEmbeddingModelId()
            );
        } catch (Exception e) {
            throw new SystemBusinessException("写入向量数据失败:" + e.getMessage());
        }
        super.save(chunk);
        incrementChunkCount(doc);
        return chunk.getId();
    }

    @Override
    public AiKnowledgeChunkVo getById(IdReqVo reqVo) {
        AiKnowledgeChunk chunk = requireChunk(reqVo.getId(), reqVo.getOrgId());
        return ModelMapperUtil.strictMap(chunk, AiKnowledgeChunkVo.class);
    }

    @Override
    public void edit(AiKnowledgeChunkEditReqVo reqVo) {
        AiKnowledgeChunk chunk = requireChunk(reqVo.getId(), reqVo.getOrgId());
        AiKnowledgeBase kb = aiKnowledgeBaseMapper.selectById(chunk.getKnowledgeBaseId());
        AiKnowledgeDocument doc = aiKnowledgeDocumentMapper.selectById(chunk.getDocumentId());
        if (kb == null) {
            throw new SystemBusinessException("知识库不存在");
        }

        // 以相同 vectorId 重新向量化并覆盖向量库中的向量
        try {
            List<Map<String, Object>> metadatas = List.of(buildChunkMetadata(chunk.getId(), doc));
            knowledgeStoreService.batchEmbedAndStore(
                    kb.getCollectionName(),
                    List.of(chunk.getVectorId()),
                    List.of(reqVo.getContent()),
                    metadatas,
                    kb.getEmbeddingModelId()
            );
        } catch (Exception e) {
            throw new SystemBusinessException("更新向量数据失败:" + e.getMessage());
        }

        AiKnowledgeChunk update = new AiKnowledgeChunk();
        update.setId(chunk.getId());
        update.setContent(reqVo.getContent());
        update.setUpdateBy(reqVo.getOperatorId());
        super.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(IdReqVo reqVo) {
        AiKnowledgeChunk chunk = requireChunk(reqVo.getId(), reqVo.getOrgId());
        AiKnowledgeBase kb = aiKnowledgeBaseMapper.selectById(chunk.getKnowledgeBaseId());
        // 先删向量,再删分块记录
        if (kb != null && StringUtils.isNotBlank(chunk.getVectorId())) {
            try {
                vectorStoreManager.removeAll(kb.getCollectionName(), List.of(chunk.getVectorId()));
            } catch (Exception e) {
                throw new SystemBusinessException("删除向量数据失败:" + e.getMessage());
            }
        }
        super.removeById(chunk.getId());
        decrementChunkCount(chunk.getDocumentId());
    }

    @Override
    public void changeStatus(AiKnowledgeChunkStatusReqVo reqVo) {
        int target = reqVo.getStatus() == null ? -1 : reqVo.getStatus();
        if (target != KnowledgeIngestionService.CHUNK_STATUS_ENABLED
                && target != KnowledgeIngestionService.CHUNK_STATUS_DISABLED) {
            throw new SystemBusinessException("无效的分块状态");
        }
        AiKnowledgeChunk chunk = requireChunk(reqVo.getId(), reqVo.getOrgId());
        if (chunk.getStatus() != null && chunk.getStatus() == target) {
            return;
        }
        AiKnowledgeBase kb = aiKnowledgeBaseMapper.selectById(chunk.getKnowledgeBaseId());
        if (kb == null) {
            throw new SystemBusinessException("知识库不存在");
        }

        if (target == KnowledgeIngestionService.CHUNK_STATUS_ENABLED) {
            // 启用:用 DB 中保存的内容重建向量
            if (StringUtils.isBlank(chunk.getVectorId())) {
                throw new SystemBusinessException("分块缺少向量ID,无法重建向量");
            }
            if (StringUtils.isBlank(chunk.getContent())) {
                throw new SystemBusinessException("分块内容为空,无法重建向量");
            }
            AiKnowledgeDocument doc = aiKnowledgeDocumentMapper.selectById(chunk.getDocumentId());
            try {
                List<Map<String, Object>> metadatas = List.of(buildChunkMetadata(chunk.getId(), doc));
                knowledgeStoreService.batchEmbedAndStore(
                        kb.getCollectionName(),
                        List.of(chunk.getVectorId()),
                        List.of(chunk.getContent()),
                        metadatas,
                        kb.getEmbeddingModelId()
                );
            } catch (Exception e) {
                log.error("重建向量数据失败", e);
                throw new SystemBusinessException("重建向量数据失败:" + e.getMessage());
            }
        } else {
            // 停用:从向量库删除该分块向量
            if (StringUtils.isNotBlank(chunk.getVectorId())) {
                try {
                    vectorStoreManager.removeAll(kb.getCollectionName(), List.of(chunk.getVectorId()));
                } catch (Exception e) {
                    throw new SystemBusinessException("删除向量数据失败:" + e.getMessage());
                }
            }
        }

        AiKnowledgeChunk update = new AiKnowledgeChunk();
        update.setId(chunk.getId());
        update.setStatus(target);
        update.setUpdateBy(reqVo.getOperatorId());
        super.updateById(update);
    }

    /**
     * 取指定文档当前最大分块序号+1(无分块时返回 0)。
     */
    private int nextSortOrder(Long documentId) {
        AiKnowledgeChunk last = super.getOne(Wrappers.<AiKnowledgeChunk>lambdaQuery()
                .eq(AiKnowledgeChunk::getDocumentId, documentId)
                .orderByDesc(AiKnowledgeChunk::getSortOrder)
                .last("limit 1"), false);
        if (last == null || last.getSortOrder() == null) {
            return 0;
        }
        return last.getSortOrder() + 1;
    }

    /**
     * 手动新增分块后递增所属文档的分块数量。
     */
    private void incrementChunkCount(AiKnowledgeDocument doc) {
        int count = doc.getChunkCount() == null ? 0 : doc.getChunkCount();
        AiKnowledgeDocument update = new AiKnowledgeDocument();
        update.setId(doc.getId());
        update.setChunkCount(count + 1);
        aiKnowledgeDocumentMapper.updateById(update);
    }

    /**
     * 删除分块后递减所属文档的分块数量(不小于 0)。
     */
    private void decrementChunkCount(Long documentId) {
        AiKnowledgeDocument doc = aiKnowledgeDocumentMapper.selectById(documentId);
        if (doc == null) {
            return;
        }
        int count = doc.getChunkCount() == null ? 0 : doc.getChunkCount();
        AiKnowledgeDocument update = new AiKnowledgeDocument();
        update.setId(documentId);
        update.setChunkCount(Math.max(0, count - 1));
        aiKnowledgeDocumentMapper.updateById(update);
    }

    private AiKnowledgeChunk requireChunk(Long id, Integer orgId) {
        AiKnowledgeChunk chunk = super.getById(id);
        if (chunk == null) {
            throw new SystemBusinessException("分块不存在");
        }
        if (orgId != null && !orgId.equals(chunk.getOrgId())) {
            throw new SystemBusinessException("操作权限获取失败！");
        }
        return chunk;
    }

    private Map<String, Object> buildChunkMetadata(Long chunkId, AiKnowledgeDocument doc) {
        return Map.of(
                "chunkId", chunkId,
                "documentId", doc.getId(),
                "title", doc.getName(),
                "sourceUrl", doc.getDocUrl()
        );
    }

}
