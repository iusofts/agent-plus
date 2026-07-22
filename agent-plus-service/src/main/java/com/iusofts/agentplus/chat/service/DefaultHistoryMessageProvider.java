package com.iusofts.agentplus.chat.service;

import com.iusofts.agentplus.chat.entity.AiAgent;
import com.iusofts.agentplus.chat.mapper.AiAgentMapper;
import com.iusofts.agentplus.chat.vo.AiMessageVo;
import com.iusofts.agentplus.engine.history.HistoryMessageProvider;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 默认历史消息提供者实现。
 * 委托给 AiMessageServiceImpl 获取会话历史。
 *
 * @author Ivan Shen
 */
@Component
public class DefaultHistoryMessageProvider implements HistoryMessageProvider {

    @Resource
    private AiMessageServiceImpl aiMessageService;
    @Resource
    private AiAgentMapper aiAgentMapper;

    @Override
    public List<AiMessageVo> getHistoryMessages(Long conversationId) {
        return aiMessageService.getList(conversationId);
    }

    @Override
    public List<AiMessageVo> getHistoryMessages(Long conversationId, int limit) {
        return aiMessageService.getHistoryMessages(conversationId, limit);
    }

    @Override
    public int clampRoundsByAgentLimit(int nodeRounds, Long agentId) {
        if (agentId == null) {
            return nodeRounds;
        }
        AiAgent agent = aiAgentMapper.selectById(agentId);
        if (agent == null) {
            return nodeRounds;
        }
        Integer agentMaxRounds = agent.getContextRounds();
        if (agentMaxRounds == null) {
            return nodeRounds;
        }
        // 节点轮数不能超过智能体设置的上限
        return Math.min(nodeRounds, agentMaxRounds);
    }
}
