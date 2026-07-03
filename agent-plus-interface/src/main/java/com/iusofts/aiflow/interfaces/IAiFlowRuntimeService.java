package com.iusofts.aiflow.interfaces;

import com.iusofts.aiflow.vo.*;
import com.iusofts.basic.page.PageResult;
import com.iusofts.common.vo.IdReqVo;

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

}
