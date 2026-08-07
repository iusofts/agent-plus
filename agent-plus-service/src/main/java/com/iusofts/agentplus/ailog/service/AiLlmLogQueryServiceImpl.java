package com.iusofts.agentplus.ailog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iusofts.agentplus.ailog.vo.*;
import com.iusofts.agentplus.ailog.entity.AiLlmCallLog;
import com.iusofts.agentplus.ailog.entity.AiKnowledgeRetrievalLog;
import com.iusofts.agentplus.ailog.interfaces.IAiLlmLogQueryService;
import com.iusofts.agentplus.ailog.mapper.AiLlmCallLogMapper;
import com.iusofts.agentplus.ailog.mapper.AiKnowledgeRetrievalLogMapper;
import com.iusofts.agentplus.trace.constants.CallSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * LLM 日志查询服务实现。
 *
 * @author Ivan
 */
@Slf4j
@Service
public class AiLlmLogQueryServiceImpl implements IAiLlmLogQueryService {

    @Resource
    private AiLlmCallLogMapper llmCallLogMapper;

    @Resource
    private AiKnowledgeRetrievalLogMapper knowledgeRetrievalLogMapper;

    @Override
    public LlmCallLogPageResult queryLlmCallLogs(LlmCallLogQueryRequest request) {
        LambdaQueryWrapper<AiLlmCallLog> wrapper = Wrappers.lambdaQuery();

        if (request.getTraceId() != null) {
            wrapper.like(AiLlmCallLog::getTraceId, request.getTraceId());
        }
        if (request.getCallSource() != null) {
            // 入参经枚举校验后再拼条件，未匹配视为脏数据并忽略
            CallSource callSource = CallSource.fromCode(request.getCallSource());
            if (callSource != null) {
                wrapper.eq(AiLlmCallLog::getCallSource, callSource.getCode());
            }
        }
        if (request.getModelId() != null) {
            wrapper.eq(AiLlmCallLog::getModelId, request.getModelId());
        }
        if (request.getAgentId() != null) {
            wrapper.eq(AiLlmCallLog::getSourceId, request.getAgentId());
        }
        if (request.getStartDate() != null) {
            wrapper.ge(AiLlmCallLog::getStartTime, request.getStartDate().atStartOfDay());
        }
        if (request.getEndDate() != null) {
            wrapper.le(AiLlmCallLog::getStartTime, request.getEndDate().atTime(LocalTime.MAX));
        }

        wrapper.orderByDesc(AiLlmCallLog::getStartTime);

        Page<AiLlmCallLog> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<AiLlmCallLog> resultPage = llmCallLogMapper.selectPage(page, wrapper);

        List<LlmCallLogItem> items = resultPage.getRecords().stream()
            .map(this::toLlmCallLogItem)
            .collect(Collectors.toList());

        return LlmCallLogPageResult.builder()
            .total(resultPage.getTotal())
            .items(items)
            .build();
    }

    @Override
    public List<LlmCallStatItem> queryLlmCallStatsByDate(LocalDate startDate, LocalDate endDate) {
        List<AiLlmCallLog> logs = queryLlmCallLogsInRange(startDate, endDate);

        Map<LocalDate, List<AiLlmCallLog>> groupedByDate = logs.stream()
            .collect(Collectors.groupingBy(log -> log.getStartTime().toLocalDate()));

        List<LlmCallStatItem> result = new ArrayList<>();
        for (Map.Entry<LocalDate, List<AiLlmCallLog>> entry : groupedByDate.entrySet()) {
            result.add(buildStatItem(entry.getKey().toString(), entry.getKey(), null, null, entry.getValue()));
        }

        result.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        return result;
    }

    @Override
    public List<LlmCallStatItem> queryLlmCallStatsByModel(LocalDate startDate, LocalDate endDate) {
        List<AiLlmCallLog> logs = queryLlmCallLogsInRange(startDate, endDate);

        Map<Long, List<AiLlmCallLog>> groupedByModel = logs.stream()
            .filter(log -> log.getModelId() != null)
            .collect(Collectors.groupingBy(AiLlmCallLog::getModelId));

        List<LlmCallStatItem> result = new ArrayList<>();
        for (Map.Entry<Long, List<AiLlmCallLog>> entry : groupedByModel.entrySet()) {
            List<AiLlmCallLog> modelLogs = entry.getValue();
            String modelName = modelLogs.get(0).getModelName();
            result.add(buildStatItem(modelName, null, entry.getKey(), null, modelLogs));
        }

        result.sort((a, b) -> b.getTotalCalls().compareTo(a.getTotalCalls()));
        return result;
    }

    @Override
    public List<LlmCallStatItem> queryLlmCallStatsByOrg(LocalDate startDate, LocalDate endDate) {
        List<AiLlmCallLog> logs = queryLlmCallLogsInRange(startDate, endDate);

        Map<Integer, List<AiLlmCallLog>> groupedByOrg = logs.stream()
            .filter(log -> log.getOrgId() != null)
            .collect(Collectors.groupingBy(AiLlmCallLog::getOrgId));

        List<LlmCallStatItem> result = new ArrayList<>();
        for (Map.Entry<Integer, List<AiLlmCallLog>> entry : groupedByOrg.entrySet()) {
            result.add(buildStatItem("Org-" + entry.getKey(), null, null, entry.getKey(), entry.getValue()));
        }

        result.sort((a, b) -> b.getTotalCalls().compareTo(a.getTotalCalls()));
        return result;
    }

    @Override
    public List<KnowledgeRetrievalStatItem> queryKnowledgeRetrievalStats(LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<AiKnowledgeRetrievalLog> wrapper = Wrappers.lambdaQuery();
        if (startDate != null) {
            wrapper.ge(AiKnowledgeRetrievalLog::getStartTime, startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.le(AiKnowledgeRetrievalLog::getStartTime, endDate.atTime(LocalTime.MAX));
        }

        List<AiKnowledgeRetrievalLog> logs = knowledgeRetrievalLogMapper.selectList(wrapper);

        Map<Long, List<AiKnowledgeRetrievalLog>> groupedByKb = logs.stream()
            .filter(log -> log.getKnowledgeBaseId() != null)
            .collect(Collectors.groupingBy(AiKnowledgeRetrievalLog::getKnowledgeBaseId));

        List<KnowledgeRetrievalStatItem> result = new ArrayList<>();
        for (Map.Entry<Long, List<AiKnowledgeRetrievalLog>> entry : groupedByKb.entrySet()) {
            List<AiKnowledgeRetrievalLog> kbLogs = entry.getValue();
            String kbName = kbLogs.get(0).getKnowledgeBaseName();

            long totalRetrievals = kbLogs.size();
            long successRetrievals = kbLogs.stream()
                .filter(log -> log.getCallStatus() != null && log.getCallStatus() == 1)
                .count();
            long totalRetrievedChunks = kbLogs.stream()
                .filter(log -> log.getRetrievedCount() != null)
                .mapToLong(AiKnowledgeRetrievalLog::getRetrievedCount)
                .sum();
            long totalEmbeddingTokens = kbLogs.stream()
                .filter(log -> log.getQueryEmbeddingTokens() != null)
                .mapToLong(AiKnowledgeRetrievalLog::getQueryEmbeddingTokens)
                .sum();
            double avgDuration = kbLogs.stream()
                .filter(log -> log.getDuration() != null)
                .mapToInt(AiKnowledgeRetrievalLog::getDuration)
                .average()
                .orElse(0);

            result.add(KnowledgeRetrievalStatItem.builder()
                .knowledgeBaseId(entry.getKey())
                .knowledgeBaseName(kbName)
                .totalRetrievals(totalRetrievals)
                .successRetrievals(successRetrievals)
                .totalRetrievedChunks(totalRetrievedChunks)
                .totalEmbeddingTokens(totalEmbeddingTokens)
                .avgDurationMs(BigDecimal.valueOf(avgDuration).setScale(2, RoundingMode.HALF_UP))
                .build());
        }

        result.sort((a, b) -> b.getTotalRetrievals().compareTo(a.getTotalRetrievals()));
        return result;
    }

    private LlmCallLogItem toLlmCallLogItem(AiLlmCallLog log) {
        return LlmCallLogItem.builder()
            .id(log.getId())
            .traceId(log.getTraceId())
            .callSource(log.getCallSource())
            .sourceId(log.getSourceId())
            .sourceNodeId(log.getSourceNodeId())
            .modelName(log.getModelName())
            .modelProvider(log.getModelProvider())
            .temperature(log.getTemperature())
            .inputCharCount(log.getInputCharCount())
            .inputTokens(log.getInputTokens())
            .outputCharCount(log.getOutputCharCount())
            .outputTokens(log.getOutputTokens())
            .totalTokens(log.getTotalTokens())
            .finishReason(log.getFinishReason())
            .success(log.getCallStatus() != null && log.getCallStatus() == 1)
            .errorMessage(log.getErrorMessage())
            .durationMs(log.getDuration())
            .createTime(log.getCreateTime())
            .build();
    }

    private List<AiLlmCallLog> queryLlmCallLogsInRange(LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<AiLlmCallLog> wrapper = Wrappers.lambdaQuery();
        if (startDate != null) {
            wrapper.ge(AiLlmCallLog::getStartTime, startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.le(AiLlmCallLog::getStartTime, endDate.atTime(LocalTime.MAX));
        }
        return llmCallLogMapper.selectList(wrapper);
    }

    private LlmCallStatItem buildStatItem(String name, LocalDate date, Long modelId, Integer orgId, List<AiLlmCallLog> logs) {
        long totalCalls = logs.size();
        long successCalls = logs.stream()
            .filter(log -> log.getCallStatus() != null && log.getCallStatus() == 1)
            .count();
        long failCalls = totalCalls - successCalls;
        long totalInputTokens = logs.stream()
            .filter(log -> log.getInputTokens() != null)
            .mapToLong(AiLlmCallLog::getInputTokens)
            .sum();
        long totalOutputTokens = logs.stream()
            .filter(log -> log.getOutputTokens() != null)
            .mapToLong(AiLlmCallLog::getOutputTokens)
            .sum();
        long totalTokens = logs.stream()
            .filter(log -> log.getTotalTokens() != null)
            .mapToLong(AiLlmCallLog::getTotalTokens)
            .sum();
        double avgDuration = logs.stream()
            .filter(log -> log.getDuration() != null)
            .mapToInt(AiLlmCallLog::getDuration)
            .average()
            .orElse(0);

        return LlmCallStatItem.builder()
            .name(name)
            .date(date)
            .modelId(modelId)
            .orgId(orgId)
            .totalCalls(totalCalls)
            .successCalls(successCalls)
            .failCalls(failCalls)
            .totalInputTokens(totalInputTokens)
            .totalOutputTokens(totalOutputTokens)
            .totalTokens(totalTokens)
            .avgDurationMs(BigDecimal.valueOf(avgDuration).setScale(2, RoundingMode.HALF_UP))
            .build();
    }
}
