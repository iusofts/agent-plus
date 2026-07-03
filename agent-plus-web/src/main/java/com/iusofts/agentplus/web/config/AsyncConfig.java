package com.iusofts.agentplus.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync // 必须开启异步注解支持
public class AsyncConfig {

    /**
     * 定义异步任务执行器（虚拟线程版）
     * Bean 名称默认是方法名，也可以指定为 "taskExecutor"（Spring 默认查找的名称）
     */
    @Bean("taskExecutor") // 名称对应 Spring 默认查找的 "taskExecutor"
    public Executor asyncVirtualThreadExecutor() {
        // 方式1：基于虚拟线程的 TaskExecutor（推荐，Java 21+）
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心：使用虚拟线程工厂创建线程
        executor.setThreadFactory(Executors.defaultThreadFactory()); // 兼容写法
        // 进阶：直接指定虚拟线程工厂（Java 21+ 专属）
        executor.setThreadFactory(Thread.ofVirtual().factory());
        
        // 可选配置（根据业务调整）
        executor.setThreadNamePrefix("async-virtual-thread-"); // 线程名前缀，便于排查
        executor.setWaitForTasksToCompleteOnShutdown(true); // 关闭时等待任务完成
        executor.setAwaitTerminationSeconds(30); // 等待终止的超时时间
        
        executor.initialize(); // 初始化线程池（必须调用）
        return executor;
    }
}