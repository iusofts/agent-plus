package com.iusofts.agentplus.engine.config;

import com.iusofts.agentplus.engine.trace.BusinessAttrSpanProcessor;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenTelemetry Trace SDK 初始化配置。
 *
 * <p>启动时构建 {@link SdkTracerProvider} + {@link BatchSpanProcessor} 并注册到
 * {@link GlobalOpenTelemetry}。业务模块（agent-plus-service）通过提供
 * {@link SpanExporter} bean 来自定义落库；未提供时不注册导出处理器（span 不落库，
 * tracer 仍可正常使用）。</p>
 *
 * @author Ivan
 * @since 2026-07-24
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "agent-plus.trace", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TraceAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(TraceAutoConfiguration.class);

    private SdkTracerProvider tracerProvider;

    @Bean
    @ConfigurationProperties(prefix = "agent-plus.trace.exporter")
    public TraceExporterProperties traceExporterProperties() {
        return new TraceExporterProperties();
    }

    @Bean
    public SdkTracerProvider sdkTracerProvider(ObjectProvider<SpanExporter> spanExporters,
                                               TraceExporterProperties props) {
        SpanExporter exporter = spanExporters.getIfUnique();

        io.opentelemetry.sdk.trace.SdkTracerProviderBuilder providerBuilder = SdkTracerProvider.builder();
        // 业务属性同步:必须先于 BatchSpanProcessor 注册,onStart 触发顺序与注册顺序一致。
        // 把 baggage 中的 ai.org_id / ai.operator_id / workflow.trial_flag 回写到每个 span 的 attributes,
        // 确保子 span 也能被 MySqlSpanExporter 与采样判断读到。
        providerBuilder.addSpanProcessor(new BusinessAttrSpanProcessor());
        if (exporter != null) {
            providerBuilder.addSpanProcessor(BatchSpanProcessor.builder(exporter)
                    .setMaxExportBatchSize(props.getBatchSize())
                    .setScheduleDelay(java.time.Duration.ofMillis(props.getScheduleDelay()))
                    .setMaxQueueSize(props.getMaxQueueSize())
                    .build());
            LOGGER.info("OpenTelemetry Trace SDK 初始化完成, exporter={}", exporter.getClass().getSimpleName());
        } else {
            LOGGER.warn("未找到 SpanExporter bean, span 将不会落库(tracer 仍可用)");
        }

        this.tracerProvider = providerBuilder.build();

        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(this.tracerProvider)
                .build();
        GlobalOpenTelemetry.set(openTelemetry);

        return this.tracerProvider;
    }

    @PreDestroy
    public void shutdown() {
        if (tracerProvider != null) {
            LOGGER.info("关闭 OpenTelemetry Trace SDK...");
            tracerProvider.shutdown();
        }
    }

    /**
     * SpanExporter 批量配置属性。
     */
    public static class TraceExporterProperties {

        /** 每批最大导出条数。 */
        private int batchSize = 512;

        /** 定时导出间隔(毫秒)。 */
        private long scheduleDelay = 5000;

        /** 队列最大容量。 */
        private int maxQueueSize = 2048;

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public long getScheduleDelay() {
            return scheduleDelay;
        }

        public void setScheduleDelay(long scheduleDelay) {
            this.scheduleDelay = scheduleDelay;
        }

        public int getMaxQueueSize() {
            return maxQueueSize;
        }

        public void setMaxQueueSize(int maxQueueSize) {
            this.maxQueueSize = maxQueueSize;
        }
    }
}