package com.iusofts.agentplus.library.service.plugin;

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
import com.iusofts.agentplus.library.interfaces.IAiPluginService;
import com.iusofts.agentplus.library.entity.AiPlugin;
import com.iusofts.agentplus.library.mapper.AiPluginMapper;
import com.iusofts.agentplus.library.vo.plugin.AiPluginAddReqVo;
import com.iusofts.agentplus.library.vo.plugin.AiPluginDetailVo;
import com.iusofts.agentplus.library.vo.plugin.AiPluginEditReqVo;
import com.iusofts.agentplus.library.vo.plugin.AiPluginQueryPageReqVo;
import com.iusofts.agentplus.library.vo.plugin.AiPluginStatusReqVo;
import com.iusofts.agentplus.library.vo.plugin.AiPluginVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * ai插件 服务实现类
 * </p>
 *
 * @author Ivan
 * @since 2026-07-13
 */
@Service
public class AiPluginServiceImpl extends ServiceImpl<AiPluginMapper, AiPlugin> implements IAiPluginService {

    @Resource
    private IdService idService;

    @Override
    public PageResult<AiPluginVo> queryPage(AiPluginQueryPageReqVo reqVo) {
        PageResult<AiPluginVo> pageResult = new PageResult<>();
        LambdaQueryWrapper<AiPlugin> wrapper = Wrappers.lambdaQuery();
        if (reqVo.getOrgId() != null) {
            wrapper.eq(AiPlugin::getOrgId, reqVo.getOrgId());
        }
        if (StringUtils.isNotBlank(reqVo.getName())) {
            wrapper.like(AiPlugin::getName, reqVo.getName());
        }
        if (reqVo.getPluginType() != null) {
            wrapper.eq(AiPlugin::getPluginType, reqVo.getPluginType());
        }
        if (reqVo.getStatus() != null) {
            wrapper.eq(AiPlugin::getStatus, reqVo.getStatus());
        }
        wrapper.orderByAsc(AiPlugin::getSort).orderByDesc(AiPlugin::getId);

        Page<AiPlugin> pageParam = new Page<>(reqVo.getCurrentPage(), reqVo.getPageSize());
        wrapper.select(AiPlugin::getId, AiPlugin::getName, AiPlugin::getPluginType,
                AiPlugin::getDescription, AiPlugin::getIcon, AiPlugin::getSort, AiPlugin::getStatus,
                AiPlugin::getCreateTime, AiPlugin::getUpdateTime);
        IPage<AiPlugin> page = super.page(pageParam, wrapper);

        List<AiPluginVo> voList = page.getRecords().stream()
                .map(item -> ModelMapperUtil.strictMap(item, AiPluginVo.class))
                .toList();

        pageResult.setDataList(voList);
        pageResult.setTotalCount(page.getTotal());
        return pageResult;
    }

    @Override
    public AiPluginDetailVo getById(IdReqVo reqVo) {
        AiPlugin aiPlugin = super.getById(reqVo.getId());
        if (aiPlugin == null) {
            throw new SystemBusinessException("插件不存在");
        }

        AiPluginDetailVo vo = new AiPluginDetailVo();
        vo.setId(aiPlugin.getId());
        vo.setName(aiPlugin.getName());
        vo.setPluginType(aiPlugin.getPluginType());
        vo.setDescription(aiPlugin.getDescription());
        vo.setIcon(aiPlugin.getIcon());
        vo.setPluginConfig(aiPlugin.getPluginConfig());
        vo.setSort(aiPlugin.getSort());
        vo.setStatus(aiPlugin.getStatus());
        vo.setCreateTime(aiPlugin.getCreateTime());
        vo.setUpdateTime(aiPlugin.getUpdateTime());

        return vo;
    }

    @Override
    public void add(AiPluginAddReqVo reqVo) {
        LambdaQueryWrapper<AiPlugin> checkWrapper = Wrappers.lambdaQuery();
        checkWrapper.eq(AiPlugin::getName, reqVo.getName());
        long count = super.count(checkWrapper);
        if (count > 0) {
            throw new SystemBusinessException("插件名称已存在");
        }

        AiPlugin aiPlugin = new AiPlugin();
        aiPlugin.setName(reqVo.getName());
        aiPlugin.setPluginType(reqVo.getPluginType());
        aiPlugin.setDescription(reqVo.getDescription());
        aiPlugin.setIcon(reqVo.getIcon());
        aiPlugin.setPluginConfig(reqVo.getPluginConfig());

        Long id = idService.generateUid(IdService.UidTypeEnum.PLUGIN).longValue();
        aiPlugin.setId(id);
        aiPlugin.setSort(reqVo.getSort() == null ? 0 : reqVo.getSort());
        aiPlugin.setStatus(1);
        aiPlugin.setCreateBy(reqVo.getOperatorId());
        aiPlugin.setOrgId(reqVo.getOrgId());
        super.save(aiPlugin);
    }

    @Override
    public void edit(AiPluginEditReqVo reqVo) {
        AiPlugin aiPlugin = super.getById(reqVo.getId());
        if (aiPlugin == null) {
            throw new SystemBusinessException("插件不存在");
        }
        
        // 如果名称发生变化判重
        if(!reqVo.getName().equals(aiPlugin.getName())) {
            LambdaQueryWrapper<AiPlugin> checkWrapper = Wrappers.lambdaQuery();
            checkWrapper.eq(AiPlugin::getName, reqVo.getName());
            checkWrapper.ne(AiPlugin::getId, reqVo.getId());
            long count = super.count(checkWrapper);
            if (count > 0) {
                throw new SystemBusinessException("插件名称已存在");
            }
        }

        AiPlugin updateEntity = new AiPlugin();
        updateEntity.setId(reqVo.getId());
        if (reqVo.getName() != null) updateEntity.setName(reqVo.getName());
        if (reqVo.getDescription() != null) updateEntity.setDescription(reqVo.getDescription());
        if (reqVo.getIcon() != null) updateEntity.setIcon(reqVo.getIcon());
        if (reqVo.getSort() != null) updateEntity.setSort(reqVo.getSort());
        updateEntity.setPluginConfig(reqVo.getPluginConfig());

        updateEntity.setUpdateBy(reqVo.getOperatorId());
        super.updateById(updateEntity);
    }

    @Override
    public void changeStatus(AiPluginStatusReqVo reqVo) {
        AiPlugin aiPlugin = super.getById(reqVo.getId());
        if (aiPlugin == null) {
            throw new SystemBusinessException("插件不存在");
        }

        AiPlugin updateEntity = new AiPlugin();
        updateEntity.setId(reqVo.getId());
        updateEntity.setStatus(reqVo.getStatus());
        updateEntity.setUpdateBy(reqVo.getOperatorId());
        super.updateById(updateEntity);
    }

    @Override
    public void remove(IdReqVo reqVo) {
        AiPlugin aiPlugin = super.getById(reqVo.getId());
        if (aiPlugin == null) {
            throw new SystemBusinessException("插件不存在");
        }
        super.removeById(reqVo.getId());
    }

}
