package com.iusofts.agentplus.system.listener;

import com.iusofts.agentplus.system.service.LogTableManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 应用程序启动监听器
 * 在应用完全启动后执行日志表检查和清理任务
 */
@Slf4j
@Component
public class ApplicationStartupListener {

    @Autowired
    private LogTableManagementService logTableManagementService;

    /**
     * 应用程序就绪事件处理方法
     * 在应用完全启动后执行检查当日日志表和清理历史表的任务
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("应用程序已启动完成，开始执行日志表检查和清理任务...");

        // 异步执行当日日志表检查和创建
        logTableManagementService.ensureTodayLogTableExists();

        // 异步执行历史表清理
        logTableManagementService.cleanupOldLogTables();

        log.info("日志表检查和清理任务已完成");
    }
}