package com.iusofts.agentplus.system.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.iusofts.agentplus.system.dao.MySQLMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 日志表管理服务类
 * 用于动态创建和管理操作日志表
 *
 * @author Ivan Shen
 */
@DS("sys")
@Service
public class LogTableManagementService {

    private static final String TABLE_NAME_PREFIX = "sys_oper_log_";

    @Autowired
    private MySQLMapper mySQLMapper;

    /**
     * 创建明天的操作日志表
     */
    public void createNextDayLogTable() {
        // 获取明天的日期
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String tableName = TABLE_NAME_PREFIX + tomorrow.format(DateTimeFormatter.ofPattern("yyyy_MM_dd"));

        // 检查表是否存在
        if (mySQLMapper.checkTableExists(tableName) == 0) {
            // 创建表
            mySQLMapper.createOperLogTable(tableName);
            System.out.println("成功创建表: " + tableName);
        } else {
            System.out.println("表已存在，跳过创建: " + tableName);
        }
    }

    /**
     * 补偿当日的操作日志表
     */
    @Async
    public void ensureTodayLogTableExists() {
        // 获取明天的日期
        LocalDate tomorrow = LocalDate.now();
        String tableName = TABLE_NAME_PREFIX + tomorrow.format(DateTimeFormatter.ofPattern("yyyy_MM_dd"));

        // 检查表是否存在
        if (mySQLMapper.checkTableExists(tableName) == 0) {
            // 创建表
            mySQLMapper.createOperLogTable(tableName);
            System.out.println("成功创建表: " + tableName);
        }
    }

    /**
     * 清理超过7天的日志表
     */
    @Async
    public void cleanupOldLogTables() {
        // 获取7天前的日期
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        String cutoffDate = TABLE_NAME_PREFIX + sevenDaysAgo.format(DateTimeFormatter.ofPattern("yyyy_MM_dd"));

        // 获取所有过期的表
        List<String> expiredTables = mySQLMapper.getExpiredLogTables(TABLE_NAME_PREFIX + "%", cutoffDate);

        // 删除过期的表
        for (String tableName : expiredTables) {
            mySQLMapper.dropOperLogTable(tableName);
            System.out.println("成功删除表: " + tableName);
        }
    }
}