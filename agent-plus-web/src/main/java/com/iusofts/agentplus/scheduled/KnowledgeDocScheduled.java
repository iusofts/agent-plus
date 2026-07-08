package com.iusofts.agentplus.scheduled;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iusofts.agentplus.ai.entity.AiKnowledgeDocument;
import com.iusofts.agentplus.ai.knowledge.KnowledgeIngestExecutor;
import com.iusofts.agentplus.ai.knowledge.KnowledgeIngestionService;
import com.iusofts.agentplus.plugin.vectorstore.KnowledgeProperties;
import com.iusofts.agentplus.ai.mapper.AiKnowledgeDocumentMapper;
import com.iusofts.agentplus.basic.redis.RedisLock;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 知识库文档处理补偿任务。
 *
 * <p>兜底两类文档,重新投递到处理线程池(处理本身幂等):</p>
 * <ul>
 *   <li>status=0 待处理:进程重启/异步提交丢失导致漏处理的文档;</li>
 *   <li>status=1 处理中且超过 {@code knowledge.process-timeout-seconds}:处理中途宕机的僵死文档。</li>
 * </ul>
 *
 * <p>Redisson 锁保证多实例下同一时刻只有一个节点扫描投递。</p>
 *
 * @author Ivan
 */
@Slf4j
@Component
public class KnowledgeDocScheduled {

    private static final String LOCK_KEY = "knowledge:schedule:compensate";
    /** 单次补偿最多投递的文档数,避免瞬时压垮线程池。 */
    private static final int BATCH_LIMIT = 100;

    @Resource
    private RedisLock redisLock;

    @Resource
    private AiKnowledgeDocumentMapper documentMapper;

    @Resource
    private KnowledgeIngestExecutor ingestExecutor;

    @Resource
    private KnowledgeProperties knowledgeProperties;

    /**
     * 每 2 分钟扫描一次待处理/处理超时文档并重新投递。
     */
    @Scheduled(cron = "0 */2 * * * ?")
    public void compensate() {
        boolean acquired = redisLock.tryLock(LOCK_KEY, 5, TimeUnit.MINUTES);
        if (!acquired) {
            return;
        }
        try {
            LocalDateTime timeoutBefore = LocalDateTime.now()
                    .minusSeconds(knowledgeProperties.getProcessTimeoutSeconds());

            LambdaQueryWrapper<AiKnowledgeDocument> wrapper = Wrappers.lambdaQuery();
            wrapper.and(w -> w
                    // 待处理
                    .eq(AiKnowledgeDocument::getStatus, KnowledgeIngestionService.STATUS_PENDING)
                    // 或 处理中且已超时(更新时间早于超时阈值)
                    .or(o -> o
                            .eq(AiKnowledgeDocument::getStatus, KnowledgeIngestionService.STATUS_PROCESSING)
                            .lt(AiKnowledgeDocument::getUpdateTime, timeoutBefore)));
            wrapper.orderByAsc(AiKnowledgeDocument::getId);
            wrapper.last("limit " + BATCH_LIMIT);

            List<AiKnowledgeDocument> docs = documentMapper.selectList(wrapper);
            if (docs.isEmpty()) {
                return;
            }
            log.info("知识库补偿任务:发现 {} 个待处理/超时文档,重新投递", docs.size());
            for (AiKnowledgeDocument doc : docs) {
                ingestExecutor.submit(doc.getId());
            }
        } catch (Exception e) {
            log.error("知识库补偿任务执行异常", e);
        } finally {
            redisLock.releaseLock(LOCK_KEY);
        }
    }
}
