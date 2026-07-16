package com.iusofts.agentplus.aiflow.interfaces;

import com.iusofts.agentplus.aiflow.vo.AiFlowTrialRunFlowReqVo;
import com.iusofts.agentplus.aiflow.vo.AiFlowTrialRunNodeReqVo;
import com.iusofts.agentplus.aiflow.vo.AiFlowTrialRunResultVo;

/**
 * <p>
 * 流程试运行 服务类
 * </p>
 *
 * @author Ivan
 * @since 2026-07-16
 */
public interface IAiFlowTrialService {

    /** 试运行整个流程。 */
    AiFlowTrialRunResultVo runFlow(AiFlowTrialRunFlowReqVo reqVo);

    /** 试运行单个节点。 */
    AiFlowTrialRunResultVo runNode(AiFlowTrialRunNodeReqVo reqVo);

}
