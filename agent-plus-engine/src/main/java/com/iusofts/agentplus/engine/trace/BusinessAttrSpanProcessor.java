package com.iusofts.agentplus.engine.trace;

import com.iusofts.agentplus.trace.constants.TraceConstant;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 业务属性同步 SpanProcessor。
 *
 * <p>OTel 的 {@code Span.attributes} 不会从 parent 继承，所以子 span 拿不到 root 上的
 * {@code ai.org_id / ai.operator_id / workflow.trial_flag}。本处理器在每个 span
 * {@code onStart} 阶段，从 {@code parentContext} 的 baggage 中读取上述业务键并写入
 * 当前 span 的 attributes，确保 {@code MySqlSpanExporter} 与采样判断对子 span 也可见。</p>
 *
 * <p>业务侧只需在 root span（{@code WorkflowEngine.execute} / 试运行入口 / chat 入口）
 * 调用 {@code TraceUtil.setOperator(operatorId, orgId)} + {@code TraceUtil.setTrialFlag(...)}
 * 写入 baggage 即可，所有子 span 自动补全。</p>
 *
 * <p>注册顺序：必须先于 {@code BatchSpanProcessor} 注册（{@code addSpanProcessor}
 * 的注册顺序即 {@code onStart} 触发顺序）。</p>
 *
 * @author Ivan
 * @since 2026-08-10
 */
public class BusinessAttrSpanProcessor implements SpanProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BusinessAttrSpanProcessor.class);

    private static final AttributeKey<Long> ATTR_OPERATOR_ID = AttributeKey.longKey(TraceConstant.KEY_OPERATOR_ID);
    private static final AttributeKey<Long> ATTR_ORG_ID = AttributeKey.longKey(TraceConstant.KEY_ORG_ID);
    private static final AttributeKey<Long> ATTR_TRIAL_FLAG = AttributeKey.longKey(TraceConstant.ATTR_TRIAL_FLAG);

    @Override
    public void onStart(Context parentContext, ReadWriteSpan span) {
        try {
            Baggage baggage = Baggage.fromContext(parentContext);
            if (baggage == null) {
                return;
            }
            String op = baggage.getEntryValue(TraceConstant.KEY_OPERATOR_ID);
            if (op != null && !op.isEmpty()) {
                span.setAttribute(ATTR_OPERATOR_ID, Long.parseLong(op));
            }
            String org = baggage.getEntryValue(TraceConstant.KEY_ORG_ID);
            if (org != null && !org.isEmpty()) {
                span.setAttribute(ATTR_ORG_ID, Long.parseLong(org));
            }
            String trial = baggage.getEntryValue(TraceConstant.ATTR_TRIAL_FLAG);
            if (trial != null && !trial.isEmpty()) {
                span.setAttribute(ATTR_TRIAL_FLAG, parseTrialFlag(trial));
            }
        } catch (NumberFormatException e) {
            LOGGER.warn("BusinessAttrSpanProcessor 解析 baggage 业务属性失败: {}", e.getMessage());
        }
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(ReadableSpan span) {
        // 无需 onEnd 处理
    }

    @Override
    public boolean isEndRequired() {
        return false;
    }

    /**
     * 兼容 trialFlag 多种字面量：{@code 0/1}、{@code true/false}、{@code yes/no}（不区分大小写）。
     */
    private static long parseTrialFlag(String raw) {
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return 0L;
        }
        char c = Character.toLowerCase(trimmed.charAt(0));
        if (c == 't' || c == 'y' || c == '1') {
            return 1L;
        }
        return 0L;
    }
}
