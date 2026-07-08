package com.iusofts.agentplus.chat.interfaces;

import com.iusofts.agentplus.basic.web.vo.page.PageResult;
import com.iusofts.agentplus.chat.vo.agent.*;
import com.iusofts.agentplus.common.vo.IdReqVo;

import java.util.List;

/**
 * <p>
 * ai智能体 服务类
 * </p>
 *
 * @author Ivan
 * @since 2026-03-31
 */
public interface IAiAgentService {

    void add(AiAgentAddReqVo reqVo);

    void edit(AiAgentEditReqVo reqVo);

    PageResult<AiAgentVo> queryPage(AiAgentQueryPageReqVo reqVo);

    List<AiAgentVo> queryAll(Integer orgId);

    void remove(IdReqVo reqVo);

    AiAgentDetailVo getById(IdReqVo reqVo);

}
