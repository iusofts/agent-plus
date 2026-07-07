package com.iusofts.agentplus.aiflow.interfaces;

import com.iusofts.agentplus.aiflow.vo.*;
import com.iusofts.agentplus.aiflow.vo.workflow.config.Knowledge;
import com.iusofts.agentplus.aiflow.vo.workflow.config.Model;
import com.iusofts.agentplus.basic.page.PageResult;
import com.iusofts.agentplus.common.vo.IdReqVo;

import java.util.List;

/**
 * <p>
 * AI流程版本 服务类
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
public interface IAiFlowVersionService {

    void saveWorkflow(AiFlowVersionSaveReqVo reqVo);

    void updateConfig(AiFlowVersionConfigUpdateReqVo reqVo);

    PageResult<AiFlowVersionVo> queryPage(AiFlowVersionQueryPageReqVo reqVo);

    List<AiFlowVersionVo> queryByFlowId(Long flowId);

    void remove(IdReqVo reqVo);

    AiFlowVersionDetailVo getById(IdReqVo reqVo);

    void publish(AiFlowVersionPublishReqVo reqVo);

    AiFlowVersionDetailVo getWorkflowEditDetailByFlowId(Long flowId);

    List<Model> queryModelList();

    List<Knowledge> queryKnowledgeList();

}
