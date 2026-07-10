package com.iusofts.agentplus.library.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iusofts.agentplus.library.entity.AiKnowledgeBase;
import com.iusofts.agentplus.library.entity.AiKnowledgeDocument;
import com.iusofts.agentplus.library.interfaces.IAiKnowledgeBaseService;
import com.iusofts.agentplus.library.mapper.AiKnowledgeBaseMapper;
import com.iusofts.agentplus.library.mapper.AiKnowledgeDocumentMapper;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeBaseAddReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeBaseDetailVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeBaseEditReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeBaseQueryPageReqVo;
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

import java.util.List;

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

}
