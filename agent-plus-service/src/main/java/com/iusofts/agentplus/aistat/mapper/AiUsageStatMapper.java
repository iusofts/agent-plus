package com.iusofts.agentplus.aistat.mapper;

import com.iusofts.agentplus.aistat.vo.AiUsageOverviewVo;
import com.iusofts.agentplus.aistat.vo.AiUsageTrendItem;
import com.iusofts.agentplus.aistat.vo.DocOperationUsageItem;
import com.iusofts.agentplus.aistat.vo.KbRetrievalUsageItem;
import com.iusofts.agentplus.aistat.vo.LlmModelUsageItem;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * AI 用量统计 Mapper。基于三类日志表做 SQL 聚合。
 *
 * @author Ivan
 */
public interface AiUsageStatMapper {

    /** LLM 调用总览(仅填充 llm* 字段)。 */
    AiUsageOverviewVo selectLlmOverview(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate,
                                        @Param("startHour") Integer startHour,
                                        @Param("endHour") Integer endHour,
                                        @Param("orgId") Integer orgId);

    /** 知识库检索总览(仅填充 retrieval* 字段)。 */
    AiUsageOverviewVo selectRetrievalOverview(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate,
                                              @Param("startHour") Integer startHour,
                                              @Param("endHour") Integer endHour,
                                              @Param("orgId") Integer orgId);

    /** 文档处理总览(仅填充 doc* 字段)。 */
    AiUsageOverviewVo selectDocOverview(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate,
                                        @Param("startHour") Integer startHour,
                                        @Param("endHour") Integer endHour,
                                        @Param("orgId") Integer orgId);

    /** LLM 调用趋势(byHour=true 时按小时,否则按天)。 */
    List<AiUsageTrendItem> selectLlmTrend(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate,
                                          @Param("startHour") Integer startHour,
                                          @Param("endHour") Integer endHour,
                                          @Param("orgId") Integer orgId,
                                          @Param("byHour") boolean byHour);

    /** 知识库检索趋势。 */
    List<AiUsageTrendItem> selectRetrievalTrend(@Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate,
                                                @Param("startHour") Integer startHour,
                                                @Param("endHour") Integer endHour,
                                                @Param("orgId") Integer orgId,
                                                @Param("byHour") boolean byHour);

    /** 文档处理趋势。 */
    List<AiUsageTrendItem> selectDocTrend(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate,
                                          @Param("startHour") Integer startHour,
                                          @Param("endHour") Integer endHour,
                                          @Param("orgId") Integer orgId,
                                          @Param("byHour") boolean byHour);

    /** 按模型维度统计 LLM 用量。 */
    List<LlmModelUsageItem> selectStatByModel(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate,
                                              @Param("startHour") Integer startHour,
                                              @Param("endHour") Integer endHour,
                                              @Param("orgId") Integer orgId);

    /** 按知识库维度统计检索用量。 */
    List<KbRetrievalUsageItem> selectStatByKnowledgeBase(@Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate,
                                                         @Param("startHour") Integer startHour,
                                                         @Param("endHour") Integer endHour,
                                                         @Param("orgId") Integer orgId);

    /** 按操作类型维度统计文档处理用量。 */
    List<DocOperationUsageItem> selectStatByDocOperation(@Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate,
                                                         @Param("startHour") Integer startHour,
                                                         @Param("endHour") Integer endHour,
                                                         @Param("orgId") Integer orgId);
}
