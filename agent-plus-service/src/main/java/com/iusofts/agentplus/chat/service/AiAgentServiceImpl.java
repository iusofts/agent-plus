package com.iusofts.agentplus.chat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iusofts.agentplus.chat.interfaces.IAiAgentService;
import com.iusofts.agentplus.chat.entity.AiAgent;
import com.iusofts.agentplus.chat.mapper.AiAgentMapper;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.basic.web.vo.page.PageResult;
import com.iusofts.agentplus.basic.utils.ModelMapperUtil;
import com.iusofts.agentplus.basic.utils.StringUtils;
import com.iusofts.agentplus.basic.utils.JsonUtils;
import com.iusofts.agentplus.chat.vo.agent.*;
import com.iusofts.agentplus.common.vo.IdReqVo;
import com.iusofts.agentplus.id.service.IdService;
import com.iusofts.agentplus.id.service.IdService.UidTypeEnum;
import jakarta.annotation.Resource;
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

    @Override
    public void add(AiAgentAddReqVo reqVo) {
        AiAgent aiAgent = ModelMapperUtil.strictMap(reqVo, AiAgent.class);
        Integer uid = idService.generateUid(UidTypeEnum.CHAT);
        aiAgent.setId(uid.longValue());
        aiAgent.setCreateBy(reqVo.getOperatorId());

        // JSON 序列化
        aiAgent.setWorkflowIds(serializeIdList(reqVo.getWorkflowIds()));
        aiAgent.setOpeningQuestions(serializeStringList(reqVo.getOpeningQuestions()));
        aiAgent.setKnowledgeBaseIds(serializeIdList(reqVo.getKnowledgeBaseIds()));

        super.save(aiAgent);
    }

    @Override
    public void edit(AiAgentEditReqVo reqVo) {
        checkDataPermission(reqVo.getId(), reqVo.getOrgId());
        AiAgent aiAgent = ModelMapperUtil.strictMap(reqVo, AiAgent.class);
        aiAgent.setUpdateBy(reqVo.getOperatorId());

        // JSON 序列化
        aiAgent.setWorkflowIds(serializeIdList(reqVo.getWorkflowIds()));
        aiAgent.setOpeningQuestions(serializeStringList(reqVo.getOpeningQuestions()));
        aiAgent.setKnowledgeBaseIds(serializeIdList(reqVo.getKnowledgeBaseIds()));

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
        wrapper.orderByDesc(AiAgent::getId);
        Page<AiAgent> pageParam = new Page<>(reqVo.getCurrentPage(), reqVo.getPageSize());
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

        // JSON 反序列化
        vo.setWorkflowIds(deserializeIdList(aiAgent.getWorkflowIds()));
        vo.setOpeningQuestions(deserializeStringList(aiAgent.getOpeningQuestions()));
        vo.setKnowledgeBaseIds(deserializeIdList(aiAgent.getKnowledgeBaseIds()));

        return vo;
    }

    private String serializeIdList(List<Long> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return JsonUtils.obj2json(list);
    }

    private List<Long> deserializeIdList(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        return JsonUtils.json2list(json, Long.class);
    }

    private String serializeStringList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return JsonUtils.obj2json(list);
    }

    private List<String> deserializeStringList(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        return JsonUtils.json2list(json, String.class);
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
