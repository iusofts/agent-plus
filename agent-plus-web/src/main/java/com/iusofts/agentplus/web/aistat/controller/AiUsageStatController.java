package com.iusofts.agentplus.web.aistat.controller;

import com.iusofts.agentplus.aistat.interfaces.IAiUsageStatService;
import com.iusofts.agentplus.aistat.vo.AiUsageOverviewVo;
import com.iusofts.agentplus.aistat.vo.AiUsageStatQueryRequest;
import com.iusofts.agentplus.aistat.vo.AiUsageTrendItem;
import com.iusofts.agentplus.aistat.vo.DocOperationUsageItem;
import com.iusofts.agentplus.aistat.vo.KbRetrievalUsageItem;
import com.iusofts.agentplus.aistat.vo.LlmModelUsageItem;
import com.iusofts.agentplus.basic.web.annotation.OperationLogExclude;
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
import static com.iusofts.agentplus.common.constants.SysConstant.SYSCODE;

/**
 * <p>
 * AI 用量统计 前端控制器
 * </p>
 *
 * 基于 AiLlmCallLog、AiKnowledgeRetrievalLog、AiKnowledgeDocLog 三类日志,
 * 支持按起始日期过滤、按天/小时聚合。
 *
 * @author Ivan
 * @since 2026-07-15
 */
@Tag(name = "AI用量统计")
@RestController
@RequestMapping("/bapi/ai/usage")
public class AiUsageStatController extends BApiController {

    @Resource
    private IAiUsageStatService aiUsageStatService;

    @Operation(description = "用量总览")
    @OperationLogExclude(type = RES)
    @PostMapping("/overview")
    public AiUsageOverviewVo overview(@RequestBody AiUsageStatQueryRequest request) {
        request.setOrgId(SYSCODE);
        return aiUsageStatService.queryOverview(request);
    }

    @Operation(description = "用量趋势(按天/小时)")
    @OperationLogExclude(type = RES)
    @PostMapping("/trend")
    public List<AiUsageTrendItem> trend(@RequestBody AiUsageStatQueryRequest request) {
        request.setOrgId(SYSCODE);
        return aiUsageStatService.queryTrend(request);
    }

    @Operation(description = "按模型维度统计 LLM 用量")
    @OperationLogExclude(type = RES)
    @PostMapping("/statByModel")
    public List<LlmModelUsageItem> statByModel(@RequestBody AiUsageStatQueryRequest request) {
        request.setOrgId(SYSCODE);
        return aiUsageStatService.queryStatByModel(request);
    }

    @Operation(description = "按知识库维度统计检索用量")
    @OperationLogExclude(type = RES)
    @PostMapping("/statByKnowledgeBase")
    public List<KbRetrievalUsageItem> statByKnowledgeBase(@RequestBody AiUsageStatQueryRequest request) {
        request.setOrgId(SYSCODE);
        return aiUsageStatService.queryStatByKnowledgeBase(request);
    }

    @Operation(description = "按操作类型维度统计文档处理用量")
    @OperationLogExclude(type = RES)
    @PostMapping("/statByDocOperation")
    public List<DocOperationUsageItem> statByDocOperation(@RequestBody AiUsageStatQueryRequest request) {
        request.setOrgId(SYSCODE);
        return aiUsageStatService.queryStatByDocOperation(request);
    }
}
