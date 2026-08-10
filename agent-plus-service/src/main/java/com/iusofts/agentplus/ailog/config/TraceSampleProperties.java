package com.iusofts.agentplus.ailog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/**
 * AI Trace 采样率兜底配置。
 *
 * <p>由 {@code agent-plus.trace.sample} 配置项前缀注入。当 DB 中
 * ai_trace_sample_config 未命中任何作用域(用户/组织/全局)配置时,
 * 解析器回退使用本类中的 {@link #getDefaultSampleRate()}。</p>
 *
 * <p>示例 yml:</p>
 * <pre>
 * agent-plus:
 *   trace:
 *     sample:
 *       # 兜底默认采样率,取值 0.0000 ~ 1.0000
 *       default-sample-rate: 1.0
 *       # 解析缓存失效时间(秒),0=不缓存。变更配置后可通过 refresh 接口立即失效
 *       cache-ttl-seconds: 60
 * </pre>
 *
 * @author Ivan
 * @since 2026-08-10
 */
@ConfigurationProperties(prefix = "agent-plus.trace.sample")
public class TraceSampleProperties {

    /**
     * 兜底默认采样率,默认 1.0(全量采集)。
     */
    private BigDecimal defaultSampleRate = new BigDecimal("1.0000");

    /**
     * 解析缓存 TTL(秒),0 表示不缓存(每次解析都查库)。默认 60 秒。
     */
    private long cacheTtlSeconds = 60L;

    public BigDecimal getDefaultSampleRate() {
        return defaultSampleRate;
    }

    public void setDefaultSampleRate(BigDecimal defaultSampleRate) {
        this.defaultSampleRate = defaultSampleRate;
    }

    public long getCacheTtlSeconds() {
        return cacheTtlSeconds;
    }

    public void setCacheTtlSeconds(long cacheTtlSeconds) {
        this.cacheTtlSeconds = cacheTtlSeconds;
    }
}
