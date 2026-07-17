package com.iusofts.agentplus.ailog.interfaces;

import com.iusofts.agentplus.ailog.vo.KnowledgeRetrievalStatItem;
import com.iusofts.agentplus.ailog.vo.LlmCallLogPageResult;
import com.iusofts.agentplus.ailog.vo.LlmCallLogQueryRequest;
import com.iusofts.agentplus.ailog.vo.LlmCallStatItem;

import java.time.LocalDate;
import java.util.List;

/**
 * LLM 日志查询服务接口。
 *
 * @author Ivan
 */
public interface IAiLlmLogQueryService {

    /**
     * 查询 LLM 调用日志列表。
     */
    LlmCallLogPageResult queryLlmCallLogs(LlmCallLogQueryRequest request);

    /**
     * 查询 LLM 调用统计报表（按时间维度）。
     */
    List<LlmCallStatItem> queryLlmCallStatsByDate(LocalDate startDate, LocalDate endDate);

    /**
     * 查询 LLM 调用统计报表（按模型维度）。
     */
    List<LlmCallStatItem> queryLlmCallStatsByModel(LocalDate startDate, LocalDate endDate);

    /**
     * 查询 LLM 调用统计报表（按组织维度）。
     */
    List<LlmCallStatItem> queryLlmCallStatsByOrg(LocalDate startDate, LocalDate endDate);

    /**
     * 查询知识库检索统计报表。
     */
    List<KnowledgeRetrievalStatItem> queryKnowledgeRetrievalStats(LocalDate startDate, LocalDate endDate);
}
