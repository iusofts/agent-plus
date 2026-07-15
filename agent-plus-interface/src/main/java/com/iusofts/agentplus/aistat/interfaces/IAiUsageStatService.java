package com.iusofts.agentplus.aistat.interfaces;

import com.iusofts.agentplus.aistat.vo.AiUsageOverviewVo;
import com.iusofts.agentplus.aistat.vo.AiUsageStatQueryRequest;
import com.iusofts.agentplus.aistat.vo.AiUsageTrendItem;
import com.iusofts.agentplus.aistat.vo.DocOperationUsageItem;
import com.iusofts.agentplus.aistat.vo.KbRetrievalUsageItem;
import com.iusofts.agentplus.aistat.vo.LlmModelUsageItem;

import java.util.List;

/**
 * AI 用量统计服务接口。
 *
 * <p>基于 AiLlmCallLog、AiKnowledgeRetrievalLog、AiKnowledgeDocLog 三类日志做用量统计,
 * 支持按起始日期过滤、按天/小时聚合。
 *
 * @author Ivan
 */
public interface IAiUsageStatService {

    /**
     * 查询用量总览(三类日志汇总)。
     */
    AiUsageOverviewVo queryOverview(AiUsageStatQueryRequest request);

    /**
     * 查询用量趋势(按天或按小时的时间序列)。
     */
    List<AiUsageTrendItem> queryTrend(AiUsageStatQueryRequest request);

    /**
     * 按模型维度统计 LLM 用量。
     */
    List<LlmModelUsageItem> queryStatByModel(AiUsageStatQueryRequest request);

    /**
     * 按知识库维度统计检索用量。
     */
    List<KbRetrievalUsageItem> queryStatByKnowledgeBase(AiUsageStatQueryRequest request);

    /**
     * 按操作类型维度统计文档处理用量。
     */
    List<DocOperationUsageItem> queryStatByDocOperation(AiUsageStatQueryRequest request);
}
