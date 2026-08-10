package com.iusofts.agentplus.ailog.sample;

import com.iusofts.agentplus.ailog.config.TraceSampleProperties;
import com.iusofts.agentplus.ailog.interfaces.IAiTraceSampleConfigService;
import com.iusofts.agentplus.ailog.vo.AiTraceSampleConfigResolveReqVo;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.trace.data.SpanData;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * AI Trace 采样过滤服务(per-trace 模式 + Redis 决策缓存)。
 *
 * <h3>设计目标</h3>
 * <ul>
 *   <li><b>trace 完整</b>:整条 trace 共进退(root 决定),避免 trace 树断链</li>
 *   <li><b>跨批/跨实例一致</b>:决策结果写入 Redis,后续批/其他实例查 Redis 复用,
 *       避免 root 在 A 实例 + child 在 B 实例的批内决策不一致</li>
 *   <li><b>热路径零开销</b>:short-circuit——yml 兜底=1.0 且无 user/org 覆盖时直接放行</li>
 * </ul>
 *
 * <h3>判定流程</h3>
 * <pre>{@code
 * filter(spans)
 *   ├─ short-circuit: defaultRate>=1.0 && !hasUserOrOrgOverride → 直接放行
 *   └─ 按 traceId 分组
 *        └─ 每组:
 *             ├─ 查 Redis 决策缓存 (ai:trace:sample:trace:{traceId})
 *             │     ├─ hit → 用缓存结果
 *             │     └─ miss → 找 root 决策 → set 写 Redis (5min TTL) → 应用
 *             └─ 应用决策:整组保留或整组丢
 * }</pre>
 *
 * <h3>试运行</h3>
 * {@code trialFlag=1} 始终保留(试运行数据量小、调试价值高)。
 *
 * @author Ivan
 * @since 2026-08-10
 */
@Component
public class AiTraceSampleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiTraceSampleService.class);

    private static final AttributeKey<Long> ATTR_OPERATOR_ID = AttributeKey.longKey("ai.operator_id");
    private static final AttributeKey<Long> ATTR_ORG_ID = AttributeKey.longKey("ai.org_id");
    private static final AttributeKey<Long> ATTR_TRIAL_FLAG = AttributeKey.longKey("workflow.trial_flag");

    /** OTel ROOT_SPAN_ID(32 字符全 0) */
    private static final String ROOT_SPAN_ID = "0000000000000000";

    /** Redis 决策缓存 key 前缀,完整 key = {prefix}{traceId} */
    private static final String DECISION_KEY_PREFIX = "ai:trace:sample:trace:";
    /** Redis 决策缓存 TTL(分钟) */
    private static final long DECISION_TTL_MINUTES = 5L;

    @Autowired
    private IAiTraceSampleConfigService configService;

    @Autowired
    private TraceSampleProperties props;

    /**
     * Redisson 客户端。{@link ObjectProvider} 包装,允许集群下未部署 Redis 时降级
     * (决策仅在单批内生效,跨批一致性由 fallback 策略保证)。
     */
    @Autowired
    private ObjectProvider<RedissonClient> redissonClientProvider;

    /**
     * 过滤 span 集合,按 traceId 分组 per-trace 决策后返回保留的 span。
     */
    public Collection<SpanData> filter(Collection<SpanData> spans) {
        if (spans == null || spans.isEmpty()) {
            return List.of();
        }

        // 1) short-circuit:yml 兜底=1.0 且无 user/org 覆盖时直接放行
        BigDecimal defaultRate = props.getDefaultSampleRate();
        if (defaultRate != null
                && defaultRate.compareTo(BigDecimal.ONE) >= 0
                && !configService.hasUserOrOrgOverride()) {
            return spans;
        }

        // 2) 按 traceId 分组(LinkedHashMap 保留插入顺序便于日志追踪)
        Map<String, List<SpanData>> byTrace = new LinkedHashMap<>();
        for (SpanData s : spans) {
            byTrace.computeIfAbsent(s.getTraceId(), k -> new ArrayList<>()).add(s);
        }

        // 3) 每组独立决策
        List<SpanData> kept = new ArrayList<>(spans.size());
        int droppedTraces = 0;
        for (Map.Entry<String, List<SpanData>> e : byTrace.entrySet()) {
            String traceId = e.getKey();
            List<SpanData> group = e.getValue();
            if (decide(traceId, group)) {
                kept.addAll(group);
            } else {
                droppedTraces++;
                LOGGER.debug("AI Trace 采样: 丢弃 trace={} 共 {} 条 span", traceId, group.size());
            }
        }

        if (droppedTraces > 0) {
            LOGGER.debug("AI Trace 采样汇总: 总 {} 个 trace, 丢弃 {} 个, 保留 {} 个",
                    byTrace.size(), droppedTraces, byTrace.size() - droppedTraces);
        }
        return kept;
    }

    /**
     * 单条 trace 决策:先查 Redis 缓存,miss 时按 root 决策并写缓存。
     */
    private boolean decide(String traceId, List<SpanData> group) {
        // 1) 查 Redis 决策缓存
        Boolean cached = readDecisionFromRedis(traceId);
        if (cached != null) {
            return cached;
        }

        // 2) 找 root 决策
        SpanData root = findRoot(group);
        boolean keep = shouldKeepTrace(root);

        // 3) 写 Redis(失败不影响主流程)
        writeDecisionToRedis(traceId, keep);

        return keep;
    }

    /**
     * 找 trace 内 root span(parentSpanId 为空/ROOT_SPAN_ID)。
     * 找不到时 fallback 到第一条(跨批场景:本批只有 child span)。
     */
    private static SpanData findRoot(List<SpanData> group) {
        for (SpanData s : group) {
            String p = s.getParentSpanId();
            if (p == null || p.isEmpty() || ROOT_SPAN_ID.equals(p)) {
                return s;
            }
        }
        return group.get(0);
    }

    /**
     * 单 trace 采样决策(用 root 的 op/orgId/trialFlag)。
     */
    private boolean shouldKeepTrace(SpanData root) {
        // 1) 试运行必采
        Long trialFlag = root.getAttributes().get(ATTR_TRIAL_FLAG);
        if (trialFlag != null && trialFlag == 1L) {
            return true;
        }

        // 2) 读业务键,解析采样率
        Long userId = root.getAttributes().get(ATTR_OPERATOR_ID);
        Long orgId = root.getAttributes().get(ATTR_ORG_ID);
        BigDecimal rate;
        try {
            AiTraceSampleConfigResolveReqVo resolveReq = new AiTraceSampleConfigResolveReqVo();
            resolveReq.setUserId(userId);
            resolveReq.setOrgId(orgId);
            rate = configService.resolveSampleRate(resolveReq);
        } catch (Exception e) {
            rate = props.getDefaultSampleRate();
        }
        if (rate == null) {
            return true;
        }

        // 3) 边界判定
        if (rate.signum() <= 0) {
            return false;
        }
        if (rate.compareTo(BigDecimal.ONE) >= 0) {
            return true;
        }

        // 4) 比例采样
        return ThreadLocalRandom.current().nextDouble() < rate.doubleValue();
    }

    // ============================================================
    //  Redis 决策缓存(降级友好)
    // ============================================================

    private Boolean readDecisionFromRedis(String traceId) {
        RedissonClient client = redissonClientProvider.getIfAvailable();
        if (client == null) {
            return null;
        }
        try {
            return client.<Boolean>getBucket(DECISION_KEY_PREFIX + traceId).get();
        } catch (Exception e) {
            LOGGER.debug("读取 trace 决策缓存失败 traceId={}", traceId, e);
            return null;
        }
    }

    private void writeDecisionToRedis(String traceId, boolean keep) {
        RedissonClient client = redissonClientProvider.getIfAvailable();
        if (client == null) {
            return;
        }
        try {
            client.<Boolean>getBucket(DECISION_KEY_PREFIX + traceId)
                    .set(keep, java.time.Duration.ofMinutes(DECISION_TTL_MINUTES));
        } catch (Exception e) {
            LOGGER.debug("写入 trace 决策缓存失败 traceId={}", traceId, e);
        }
    }
}
