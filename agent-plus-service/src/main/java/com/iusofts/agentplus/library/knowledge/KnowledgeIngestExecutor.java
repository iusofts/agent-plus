package com.iusofts.agentplus.library.knowledge;

import com.iusofts.agentplus.basic.enums.AsyncTaskGroup;
import com.iusofts.agentplus.basic.thread.AsyncManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 知识库文档处理执行器（接入 AsyncManager）。
 *
 * <p>使用 AsyncManager 虚拟线程执行文档处理任务，通过 KNOWLEDGE_INGEST 分组进行并发控制。
 * 文档处理本身幂等(带 Redisson 锁),重复提交安全。</p>
 *
 * @author Ivan
 */
@Component
public class KnowledgeIngestExecutor {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeIngestExecutor.class);

    @Resource
    private KnowledgeIngestionService ingestionService;

    /**
     * 提交文档处理任务。异常在任务内部已捕获落库,此处仅兜底日志。
     *
     * @param documentId 文档 id
     */
    public void submit(Long documentId) {
        boolean accepted = AsyncManager.me().executeVirtualTask(AsyncTaskGroup.KNOWLEDGE_INGEST, () -> {
            try {
                ingestionService.process(documentId);
            } catch (Exception e) {
                log.error("文档处理任务异常: documentId={}", documentId, e);
            }
        });
        if (!accepted) {
            log.warn("文档处理任务被拒绝(在途上限已满),将由补偿定时任务兜底: documentId={}", documentId);
        }
    }

    /**
     * 当前在途任务剩余名额(供监控/定时任务判断是否继续投递)。
     * 返回-1表示分组未初始化或无限制。
     */
    public int availableInFlight() {
        return AsyncManager.me().getAvailableInFlight(AsyncTaskGroup.KNOWLEDGE_INGEST.getGroupName());
    }

}
