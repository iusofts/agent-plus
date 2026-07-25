package com.iusofts.agentplus.aiflow.interfaces;

import com.iusofts.agentplus.aiflow.vo.*;
import com.iusofts.agentplus.basic.web.vo.page.PageResult;
import com.iusofts.agentplus.common.vo.IdReqVo;

import java.util.List;

/**
 * <p>
 * 流程运行实例 服务类
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
public interface IAiFlowRuntimeService {

    void add(AiFlowRuntimeAddReqVo reqVo);

    PageResult<AiFlowRuntimeVo> queryPage(AiFlowRuntimeQueryPageReqVo reqVo);

    List<AiFlowRuntimeVo> queryByFlowId(Long flowId);

    void remove(IdReqVo reqVo);

    AiFlowRuntimeDetailVo getById(IdReqVo reqVo);

    void terminate(AiFlowRuntimeTerminateReqVo reqVo);

    List<AiFlowRuntimeTraceListVo> queryTraceList(AiFlowRuntimeTraceListReqVo reqVo);

}
