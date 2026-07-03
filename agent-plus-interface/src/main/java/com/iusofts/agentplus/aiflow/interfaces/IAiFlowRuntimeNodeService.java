package com.iusofts.agentplus.aiflow.interfaces;

import com.iusofts.agentplus.aiflow.vo.AiFlowRuntimeNodeDetailVo;
import com.iusofts.agentplus.aiflow.vo.AiFlowRuntimeNodeQueryReqVo;
import com.iusofts.agentplus.aiflow.vo.AiFlowRuntimeNodeVo;
import com.iusofts.agentplus.common.vo.IdReqVo;

import java.util.List;

/**
 * <p>
 * 运行节点明细 服务类
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
public interface IAiFlowRuntimeNodeService {

    List<AiFlowRuntimeNodeVo> queryByRuntimeId(AiFlowRuntimeNodeQueryReqVo reqVo);

    AiFlowRuntimeNodeDetailVo getById(IdReqVo reqVo);

}
