package com.iusofts.agentplus.ai.knowledge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 文档处理有界线程池执行器。
 *
 * <p>用有界队列 + {@code CallerRunsPolicy} 实现并发限制与背压:队列满时由提交线程(或定时任务线程)
 * 直接执行,天然限速而不丢任务。文档处理本身幂等(带 Redisson 锁),重复提交安全。</p>
 *
 * @author Ivan
 */
@Component
public class KnowledgeIngestExecutor {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeIngestExecutor.class);

    @Resource
    private KnowledgeProperties properties;

    @Resource
    private KnowledgeIngestionService ingestionService;

    private ThreadPoolExecutor executor;

    @PostConstruct
    public void init() {
        KnowledgeProperties.Ingest cfg = properties.getIngest();
        AtomicLong seq = new AtomicLong();
        this.executor = new ThreadPoolExecutor(
                cfg.getCorePoolSize(),
                cfg.getMaxPoolSize(),
                cfg.getKeepAliveSeconds(), TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(cfg.getQueueCapacity()),
                r -> {
                    Thread t = new Thread(r, "kb-ingest-" + seq.incrementAndGet());
                    t.setDaemon(true);
                    return t;
                },
                // 队列满 -> 调用方线程执行,形成背压
                new ThreadPoolExecutor.CallerRunsPolicy());
        log.info("知识库文档处理线程池初始化: core={}, max={}, queue={}",
                cfg.getCorePoolSize(), cfg.getMaxPoolSize(), cfg.getQueueCapacity());
    }

    /**
     * 提交文档处理任务。异常在任务内部已捕获落库,此处仅兜底日志。
     *
     * @param documentId 文档 id
     */
    public void submit(Long documentId) {
        executor.execute(() -> {
            try {
                ingestionService.process(documentId);
            } catch (Exception e) {
                log.error("文档处理任务异常: documentId={}", documentId, e);
            }
        });
    }

    /** 当前队列积压任务数(供监控/定时任务判断是否继续投递)。 */
    public int queueSize() {
        return executor.getQueue().size();
    }

    @PreDestroy
    public void shutdown() {
        if (executor == null) {
            return;
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }
}
