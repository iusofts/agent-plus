package com.iusofts.system.service;

import common.BaseTest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class LogTableManagementServiceTest extends BaseTest {

    @Resource
    private LogTableManagementService logTableManagementService;

    @Test
    public void createNextDayLogTable() {
        log.info("开始执行创建第二天操作日志表及清理过期表的任务");

        // 创建明天的操作日志表
        logTableManagementService.createNextDayLogTable();

        // 清理超过7天的旧表
        logTableManagementService.cleanupOldLogTables();

        log.info("完成创建第二天操作日志表及清理过期表的任务");
    }

    @Test
    public void ensureTodayLogTableExists() {
        log.info("开始执行检查当日操作日志表存在的任务");

        // 检查当日日志表是否存在，如果不存在则创建
        logTableManagementService.ensureTodayLogTableExists();

        log.info("完成检查当日操作日志表存在的任务");
    }
    
}