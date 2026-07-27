package com.iusofts.agentplus.ailog.interfaces;

import com.iusofts.agentplus.aiflow.vo.AiFlowRuntimeTraceReqVo;
import com.iusofts.agentplus.aiflow.vo.AiFlowTraceTreeVo;
import com.iusofts.agentplus.aiflow.vo.AiFlowTraceVo;
import com.iusofts.agentplus.aiflow.vo.AiSpanDetailVo;

import java.util.List;

/**
 * AI Trace 查询服务接口。
 *
 * @author Ivan
 * @since 2026-07-25
 */
public interface IAiTraceQueryService {

    /**
     * 查询流程运行时序火焰图。
     *
     * @param reqVo 请求参数，含 traceId
     * @return 火焰图数据
     */
    AiFlowTraceVo queryTrace(AiFlowRuntimeTraceReqVo reqVo);

    /**
     * 查询流程运行时序树形结构。
     *
     * @param reqVo 请求参数，含 traceId
     * @return 树形结构数据
     */
    List<AiFlowTraceTreeVo> queryTraceTree(AiFlowRuntimeTraceReqVo reqVo);

    /**
     * 根据span主键ID查询span详情。
     *
     * @param id span表主键ID
     * @return span详情，含出入参
     */
    AiSpanDetailVo querySpanDetail(Long id);

}
