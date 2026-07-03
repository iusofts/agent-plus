package com.iusofts.scheduled;

import com.iusofts.system.dao.MySQLMapper;
import com.iusofts.system.service.LogTableManagementService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableAsync
@EnableScheduling
public class DataSourceScheduled {

    @Value("${spring.profiles.active}")
    private String profies;

    @Resource
    private MySQLMapper mySQLMapper;

    @Resource
    private LogTableManagementService logTableManagementService;

    @Async
    @Scheduled(cron = "1 * * * * ?")
    public void activeDb() {
        if("dev".equals(profies)) {
            log.info("激活连接：" + profies);
            mySQLMapper.activeYz();
            mySQLMapper.activeSys();
        }
    }

    /**
     * 每天23点执行：创建第二天的操作日志表并清理过期表
     */
    @Scheduled(cron = "0 0 23 * * ?")
    public void createNextDayLogTableAndCleanup() {
        log.info("开始执行创建第二天操作日志表及清理过期表的任务");

        // 创建明天的操作日志表
        logTableManagementService.createNextDayLogTable();

        // 清理超过7天的旧表
        logTableManagementService.cleanupOldLogTables();

        log.info("完成创建第二天操作日志表及清理过期表的任务");
    }
}