package com.iusofts.agentplus.ailog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.iusofts.agentplus.aiflow.vo.AiTraceSpanListVo;
import com.iusofts.agentplus.aiflow.vo.AiTraceSpanPageReqVo;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.basic.utils.ModelMapperUtil;
import com.iusofts.agentplus.basic.web.vo.page.PageResult;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.iusofts.agentplus.trace.constants.TraceConstant.ROOT_SPAN_ID;

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

        List<AiTraceSpan> spans = loadSpansByTraceId(traceId);
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

        List<AiTraceSpan> spans = loadSpansByTraceId(traceId);
        if (spans.isEmpty()) {
            throw new SystemBusinessException("链路数据不存在");
        }

        // 构建 spanId -> span 的映射
        Map<String, AiTraceSpan> spanMap = spans.stream()
                .collect(Collectors.toMap(AiTraceSpan::getSpanId, s -> s));

        Map<String, List<AiTraceSpan>> childrenMap = buildChildrenMap(spans);

        // 找到根节点 (parentSpanId 为 ROOT_SPAN_ID 或为空，且没有父 span 的)
        AiTraceSpan rootSpan = null;
        for (AiTraceSpan span : spans) {
            String parentSpanId = span.getParentSpanId();
            if (ROOT_SPAN_ID.equals(parentSpanId)
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

    /**
     * 按 traceId 加载链路下全部 span（按开始时间升序）。树视图、详情视图共用，
     * 详情页需要拿到整条链路后才能对子级 tokens 进行聚合。
     */
    private List<AiTraceSpan> loadSpansByTraceId(String traceId) {
        return aiTraceSpanMapper.selectList(
                Wrappers.<AiTraceSpan>lambdaQuery()
                        .eq(AiTraceSpan::getTraceId, traceId)
                        .orderByAsc(AiTraceSpan::getStartTime));
    }

    /**
     * 构建 parentSpanId -> children 的映射。parentSpanId 为空/null 的归到 "" 分组。
     */
    private Map<String, List<AiTraceSpan>> buildChildrenMap(List<AiTraceSpan> spans) {
        Map<String, List<AiTraceSpan>> childrenMap = new LinkedHashMap<>();
        for (AiTraceSpan span : spans) {
            String parentSpanId = span.getParentSpanId();
            if (parentSpanId == null) {
                parentSpanId = "";
            }
            childrenMap.computeIfAbsent(parentSpanId, k -> new ArrayList<>()).add(span);
        }
        return childrenMap;
    }

    private AiFlowTraceTreeVo buildTree(AiTraceSpan span, Map<String, List<AiTraceSpan>> childrenMap) {
        AiFlowTraceTreeVo vo = new AiFlowTraceTreeVo();
        vo.setId(span.getId());
        vo.setLabel(getSpanLabel(span));
        vo.setDur(span.getDurationMs() == null ? 0L : span.getDurationMs() * 1000L);
        vo.setCat(getSpanCat(span));
        vo.setNodeId(getNodeId(span));
        // 自己携带 ai.tokens 时使用自身值，否则递归汇总所有子孙级 tokens。
        // 原 Exporter 层的累加逻辑移到这里，避免落库时反复改写 attribute。
        vo.setTokens(resolveTokens(span, childrenMap));
        vo.setStatus("UNSET".equals(span.getStatus()) ? "OK" : span.getStatus());

        List<AiTraceSpan> children = childrenMap.get(span.getSpanId());
        if (children != null) {
            for (AiTraceSpan child : children) {
                vo.getChildren().add(buildTree(child, childrenMap));
            }
        }

        return vo;
    }

    /**
     * 解析 span 的展示 tokens：自己有则用自己的 tokens；否则递归汇总所有子孙级 tokens。
     */
    private Long resolveTokens(AiTraceSpan span, Map<String, List<AiTraceSpan>> childrenMap) {
        Long own = getTokens(span);
        if (own != null && own > 0) {
            return own;
        }
        List<AiTraceSpan> children = childrenMap.get(span.getSpanId());
        if (children == null || children.isEmpty()) {
            return own;
        }
        long sum = 0L;
        boolean hasAny = false;
        for (AiTraceSpan child : children) {
            Long childTokens = resolveTokens(child, childrenMap);
            if (childTokens != null && childTokens > 0) {
                sum += childTokens;
                hasAny = true;
            }
        }
        return hasAny ? sum : own;
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
        // 与树视图同口径：自身携带 ai.tokens > 0 时直接返回（多为 LLM/工具等真实消耗点），
        // 只有父级未记、子级才有消耗的场景才去加载整条 trace 做子孙级累加。
        Long ownTokens = getTokens(span);
        if (ownTokens != null && ownTokens > 0) {
            vo.setTokens(ownTokens);
        } else {
            List<AiTraceSpan> traceSpans = loadSpansByTraceId(span.getTraceId());
            vo.setTokens(traceSpans.isEmpty()
                    ? ownTokens
                    : resolveTokens(span, buildChildrenMap(traceSpans)));
        }
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

    @Override
    public PageResult<AiTraceSpanListVo> pageRootSpan(AiTraceSpanPageReqVo reqVo) {
        // 仅查询根 Span（parent_span_id = ROOT_SPAN_ID），按 start_time 倒序
        var wrapper = Wrappers.<AiTraceSpan>lambdaQuery()
                .eq(AiTraceSpan::getParentSpanId, ROOT_SPAN_ID)
                .orderByDesc(AiTraceSpan::getStartTime);

        if (StringUtils.hasText(reqVo.getTraceId())) {
            wrapper.like(AiTraceSpan::getTraceId, reqVo.getTraceId());
        }
        if (StringUtils.hasText(reqVo.getSpanName())) {
            wrapper.like(AiTraceSpan::getSpanName, reqVo.getSpanName());
        }
        if (StringUtils.hasText(reqVo.getStatus())) {
            wrapper.eq(AiTraceSpan::getStatus, reqVo.getStatus());
        }
        if (reqVo.getOrgId() != null) {
            wrapper.eq(AiTraceSpan::getOrgId, reqVo.getOrgId());
        }
        if (reqVo.getOperatorId() != null) {
            wrapper.eq(AiTraceSpan::getOperatorId, reqVo.getOperatorId());
        }
        if (reqVo.getTrialFlag() != null) {
            wrapper.eq(AiTraceSpan::getTrialFlag, reqVo.getTrialFlag());
        }

        Page<AiTraceSpan> pageParam = new Page<>(reqVo.getCurrentPage(), reqVo.getPageSize());
        IPage<AiTraceSpan> page = aiTraceSpanMapper.selectPage(pageParam, wrapper);

        List<AiTraceSpanListVo> list = page.getRecords().stream()
                .map(span -> {
                    AiTraceSpanListVo vo = ModelMapperUtil.strictMap(span, AiTraceSpanListVo.class);
                    vo.setLabel(getSpanLabel(span));
                    return vo;
                })
                .toList();

        PageResult<AiTraceSpanListVo> pageResult = new PageResult<>();
        pageResult.setDataList(list);
        pageResult.setTotalCount(page.getTotal());
        return pageResult;
    }

}
