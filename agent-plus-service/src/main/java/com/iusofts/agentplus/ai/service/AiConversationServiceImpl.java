package com.iusofts.agentplus.ai.service;

import com.alibaba.dashscope.common.Role;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iusofts.agentplus.ai.interfaces.IAiConversationService;
import com.iusofts.agentplus.ai.entity.AiConversation;
import com.iusofts.agentplus.ai.enums.AiBusinessType;
import com.iusofts.agentplus.ai.mapper.AiConversationMapper;
import com.iusofts.agentplus.ai.vo.conversation.AiConversationAddReqVo;
import com.iusofts.agentplus.ai.vo.conversation.AiConversationQueryPageReqVo;
import com.iusofts.agentplus.ai.vo.conversation.AiConversationTestInfoVo;
import com.iusofts.agentplus.ai.vo.conversation.AiConversationVo;
import com.iusofts.agentplus.ai.vo.service.AiMessageVo;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.basic.page.PageResult;
import com.iusofts.agentplus.basic.utils.ModelMapperUtil;
import com.iusofts.agentplus.common.vo.IdReqVo;
import com.iusofts.agentplus.id.service.IdService;
import com.iusofts.agentplus.id.service.IdService.UidTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * ai对话会话 服务实现类
 * </p>
 *
 * @author Ivan
 * @since 2026-03-31
 */
@Service
public class AiConversationServiceImpl extends ServiceImpl<AiConversationMapper, AiConversation> implements IAiConversationService {

    @Resource
    private IdService idService;
    
    @Resource
    private AiMessageServiceImpl aiMessageService;

    @Override
    public Long add(AiConversationAddReqVo reqVo) {
        AiConversation aiConversation = ModelMapperUtil.strictMap(reqVo, AiConversation.class);
        Integer uid = idService.generateUid(UidTypeEnum.CHAT);
        aiConversation.setId(uid.longValue());
        aiConversation.setCreateBy(reqVo.getOperatorId()); // 设置创建人
        super.save(aiConversation);
        return uid.longValue();
    }

    @Override
    public PageResult<AiConversationVo> queryPage(AiConversationQueryPageReqVo reqVo) {
        PageResult<AiConversationVo> pageResult = new PageResult<>();
        LambdaQueryWrapper<AiConversation> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AiConversation::getBusinessType, AiBusinessType.TEST.getCode());
        if (reqVo.getOrgId() != null) {
            wrapper.eq(AiConversation::getOrgId, reqVo.getOrgId());
        }
        if (reqVo.getAgentId() != null) {
            wrapper.eq(AiConversation::getAgentId, reqVo.getAgentId());
        }
        wrapper.orderByDesc(AiConversation::getId);
        Page<AiConversation> pageParam = new Page<>(reqVo.getCurrentPage(), reqVo.getPageSize());
        IPage<AiConversation> page = super.page(pageParam, wrapper);
        List<AiConversationVo> voList = page.getRecords().stream().map(item -> {
            AiConversationVo vo = ModelMapperUtil.strictMap(item, AiConversationVo.class);
            return vo;
        }).toList();
        pageResult.setDataList(voList);
        pageResult.setTotalCount(page.getTotal());
        return pageResult;
    }

    @Override
    public List<AiConversationVo> queryAll(Integer orgId) {
        LambdaQueryWrapper<AiConversation> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AiConversation::getOrgId, orgId);
        List<AiConversation> list = super.list(wrapper);
        List<AiConversationVo> voList = list.stream().map(item -> {
            AiConversationVo vo = ModelMapperUtil.strictMap(item, AiConversationVo.class);
            return vo;
        }).toList();
        return voList;
    }

    @Override
    public void remove(IdReqVo reqVo) {
        checkDataPermission(reqVo.getId(), reqVo.getOrgId());
        super.removeById(reqVo.getId());
    }

    @Override
    public void updateTitle(Long id, String title, Integer orgId, Long operatorId) {
        checkDataPermission(id, orgId);
        AiConversation aiConversation = new AiConversation();
        aiConversation.setId(id);
        aiConversation.setTitle(title);
        aiConversation.setUpdateBy(operatorId);
        super.updateById(aiConversation);
    }

    @Override
    public AiConversationTestInfoVo getInfo(IdReqVo reqVo) {
        // 首先检查会话是否存在
        AiConversation conversation = super.getById(reqVo.getId());
        if (conversation == null) {
            throw new SystemBusinessException("会话不存在");
        }

        if (!conversation.getOrgId().equals(reqVo.getOrgId())) {
            throw new SystemBusinessException("操作权限获取失败！");
        }

        // 将实体转换为VO
        AiConversationTestInfoVo infoVo = ModelMapperUtil.strictMap(conversation, AiConversationTestInfoVo.class);

        // 查询相关的消息记录
        List<AiMessageVo> messageVoList = aiMessageService.getList(reqVo.getId());
        // 排除系统预设内容
        messageVoList = messageVoList.stream()
            .filter(item-> !item.getRole().equals(Role.SYSTEM.getValue())).map(message -> ModelMapperUtil.strictMap(message, AiMessageVo.class))
            .toList();
        infoVo.setMessages(messageVoList);

        // 计算使用量统计 - 聚合所有消息的token使用量
        AiConversationTestInfoVo.Usage usage = new AiConversationTestInfoVo.Usage();
        int totalInputTokens = messageVoList.stream()
            .mapToInt(msg -> msg.getInputTokens() != null ? msg.getInputTokens() : 0)
            .sum();
        int totalOutputTokens = messageVoList.stream()
            .mapToInt(msg -> msg.getOutputTokens() != null ? msg.getOutputTokens() : 0)
            .sum();
        int totalTokens = messageVoList.stream()
            .mapToInt(msg -> msg.getTotalTokens() != null ? msg.getTotalTokens() : 0)
            .sum();

        usage.setInputTokens(totalInputTokens);
        usage.setOutputTokens(totalOutputTokens);
        usage.setTotalTokens(totalTokens);
        infoVo.setUsage(usage);

        return infoVo;
    }

    @Override
    public AiConversationVo findByBusinessId(Integer businessType, String businessId, Integer orgId) {
        LambdaQueryWrapper<AiConversation> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AiConversation::getBusinessType, businessType);
        wrapper.eq(AiConversation::getBusinessId, businessId);
        wrapper.eq(AiConversation::getOrgId, orgId);
        wrapper.orderByDesc(AiConversation::getId);
        AiConversation conversation = super.getOne(wrapper, false);
        if (conversation != null) {
            return ModelMapperUtil.strictMap(conversation, AiConversationVo.class);
        }
        return null;
    }

    private void checkDataPermission(Long id, Integer orgId) {
        LambdaQueryWrapper<AiConversation> checkWrapper = Wrappers.lambdaQuery();
        checkWrapper.eq(AiConversation::getId, id);
        checkWrapper.eq(AiConversation::getOrgId, orgId);
        long count = super.count(checkWrapper);
        if (count == 0) {
            throw new SystemBusinessException("操作权限获取失败！");
        }
    }

}
