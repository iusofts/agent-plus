package com.iusofts.agentplus.chat.interfaces;

import com.iusofts.agentplus.chat.vo.conversation.AiConversationAddReqVo;
import com.iusofts.agentplus.chat.vo.conversation.AiConversationQueryPageReqVo;
import com.iusofts.agentplus.chat.vo.conversation.AiConversationTestInfoVo;
import com.iusofts.agentplus.chat.vo.conversation.AiConversationVo;
import com.iusofts.agentplus.basic.web.vo.page.PageResult;
import com.iusofts.agentplus.common.vo.IdReqVo;

import java.util.List;

/**
 * <p>
 * ai对话会话 服务类
 * </p>
 *
 * @author Ivan
 * @since 2026-03-31
 */
public interface IAiConversationService {

    Long add(AiConversationAddReqVo reqVo);

    PageResult<AiConversationVo> queryPage(AiConversationQueryPageReqVo reqVo);

    List<AiConversationVo> queryAll(Integer orgId);

    void remove(IdReqVo reqVo);

    void updateTitle(Long id, String title, Integer orgId, Long operatorId);

    AiConversationTestInfoVo getInfo(IdReqVo reqVo);

    AiConversationVo findByBusinessId(Integer businessType, String businessId, Integer orgId);

}
