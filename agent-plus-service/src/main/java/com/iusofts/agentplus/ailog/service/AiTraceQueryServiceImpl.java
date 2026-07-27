package com.iusofts.agentplus.ailog.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iusofts.agentplus.ailog.entity.AiTraceSpan;
import com.iusofts.agentplus.ailog.entity.AiTraceSpanPayload;
import com.iusofts.agentplus.ailog.interfaces.IAiTraceQueryService;
import com.iusofts.agentplus.ailog.mapper.AiTraceSpanMapper;
import com.iusofts.agentplus.ailog.mapper.AiTraceSpanPayloadMapper;
import com.iusofts.agentplus.aiflow.vo.AiFlowRuntimeTraceReqVo;
import com.iusofts.agentplus.aiflow.vo.AiFlowTraceEventVo;
import com.iusofts.agentplus.aiflow.vo.AiFlowTraceTreeVo;
import com.iusofts.agentplus.aiflow.vo.AiFlowTraceVo;
import com.iusofts.agentplus.aiflow.vo.AiSpanDetailVo;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI Trace 查询服务实现。
 *
 * <p>基于 ai_trace_span 单表组装查询结果。</p>
 *
 * @author Ivan
 * @since 2026-07-25
 */
@Service
public class AiTraceQueryServiceImpl implements IAiTraceQueryService {

    @Resource
    private AiTraceSpanMapper aiTraceSpanMapper;

    @Resource
    private AiTraceSpanPayloadMapper aiTraceSpanPayloadMapper;

    @Override
    public AiFlowTraceVo queryTrace(AiFlowRuntimeTraceReqVo reqVo) {
        String traceId = reqVo.getTraceId();

        List<AiTraceSpan> spans = aiTraceSpanMapper.selectList(
                Wrappers.<AiTraceSpan>lambdaQuery()
                        .eq(AiTraceSpan::getTraceId, traceId)
                        .orderByAsc(AiTraceSpan::getStartTime));

        if (spans.isEmpty()) {
            throw new SystemBusinessException("链路数据不存在");
        }

        AiFlowTraceVo traceVo = new AiFlowTraceVo();
        List<AiFlowTraceEventVo> events = traceVo.getTraceEvents();

        for (AiTraceSpan span : spans) {
            AiFlowTraceEventVo event = new AiFlowTraceEventVo();
            event.setId(span.getId());
            event.setName(getSpanLabel(span));
            event.setPh("X");
            event.setTs(toMicros(span.getStartTime()));
            event.setDur(span.getDurationMs() == null ? 0L : span.getDurationMs() * 1000L);
            event.setCat(getSpanCat(span));
            events.add(event);
        }

        // 按开始时间升序、时长降序排序(父区间在前),便于前端火焰图渲染
        events.sort((a, b) -> {
            int c = Long.compare(a.getTs(), b.getTs());
            return c != 0 ? c : Long.compare(b.getDur(), a.getDur());
        });

        return traceVo;
    }

    @Override
    public List<AiFlowTraceTreeVo> queryTraceTree(AiFlowRuntimeTraceReqVo reqVo) {
        String traceId = reqVo.getTraceId();

        List<AiTraceSpan> spans = aiTraceSpanMapper.selectList(
                Wrappers.<AiTraceSpan>lambdaQuery()
                        .eq(AiTraceSpan::getTraceId, traceId)
                        .orderByAsc(AiTraceSpan::getStartTime));

        if (spans.isEmpty()) {
            throw new SystemBusinessException("链路数据不存在");
        }

        // 构建 spanId -> span 的映射
        Map<String, AiTraceSpan> spanMap = spans.stream()
                .collect(Collectors.toMap(AiTraceSpan::getSpanId, s -> s));

        // 构建 parentSpanId -> children 的映射
        Map<String, List<AiTraceSpan>> childrenMap = new LinkedHashMap<>();
        for (AiTraceSpan span : spans) {
            String parentSpanId = span.getParentSpanId();
            if (parentSpanId == null) {
                parentSpanId = "";
            }
            childrenMap.computeIfAbsent(parentSpanId, k -> new ArrayList<>()).add(span);
        }

        // 找到根节点 (parentSpanId 为 "0000000000000000" 或为空，且没有父 span 的)
        AiTraceSpan rootSpan = null;
        for (AiTraceSpan span : spans) {
            String parentSpanId = span.getParentSpanId();
            if ("0000000000000000".equals(parentSpanId)
                    || parentSpanId == null
                    || parentSpanId.isEmpty()
                    || !spanMap.containsKey(parentSpanId)) {
                rootSpan = span;
                break;
            }
        }

        if (rootSpan == null) {
            // 如果没有找到明确的根节点，取最早的一个作为根
            rootSpan = spans.get(0);
        }

        // 递归构建树
        AiFlowTraceTreeVo root = buildTree(rootSpan, childrenMap);

        List<AiFlowTraceTreeVo> result = new ArrayList<>();
        result.add(root);
        return result;
    }

    private AiFlowTraceTreeVo buildTree(AiTraceSpan span, Map<String, List<AiTraceSpan>> childrenMap) {
        AiFlowTraceTreeVo vo = new AiFlowTraceTreeVo();
        vo.setId(span.getId());
        vo.setLabel(getSpanLabel(span));
        vo.setDur(span.getDurationMs() == null ? 0L : span.getDurationMs() * 1000L);
        vo.setCat(getSpanCat(span));
        vo.setNodeId(getNodeId(span));
        vo.setTokens(getTokens(span));
        vo.setStatus("UNSET".equals(span.getStatus()) ? "OK" : span.getStatus());

        List<AiTraceSpan> children = childrenMap.get(span.getSpanId());
        if (children != null) {
            for (AiTraceSpan child : children) {
                vo.getChildren().add(buildTree(child, childrenMap));
            }
        }

        return vo;
    }

    private String getSpanLabel(AiTraceSpan span) {
        Map<String, Object> attrs = span.getAttributes();
        if (attrs != null && attrs.containsKey("label")) {
            Object label = attrs.get("label");
            if (label != null) {
                return label.toString();
            }
        }
        return span.getSpanName();
    }

    private String getSpanCat(AiTraceSpan span) {
        Map<String, Object> attrs = span.getAttributes();
        if (attrs != null && attrs.containsKey("nodeType")) {
            Object nodeType = attrs.get("nodeType");
            if (nodeType != null) {
                return nodeType.toString();
            }
        }
        String spanName = span.getSpanName();
        if (spanName != null) {
            // 按 "." 分割取第一个部分
            int dotIndex = spanName.indexOf('.');
            if (dotIndex > 0) {
                return spanName.substring(0, dotIndex);
            }
            return spanName;
        }
        return "internal";
    }

    private String getNodeId(AiTraceSpan span) {
        Map<String, Object> attrs = span.getAttributes();
        if (attrs != null && attrs.containsKey("nodeId")) {
            Object nodeId = attrs.get("nodeId");
            if (nodeId != null) {
                return nodeId.toString();
            }
        }
        if (attrs != null && attrs.containsKey("ai.source_node_id")) {
            Object sourceNodeId = attrs.get("ai.source_node_id");
            if (sourceNodeId != null) {
                return sourceNodeId.toString();
            }
        }
        return null;
    }

    private Long getTokens(AiTraceSpan span) {
        Map<String, Object> attrs = span.getAttributes();
        if (attrs != null && attrs.containsKey("ai.tokens")) {
            Object tokens = attrs.get("ai.tokens");
            if (tokens instanceof Number) {
                return ((Number) tokens).longValue();
            }
            if (tokens != null) {
                try {
                    return Long.parseLong(tokens.toString());
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
        return null;
    }

    /** LocalDateTime 转微秒时间戳(系统默认时区)。 */
    private long toMicros(LocalDateTime time) {
        if (time == null) {
            return 0L;
        }
        var instant = time.atZone(ZoneId.systemDefault()).toInstant();
        return instant.getEpochSecond() * 1_000_000L + instant.getNano() / 1000L;
    }

    @Override
    public AiSpanDetailVo querySpanDetail(Long id) {
        AiTraceSpan span = aiTraceSpanMapper.selectById(id);
        if (span == null) {
            throw new SystemBusinessException("Span数据不存在");
        }

        AiSpanDetailVo vo = new AiSpanDetailVo();
        vo.setId(span.getId());
        vo.setTraceId(span.getTraceId());
        vo.setLabel(getSpanLabel(span));
        vo.setDur(span.getDurationMs() == null ? 0L : span.getDurationMs() * 1000L);
        vo.setCat(getSpanCat(span));
        vo.setNodeId(getNodeId(span));
        vo.setTokens(getTokens(span));
        vo.setStatus("UNSET".equals(span.getStatus()) ? "OK" : span.getStatus());
        vo.setStatusMessage(span.getStatusMessage());

        // 查询出入参
        AiTraceSpanPayload payload = aiTraceSpanPayloadMapper.selectOne(
                Wrappers.<AiTraceSpanPayload>lambdaQuery()
                        .eq(AiTraceSpanPayload::getTraceId, span.getTraceId())
                        .eq(AiTraceSpanPayload::getSpanId, span.getSpanId())
                        .last("LIMIT 1"));
        if (payload != null) {
            vo.setInputPayload(payload.getInputPayload());
            vo.setOutputPayload(payload.getOutputPayload());
        }

        return vo;
    }

}
