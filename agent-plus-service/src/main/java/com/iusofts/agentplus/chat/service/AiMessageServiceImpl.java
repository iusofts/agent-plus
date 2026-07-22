package com.iusofts.agentplus.chat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iusofts.agentplus.chat.interfaces.IAiMessageService;
import com.iusofts.agentplus.chat.entity.AiMessage;
import com.iusofts.agentplus.chat.mapper.AiMessageMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iusofts.agentplus.chat.vo.AiMessageVo;
import com.iusofts.agentplus.basic.utils.ModelMapperUtil;
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
        messageWrapper.ne(AiMessage::getRole, "system"); // 过滤掉 system 角色
        messageWrapper.orderByAsc(AiMessage::getId); // 按时间顺序排列消息
        List<AiMessage> messageList = super.list(messageWrapper);
        return ModelMapperUtil.strictMapList(messageList, AiMessageVo.class);
    }

    /**
     * 获取指定条数的历史消息，只返回最后 limit 条（过滤掉 system 角色）
     * @param conversationId 会话ID
     * @param limit 最大返回条数
     * @return 历史消息列表，按时间升序排列
     */
    public List<AiMessageVo> getHistoryMessages(Long conversationId, int limit) {
        LambdaQueryWrapper<AiMessage> messageWrapper = Wrappers.lambdaQuery();
        messageWrapper.eq(AiMessage::getConversationId, conversationId);
        messageWrapper.ne(AiMessage::getRole, "system"); // 过滤掉 system 角色
        messageWrapper.orderByDesc(AiMessage::getId); // 按时间倒序，最新的在前
        messageWrapper.last("LIMIT " + limit); // 只取最新的 limit 条
        List<AiMessage> messageList = super.list(messageWrapper);
        // 反转回升序，保持和原有接口一致的顺序
        java.util.Collections.reverse(messageList);
        return ModelMapperUtil.strictMapList(messageList, AiMessageVo.class);
    }

}
