package com.iusofts.agentplus.chat.interfaces;

import com.iusofts.agentplus.chat.vo.AiMessageVo;

import java.util.List;

/**
 * <p>
 * ai对话消息 服务类
 * </p>
 *
 * @author Ivan
 * @since 2026-03-31
 */
public interface IAiMessageService {

    List<AiMessageVo> getList(Long conversationId);
    
}
