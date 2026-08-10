package com.iusofts.agentplus.ailog.sample;

import com.iusofts.agentplus.ailog.config.TraceSampleProperties;
import com.iusofts.agentplus.ailog.interfaces.IAiTraceSampleConfigService;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.trace.data.SpanData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * AI Trace 采样过滤服务。
 *
 * <p>由 {@link com.iusofts.agentplus.ailog.exporter.MySqlSpanExporter}
 * 在批量导出前调用,根据
 * {@link com.iusofts.agentplus.ailog.service.AiTraceSampleConfigServiceImpl#resolveSampleRate}
 * 解析的采样率决定哪些 span 落库。</p>
 *
 * <p>判定规则(按顺序):</p>
 * <ol>
 *   <li>{@code trialFlag=1} → 必采(试运行数据量小,调试价值高)</li>
 *   <li>从 span attributes 读 {@code ai.operator_id} / {@code ai.org_id},
 *       调 {@code resolveSampleRate} 拿 rate(优先级:用户>组织>全局>yml 兜底)</li>
 *   <li>{@code rate <= 0} → 必丢</li>
 *   <li>{@code rate >= 1.0} → 必采</li>
 *   <li>{@code 0 < rate < 1.0} → {@code ThreadLocalRandom.nextDouble() < rate} 采样</li>
 * </ol>
 *
 * <p>每条 span 独立判定(不按 trace 整体采样),简化实现;
 * root span 与 child span 各自拿 attribute 中的 op/orgId/trialFlag
 * (由 {{@link com.iusofts.agentplus.engine.trace.BusinessAttrSpanProcessor}}
 * 已保证子 span 也补全业务键)。</p>
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

    @Autowired
    private IAiTraceSampleConfigService configService;

    @Autowired
    private TraceSampleProperties props;

    /**
     * 过滤 span 集合,返回保留下来的 span(同 trace 可能不完整,但降低落库量)。
     *
     * @param spans 待落库的 span 集合
     * @return 过滤后保留的 span;若全部被丢弃或入参为空,返回空集合
     */
    public Collection<SpanData> filter(Collection<SpanData> spans) {
        if (spans == null || spans.isEmpty()) {
            return List.of();
        }
        List<SpanData> kept = new ArrayList<>(spans.size());
        for (SpanData span : spans) {
            if (shouldKeep(span)) {
                kept.add(span);
            }
        }
        if (kept.size() != spans.size()) {
            LOGGER.debug("AI Trace 采样过滤: 入 {} 保留 {} 丢弃 {}",
                    spans.size(), kept.size(), spans.size() - kept.size());
        }
        return kept;
    }

    /**
     * 单条 span 采样判定。
     */
    private boolean shouldKeep(SpanData span) {
        // 1) 试运行必采
        Long trialFlag = span.getAttributes().get(ATTR_TRIAL_FLAG);
        if (trialFlag != null && trialFlag == 1L) {
            return true;
        }

        // 2) 读业务键,解析采样率
        Long userId = span.getAttributes().get(ATTR_OPERATOR_ID);
        Long orgId = span.getAttributes().get(ATTR_ORG_ID);
        BigDecimal rate;
        try {
            rate = configService.resolveSampleRate(userId, orgId);
        } catch (Exception e) {
            // 解析失败按 yml 兜底,避免阻塞导出
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
}
