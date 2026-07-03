package com.iusofts.agentplus.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iusofts.agentplus.ai.interfaces.IAiAgentService;
import com.iusofts.agentplus.ai.entity.AiAgent;
import com.iusofts.agentplus.ai.mapper.AiAgentMapper;
import com.iusofts.agentplus.ai.vo.*;
import com.iusofts.agentplus.basic.enums.YesNoEnums;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.basic.page.PageResult;
import com.iusofts.agentplus.basic.utils.ModelMapperUtil;
import com.iusofts.agentplus.basic.utils.StringUtils;
import com.iusofts.agentplus.basics.entity.Industry;
import com.iusofts.agentplus.basics.mapper.IndustryMapper;
import com.iusofts.agentplus.common.vo.IdReqVo;
import com.iusofts.agentplus.id.service.IdService;
import com.iusofts.agentplus.id.service.IdService.UidTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * ai智能体 服务实现类
 * </p>
 *
 * @author Ivan
 * @since 2026-03-31
 */
@Service
public class AiAgentServiceImpl extends ServiceImpl<AiAgentMapper, AiAgent> implements IAiAgentService {

    @Resource
    private IdService idService;
    @Autowired
    private IndustryMapper industryMapper;

    @Override
    public void add(AiAgentAddReqVo reqVo) {
        AiAgent aiAgent = ModelMapperUtil.strictMap(reqVo, AiAgent.class);
        Integer uid = idService.generateUid(UidTypeEnum.CHAT);
        aiAgent.setId(uid.longValue());
        aiAgent.setCreateBy(reqVo.getOperatorId()); // 设置创建人
        if (reqVo.getIndustryId() != null) {
            Industry industry = industryMapper.selectById(reqVo.getIndustryId());
            if (industry != null) {
                aiAgent.setIndustryId(industry.getId());
                aiAgent.setIndustryName(industry.getName());
            }
        }
        super.save(aiAgent);
    }

    @Override
    public void edit(AiAgentEditReqVo reqVo) {
        checkDataPermission(reqVo.getId(), reqVo.getOrgId());
        AiAgent aiAgent = ModelMapperUtil.strictMap(reqVo, AiAgent.class);
        aiAgent.setUpdateBy(reqVo.getOperatorId()); // 设置更新人
        if (reqVo.getIndustryId() != null) {
            Industry industry = industryMapper.selectById(reqVo.getIndustryId());
            if (industry != null) {
                aiAgent.setIndustryId(industry.getId());
                aiAgent.setIndustryName(industry.getName());
            }
        }
        super.updateById(aiAgent);
    }

    @Override
    public PageResult<AiAgentVo> queryPage(AiAgentQueryPageReqVo reqVo) {
        PageResult<AiAgentVo> pageResult = new PageResult<>();
        LambdaQueryWrapper<AiAgent> wrapper = Wrappers.lambdaQuery();
        if (reqVo.getOrgId() != null) {
            wrapper.eq(AiAgent::getOrgId, reqVo.getOrgId());
        }
        if (StringUtils.isNotBlank(reqVo.getName())) {
            wrapper.like(AiAgent::getName, reqVo.getName());
        }
        if (reqVo.getType() != null) {
            wrapper.eq(AiAgent::getType, reqVo.getType());
        }
        if (reqVo.getIsDefault() != null) {
            wrapper.eq(AiAgent::getIsDefault, reqVo.getIsDefault());
        }
        if (reqVo.getIsSystem() != null) {
            wrapper.eq(AiAgent::getIsSystem, reqVo.getIsSystem());
        }
        if(reqVo.getIndustryId() != null) {
            wrapper.eq(AiAgent::getIndustryId, reqVo.getIndustryId());
        }
        wrapper.orderByDesc(AiAgent::getId);
        Page<AiAgent> pageParam = new Page<>(reqVo.getCurrentPage(), reqVo.getPageSize());
        // 使用select方法排除systemPrompt字段
        wrapper.select(AiAgent.class, field -> !field.getColumn().equals("system_prompt"));
        IPage<AiAgent> page = super.page(pageParam, wrapper);
        List<AiAgentVo> voList = page.getRecords().stream().map(item -> {
            AiAgentVo vo = ModelMapperUtil.strictMap(item, AiAgentVo.class);
            return vo;
        }).toList();
        pageResult.setDataList(voList);
        pageResult.setTotalCount(page.getTotal());
        return pageResult;
    }

    @Override
    public List<AiAgentVo> queryAll(Integer orgId) {
        LambdaQueryWrapper<AiAgent> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AiAgent::getOrgId, orgId);
        List<AiAgent> list = super.list(wrapper);
        List<AiAgentVo> voList = list.stream().map(item -> {
            AiAgentVo vo = ModelMapperUtil.strictMap(item, AiAgentVo.class);
            return vo;
        }).toList();
        return voList;
    }

    @Override
    public void remove(IdReqVo reqVo) {
        checkDataPermission(reqVo.getId(), reqVo.getOrgId());
        AiAgent aiAgent = super.getById(reqVo.getId());
        if (aiAgent == null) {
            throw new SystemBusinessException("智能体不存在");
        }
        if (aiAgent.getIsSystem() == YesNoEnums.YES.getCode()) {
            throw new SystemBusinessException("当前智能体为系统预设不能删除");
        }
        if (aiAgent.getIsDefault() == YesNoEnums.YES.getCode()) {
            throw new SystemBusinessException("当前智能体为默认设置不能删除");
        }
        super.removeById(reqVo.getId());
    }

    @Override
    public AiAgentDetailVo getById(IdReqVo reqVo) {
        checkDataPermission(reqVo.getId(), reqVo.getOrgId());
        AiAgent aiAgent = super.getById(reqVo.getId());
        if (aiAgent == null) {
            throw new SystemBusinessException("智能体不存在");
        }
        AiAgentDetailVo vo = ModelMapperUtil.strictMap(aiAgent, AiAgentDetailVo.class);
        return vo;
    }

    @Override
    public void setDefault(AiAgentSetDefaultReqVo reqVo) {
        // 获取要设置的智能体信息
        AiAgent currentAgent = super.getById(reqVo.getId());
        if (currentAgent == null) {
            throw new SystemBusinessException("智能体不存在");
        }

        // 验证权限
        checkDataPermission(reqVo.getId(), reqVo.getOrgId());

        Integer orgId = currentAgent.getOrgId(); // 从数据库中获取orgId而不是依赖参数
        Integer type = currentAgent.getType(); // 从数据库中获取type
        
        // 将该类型下其他智能体的isDefault设为0
        LambdaQueryWrapper<AiAgent> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AiAgent::getOrgId, orgId)
               .eq(AiAgent::getType, type)
               .eq(AiAgent::getIsDefault, 1); // 找到当前默认智能体

        List<AiAgent> defaultAgents = super.list(wrapper);

        // 更新其他智能体为非默认
        for (AiAgent agent : defaultAgents) {
            if (!agent.getId().equals(reqVo.getId())) { // 不是当前正在设置的智能体
                agent.setIsDefault(0);
                agent.setUpdateBy(reqVo.getOperatorId());
                super.updateById(agent);
            }
        }

        // 设置当前智能体为默认
        currentAgent.setIsDefault(1);
        currentAgent.setUpdateBy(reqVo.getOperatorId());
        super.updateById(currentAgent);
    }

    @Override
    public void setSystem(AiAgentSetSystemReqVo reqVo) {
        // 获取要设置的智能体信息
        AiAgent currentAgent = super.getById(reqVo.getId());
        if (currentAgent == null) {
            throw new SystemBusinessException("智能体不存在");
        }

        // 验证权限
        checkDataPermission(reqVo.getId(), reqVo.getOrgId());

        Integer orgId = currentAgent.getOrgId(); // 从数据库中获取orgId而不是依赖参数
        Integer type = currentAgent.getType(); // 从数据库中获取type
        Long industryId = currentAgent.getIndustryId(); // 从数据库中获取industryId

        // 将该类型下其他智能体的isSystem设为0
        LambdaQueryWrapper<AiAgent> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AiAgent::getOrgId, orgId)
                .eq(AiAgent::getType, type)
                .eq(AiAgent::getIndustryId, industryId)
                .eq(AiAgent::getIsSystem, 1); // 找到当前系统预设智能体

        List<AiAgent> defaultAgents = super.list(wrapper);

        // 更新其他智能体为非系统预设
        for (AiAgent agent : defaultAgents) {
            if (!agent.getId().equals(reqVo.getId())) { // 不是当前正在设置的智能体
                agent.setIsSystem(0);
                agent.setUpdateBy(reqVo.getOperatorId());
                super.updateById(agent);
            }
        }

        // 设置当前智能体为系统预设
        currentAgent.setIsSystem(1);
        currentAgent.setUpdateBy(reqVo.getOperatorId());
        super.updateById(currentAgent);
    }

    @Override
    public AiAgentDetailVo getByCode(String code) {
        LambdaQueryWrapper<AiAgent> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AiAgent::getCode, code);
        AiAgent aiAgent = super.getOne(wrapper, false);
        if (aiAgent == null) {
            return null;
        }
        return ModelMapperUtil.strictMap(aiAgent, AiAgentDetailVo.class);
    }

    private void checkDataPermission(Long id, Integer orgId) {
        LambdaQueryWrapper<AiAgent> checkWrapper = Wrappers.lambdaQuery();
        checkWrapper.eq(AiAgent::getId, id);
        if (orgId != null) {
            checkWrapper.eq(AiAgent::getOrgId, orgId);
        }
        long count = super.count(checkWrapper);
        if (count == 0) {
            throw new SystemBusinessException("操作权限获取失败！");
        }
    }

}
