package com.iusofts.agentplus.web.ailog.controller;

import com.iusofts.agentplus.ailog.interfaces.IAiTraceQueryService;
import com.iusofts.agentplus.aiflow.vo.AiFlowRuntimeTraceReqVo;
import com.iusofts.agentplus.aiflow.vo.AiFlowTraceTreeVo;
import com.iusofts.agentplus.aiflow.vo.AiFlowTraceVo;
import com.iusofts.agentplus.aiflow.vo.AiSpanDetailVo;
import com.iusofts.agentplus.basic.web.annotation.OperationLogExclude;
import com.iusofts.agentplus.common.vo.IdReqVo;
import com.iusofts.agentplus.web.common.controller.BApiController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.iusofts.agentplus.basic.enums.OperationLogExcludeTypeEnums.RES;

/**
 * AI Trace 前端控制器。
 *
 * @author Ivan
 * @since 2026-07-25
 */
@Tag(name = "AI Trace 链路追踪")
@RestController
@RequestMapping("/bapi/ai/trace")
public class AiTraceController extends BApiController {

    @Resource
    private IAiTraceQueryService aiTraceQueryService;

    @Operation(description = "查询链路时序火焰图")
    @OperationLogExclude(type = RES)
    @PostMapping("/queryTrace")
    public AiFlowTraceVo queryTrace(@RequestBody AiFlowRuntimeTraceReqVo reqVo) {
        return aiTraceQueryService.queryTrace(reqVo);
    }

    @Operation(description = "查询链路时序树形结构")
    @OperationLogExclude(type = RES)
    @PostMapping("/queryTraceTree")
    public List<AiFlowTraceTreeVo> queryTraceTree(@RequestBody AiFlowRuntimeTraceReqVo reqVo) {
        return aiTraceQueryService.queryTraceTree(reqVo);
    }

    @Operation(description = "根据span主键ID查询span详情")
    @OperationLogExclude(type = RES)
    @PostMapping("/querySpanDetail")
    public AiSpanDetailVo querySpanDetail(@RequestBody IdReqVo reqVo) {
        return aiTraceQueryService.querySpanDetail(reqVo.getId());
    }

}
