package com.iusofts.agentplus.aistat.service;

import com.iusofts.agentplus.aistat.interfaces.IAiUsageStatService;
import com.iusofts.agentplus.aistat.mapper.AiUsageStatMapper;
import com.iusofts.agentplus.aistat.vo.AiUsageOverviewVo;
import com.iusofts.agentplus.aistat.vo.AiUsageStatQueryRequest;
import com.iusofts.agentplus.aistat.vo.AiUsageTrendItem;
import com.iusofts.agentplus.aistat.vo.DocOperationUsageItem;
import com.iusofts.agentplus.aistat.vo.KbRetrievalUsageItem;
import com.iusofts.agentplus.aistat.vo.LlmModelUsageItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 用量统计服务实现。基于三类日志表做 SQL 聚合。
 *
 * @author Ivan
 */
@Slf4j
@Service
public class AiUsageStatServiceImpl implements IAiUsageStatService {

    @Resource
    private AiUsageStatMapper usageStatMapper;

    @Override
    public AiUsageOverviewVo queryOverview(AiUsageStatQueryRequest request) {
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        Integer startHour = request.getStartHour();
        Integer endHour = request.getEndHour();
        Integer orgId = request.getOrgId();

        AiUsageOverviewVo llm = usageStatMapper.selectLlmOverview(startDate, endDate, startHour, endHour, orgId);
        AiUsageOverviewVo retrieval = usageStatMapper.selectRetrievalOverview(startDate, endDate, startHour, endHour, orgId);
        AiUsageOverviewVo doc = usageStatMapper.selectDocOverview(startDate, endDate, startHour, endHour, orgId);

        AiUsageOverviewVo result = AiUsageOverviewVo.builder()
            // LLM
            .llmTotalCalls(nvl(llm.getLlmTotalCalls()))
            .llmSuccessCalls(nvl(llm.getLlmSuccessCalls()))
            .llmFailCalls(nvl(llm.getLlmFailCalls()))
            .llmInputTokens(nvl(llm.getLlmInputTokens()))
            .llmOutputTokens(nvl(llm.getLlmOutputTokens()))
            .llmTotalTokens(nvl(llm.getLlmTotalTokens()))
            .llmAvgDurationMs(nvl(llm.getLlmAvgDurationMs()))
            // 检索
            .retrievalTotalCalls(nvl(retrieval.getRetrievalTotalCalls()))
            .retrievalSuccessCalls(nvl(retrieval.getRetrievalSuccessCalls()))
            .retrievalTotalChunks(nvl(retrieval.getRetrievalTotalChunks()))
            .retrievalEmbeddingTokens(nvl(retrieval.getRetrievalEmbeddingTokens()))
            .retrievalAvgDurationMs(nvl(retrieval.getRetrievalAvgDurationMs()))
            // 文档处理
            .docTotalOps(nvl(doc.getDocTotalOps()))
            .docSuccessOps(nvl(doc.getDocSuccessOps()))
            .docTotalChunks(nvl(doc.getDocTotalChunks()))
            .docEmbeddingTokens(nvl(doc.getDocEmbeddingTokens()))
            .build();

        // 成功率
        long total = result.getLlmTotalCalls();
        if (total > 0) {
            result.setLlmSuccessRate(BigDecimal.valueOf(result.getLlmSuccessCalls() * 100.0 / total)
                .setScale(2, RoundingMode.HALF_UP));
        } else {
            result.setLlmSuccessRate(BigDecimal.ZERO);
        }
        return result;
    }

    @Override
    public List<AiUsageTrendItem> queryTrend(AiUsageStatQueryRequest request) {
        boolean byHour = "HOUR".equalsIgnoreCase(request.getGranularity());
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        Integer startHour = request.getStartHour();
        Integer endHour = request.getEndHour();
        Integer orgId = request.getOrgId();

        List<AiUsageTrendItem> llmTrend = usageStatMapper.selectLlmTrend(startDate, endDate, startHour, endHour, orgId, byHour);
        List<AiUsageTrendItem> retrievalTrend = usageStatMapper.selectRetrievalTrend(startDate, endDate, startHour, endHour, orgId, byHour);
        List<AiUsageTrendItem> docTrend = usageStatMapper.selectDocTrend(startDate, endDate, startHour, endHour, orgId, byHour);

        // 按时间桶合并三类趋势,保持时间有序
        Map<String, AiUsageTrendItem> merged = new LinkedHashMap<>();
        for (AiUsageTrendItem item : llmTrend) {
            AiUsageTrendItem bucket = getBucket(merged, item, byHour);
            bucket.setLlmCalls(nvl(item.getLlmCalls()));
            bucket.setLlmTotalTokens(nvl(item.getLlmTotalTokens()));
            bucket.setLlmAvgDurationMs(nvl(item.getLlmAvgDurationMs()));
        }
        for (AiUsageTrendItem item : retrievalTrend) {
            AiUsageTrendItem bucket = getBucket(merged, item, byHour);
            bucket.setRetrievals(nvl(item.getRetrievals()));
            bucket.setRetrievalEmbeddingTokens(nvl(item.getRetrievalEmbeddingTokens()));
        }
        for (AiUsageTrendItem item : docTrend) {
            AiUsageTrendItem bucket = getBucket(merged, item, byHour);
            bucket.setDocOps(nvl(item.getDocOps()));
        }

        List<AiUsageTrendItem> result = new ArrayList<>(merged.values());
        result.sort((a, b) -> {
            int c = a.getDate().compareTo(b.getDate());
            if (c != 0) {
                return c;
            }
            int ha = a.getHour() != null ? a.getHour() : 0;
            int hb = b.getHour() != null ? b.getHour() : 0;
            return Integer.compare(ha, hb);
        });
        return result;
    }

    @Override
    public List<LlmModelUsageItem> queryStatByModel(AiUsageStatQueryRequest request) {
        return usageStatMapper.selectStatByModel(request.getStartDate(), request.getEndDate(),
            request.getStartHour(), request.getEndHour(), request.getOrgId());
    }

    @Override
    public List<KbRetrievalUsageItem> queryStatByKnowledgeBase(AiUsageStatQueryRequest request) {
        return usageStatMapper.selectStatByKnowledgeBase(request.getStartDate(), request.getEndDate(),
            request.getStartHour(), request.getEndHour(), request.getOrgId());
    }

    @Override
    public List<DocOperationUsageItem> queryStatByDocOperation(AiUsageStatQueryRequest request) {
        return usageStatMapper.selectStatByDocOperation(request.getStartDate(), request.getEndDate(),
            request.getStartHour(), request.getEndHour(), request.getOrgId());
    }

    /** 取/建时间桶,并生成时间标签。 */
    private AiUsageTrendItem getBucket(Map<String, AiUsageTrendItem> merged, AiUsageTrendItem source, boolean byHour) {
        String key = source.getDate().toString() + (byHour ? "#" + source.getHour() : "");
        return merged.computeIfAbsent(key, k -> {
            String label = byHour
                ? source.getDate().toString() + " " + String.format("%02d", source.getHour())
                : source.getDate().toString();
            return AiUsageTrendItem.builder()
                .timeLabel(label)
                .date(source.getDate())
                .hour(byHour ? source.getHour() : null)
                .llmCalls(0L)
                .llmTotalTokens(0L)
                .retrievals(0L)
                .retrievalEmbeddingTokens(0L)
                .docOps(0L)
                .build();
        });
    }

    private long nvl(Long v) {
        return v != null ? v : 0L;
    }

    private BigDecimal nvl(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
