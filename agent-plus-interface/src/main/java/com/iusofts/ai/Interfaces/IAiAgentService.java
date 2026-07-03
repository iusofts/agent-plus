package com.iusofts.ai.Interfaces;

import com.iusofts.ai.vo.*;
import com.iusofts.basic.page.PageResult;
import com.iusofts.common.vo.IdReqVo;

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

    /**
     * 设置智能体为默认智能体
     */
    void setDefault(AiAgentSetDefaultReqVo reqVo);

    /**
     * 设置智能体为系统预制智能体
     */
    void setSystem(AiAgentSetSystemReqVo reqVo);

    AiAgentDetailVo getByCode(String code);

}
