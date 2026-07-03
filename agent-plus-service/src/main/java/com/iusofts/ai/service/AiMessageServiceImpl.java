package com.iusofts.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iusofts.ai.Interfaces.IAiMessageService;
import com.iusofts.ai.entity.AiMessage;
import com.iusofts.ai.mapper.AiMessageMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iusofts.ai.vo.service.AiMessageVo;
import com.iusofts.basic.utils.ModelMapperUtil;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * ai对话消息 服务实现类
 * </p>
 *
 * @author Ivan
 * @since 2026-03-31
 */
@Service
public class AiMessageServiceImpl extends ServiceImpl<AiMessageMapper, AiMessage> implements IAiMessageService {

    @Override
    public List<AiMessageVo> getList(Long conversationId) {
        LambdaQueryWrapper<AiMessage> messageWrapper = Wrappers.lambdaQuery();
        messageWrapper.eq(AiMessage::getConversationId, conversationId);
        messageWrapper.orderByAsc(AiMessage::getId); // 按时间顺序排列消息
        List<AiMessage> messageList = super.list(messageWrapper);
        return ModelMapperUtil.strictMapList(messageList, AiMessageVo.class);
    }
    
}
