package com.iusofts.agentplus.engine.history;

import com.iusofts.agentplus.chat.vo.AiMessageVo;

import java.util.List;

/**
 * 会话历史消息提供者接口。
 * 由业务层实现，提供从数据库查询会话历史消息的能力。
 *
 * @author Ivan Shen
 */
public interface HistoryMessageProvider {

    /**
     * 获取指定会话的历史消息。
     *
     * @param conversationId 会话ID
     * @return 历史消息列表，按时间升序排列
     */
    List<AiMessageVo> getHistoryMessages(Long conversationId);

    /**
     * 根据智能体配置的最大上下文轮数上限校正节点请求的轮数。
     * 节点配置的轮数不能超过智能体设置的上限。
     *
     * @param nodeRounds 节点请求的轮数
     * @param agentId 智能体ID
     * @return 校正后的轮数（不超过智能体上限）
     */
    default int clampRoundsByAgentLimit(int nodeRounds, Long agentId) {
        return nodeRounds;
    }
}
