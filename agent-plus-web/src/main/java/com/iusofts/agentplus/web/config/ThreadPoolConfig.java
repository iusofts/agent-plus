package com.iusofts.agentplus.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * 线程池配置
 *
 * @author
 **/
@Configuration
public class ThreadPoolConfig {

    /**
     * 执行周期性或定时任务 - 虚拟线程版本 (JDK 21+)
     */
    @Bean(name = "virtualScheduledExecutorService")
    protected ScheduledExecutorService virtualScheduledExecutorService() {
        // 创建使用虚拟线程的调度执行器 (JDK 21+)
        ThreadFactory virtualThreadFactory = Thread.ofVirtual()
                .name("virtual-schedule-pool-")
                .factory();
        return Executors.newScheduledThreadPool(0, virtualThreadFactory);
    }
}
