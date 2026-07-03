package com.iusofts.aiflow.interfaces;

import com.iusofts.aiflow.vo.*;
import com.iusofts.common.vo.IdReqVo;

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
