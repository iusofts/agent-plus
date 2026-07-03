package com.iusofts.ai.Interfaces;

import com.iusofts.ai.vo.conversation.*;
import com.iusofts.basic.page.PageResult;
import com.iusofts.common.vo.IdReqVo;

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
