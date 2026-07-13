package com.iusofts.agentplus.library.service.tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.basic.utils.ModelMapperUtil;
import com.iusofts.agentplus.basic.utils.StringUtils;
import com.iusofts.agentplus.basic.web.vo.page.PageResult;
import com.iusofts.agentplus.common.vo.IdReqVo;
import com.iusofts.agentplus.id.service.IdService;
import com.iusofts.agentplus.library.interfaces.IAiToolService;
import com.iusofts.agentplus.library.entity.AiTool;
import com.iusofts.agentplus.library.mapper.AiToolMapper;
import com.iusofts.agentplus.library.vo.tool.AiToolAddReqVo;
import com.iusofts.agentplus.library.vo.tool.AiToolDetailVo;
import com.iusofts.agentplus.library.vo.tool.AiToolEditReqVo;
import com.iusofts.agentplus.library.vo.tool.AiToolQueryPageReqVo;
import com.iusofts.agentplus.library.vo.tool.AiToolStatusReqVo;
import com.iusofts.agentplus.library.vo.tool.AiToolVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * ai工具 服务实现类
 * </p>
 *
 * @author Ivan
 * @since 2026-07-12
 */
@Service
public class AiToolServiceImpl extends ServiceImpl<AiToolMapper, AiTool> implements IAiToolService {

    @Resource
    private IdService idService;

    @Override
    public PageResult<AiToolVo> queryPage(AiToolQueryPageReqVo reqVo) {
        PageResult<AiToolVo> pageResult = new PageResult<>();
        LambdaQueryWrapper<AiTool> wrapper = Wrappers.lambdaQuery();
        if (reqVo.getOrgId() != null) {
            wrapper.eq(AiTool::getOrgId, reqVo.getOrgId());
        }
        if (StringUtils.isNotBlank(reqVo.getName())) {
            wrapper.like(AiTool::getName, reqVo.getName());
        }
        if (reqVo.getPluginId() != null) {
            wrapper.eq(AiTool::getPluginId, reqVo.getPluginId());
        }
        if (reqVo.getType() != null) {
            wrapper.eq(AiTool::getType, reqVo.getType());
        }
        if (reqVo.getStatus() != null) {
            wrapper.eq(AiTool::getStatus, reqVo.getStatus());
        }
        wrapper.orderByDesc(AiTool::getId);

        Page<AiTool> pageParam = new Page<>(reqVo.getCurrentPage(), reqVo.getPageSize());
        wrapper.select(AiTool::getId, AiTool::getName, AiTool::getPluginId, AiTool::getType,
                AiTool::getDescription, AiTool::getIcon, AiTool::getStatus,
                AiTool::getCreateTime, AiTool::getUpdateTime);
        IPage<AiTool> page = super.page(pageParam, wrapper);

        List<AiToolVo> voList = page.getRecords().stream()
                .map(item -> ModelMapperUtil.strictMap(item, AiToolVo.class))
                .toList();

        pageResult.setDataList(voList);
        pageResult.setTotalCount(page.getTotal());
        return pageResult;
    }

    @Override
    public AiToolDetailVo getById(IdReqVo reqVo) {
        AiTool aiTool = super.getById(reqVo.getId());
        if (aiTool == null) {
            throw new SystemBusinessException("工具不存在");
        }

        AiToolDetailVo vo = new AiToolDetailVo();
        vo.setId(aiTool.getId());
        vo.setName(aiTool.getName());
        vo.setPluginId(aiTool.getPluginId());
        vo.setType(aiTool.getType());
        vo.setDescription(aiTool.getDescription());
        vo.setIcon(aiTool.getIcon());
        vo.setStatus(aiTool.getStatus());
        vo.setCreateTime(aiTool.getCreateTime());
        vo.setUpdateTime(aiTool.getUpdateTime());
        vo.setParamsSchema(aiTool.getParamsSchema());
        vo.setResponseSchema(aiTool.getResponseSchema());
        vo.setHttpConfig(aiTool.getHttpConfig());

        return vo;
    }

    @Override
    public void add(AiToolAddReqVo reqVo) {
        LambdaQueryWrapper<AiTool> checkWrapper = Wrappers.lambdaQuery();
        checkWrapper.eq(AiTool::getPluginId, reqVo.getPluginId());
        checkWrapper.eq(AiTool::getName, reqVo.getName());
        long count = super.count(checkWrapper);
        if (count > 0) {
            throw new SystemBusinessException("同一插件下工具名称已存在");
        }

        AiTool aiTool = new AiTool();
        aiTool.setName(reqVo.getName());
        aiTool.setPluginId(reqVo.getPluginId());
        aiTool.setType(reqVo.getType());
        aiTool.setDescription(reqVo.getDescription());
        aiTool.setIcon(reqVo.getIcon());
        aiTool.setParamsSchema(reqVo.getParamsSchema());
        aiTool.setResponseSchema(reqVo.getResponseSchema());
        aiTool.setHttpConfig(reqVo.getHttpConfig());

        Long id = idService.generateUid(IdService.UidTypeEnum.TOOL).longValue();
        aiTool.setId(id);
        aiTool.setStatus(1);
        aiTool.setCreateBy(reqVo.getOperatorId());
        aiTool.setOrgId(reqVo.getOrgId());
        super.save(aiTool);
    }

    @Override
    public void edit(AiToolEditReqVo reqVo) {
        AiTool aiTool = super.getById(reqVo.getId());
        if (aiTool == null) {
            throw new SystemBusinessException("工具不存在");
        }

        Long targetPluginId = aiTool.getPluginId() != null ? aiTool.getPluginId() : aiTool.getPluginId();
        String targetName = reqVo.getName() != null ? reqVo.getName() : aiTool.getName();
        if (reqVo.getName() != null || aiTool.getPluginId() != null) {
            LambdaQueryWrapper<AiTool> checkWrapper = Wrappers.lambdaQuery();
            checkWrapper.eq(AiTool::getPluginId, targetPluginId);
            checkWrapper.eq(AiTool::getName, targetName);
            checkWrapper.ne(AiTool::getId, reqVo.getId());
            long count = super.count(checkWrapper);
            if (count > 0) {
                throw new SystemBusinessException("同一插件下工具名称已存在");
            }
        }

        AiTool updateEntity = new AiTool();
        updateEntity.setId(reqVo.getId());
        if (reqVo.getName() != null) updateEntity.setName(reqVo.getName());
        if (reqVo.getDescription() != null) updateEntity.setDescription(reqVo.getDescription());
        if (reqVo.getIcon() != null) updateEntity.setIcon(reqVo.getIcon());
        updateEntity.setParamsSchema(reqVo.getParamsSchema());
        updateEntity.setResponseSchema(reqVo.getResponseSchema());
        updateEntity.setHttpConfig(reqVo.getHttpConfig());

        updateEntity.setUpdateBy(reqVo.getOperatorId());
        super.updateById(updateEntity);
    }

    @Override
    public void changeStatus(AiToolStatusReqVo reqVo) {
        AiTool aiTool = super.getById(reqVo.getId());
        if (aiTool == null) {
            throw new SystemBusinessException("工具不存在");
        }

        AiTool updateEntity = new AiTool();
        updateEntity.setId(reqVo.getId());
        updateEntity.setStatus(reqVo.getStatus());
        updateEntity.setUpdateBy(reqVo.getOperatorId());
        super.updateById(updateEntity);
    }

    @Override
    public void remove(IdReqVo reqVo) {
        AiTool aiTool = super.getById(reqVo.getId());
        if (aiTool == null) {
            throw new SystemBusinessException("工具不存在");
        }
        super.removeById(reqVo.getId());
    }

}
