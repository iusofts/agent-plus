package com.iusofts.config;

import com.iusofts.basic.enums.AsyncTaskGroup;
import com.iusofts.basic.thread.AsyncManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 异步任务并发配置初始化器
 */
@Slf4j
@Component
public class AsyncTaskConcurrencyConfig implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        AsyncManager manager = AsyncManager.me();

        // 由于在AsyncManager构造函数中已经设置了默认值，这里可以根据需要进行调整
        log.info("Async task configurations loaded with default values:");

        // 遍历所有任务组并打印配置信息
        for (AsyncTaskGroup group : AsyncTaskGroup.values()) {
            if (group != AsyncTaskGroup.CUSTOM) {  // CUSTOM是预留的，不显示在初始化日志中
                log.info("{}: {} permits, Strategy: {}, Timeout: {}s",
                    group.getGroupName(),
                    manager.getAvailablePermits(group),
                    manager.getCurrentExecutionStrategy(group),
                    manager.getCurrentTimeoutSeconds(group));
            }
        }
    }
}