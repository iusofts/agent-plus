package com.iusofts.agentplus.aiflow.interfaces;

import com.iusofts.agentplus.aiflow.vo.*;
import com.iusofts.agentplus.basic.web.vo.page.PageResult;
import com.iusofts.agentplus.common.vo.IdReqVo;

import java.util.List;

/**
 * <p>
 * AI流程 服务类
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
public interface IAiFlowService {

    void add(AiFlowAddReqVo reqVo);

    void edit(AiFlowEditReqVo reqVo);

    PageResult<AiFlowVo> queryPage(AiFlowQueryPageReqVo reqVo);

    List<AiFlowVo> queryAll();

    void remove(IdReqVo reqVo);

    AiFlowDetailVo getById(IdReqVo reqVo);

    void setStatus(AiFlowSetStatusReqVo reqVo);

}
