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
 * 用于动态创建和管理操作日志表(含主表与载荷附表)
 *
 * <p>主表与附表按天分表,生命周期完全一致,同步建/删。</p>
 *
 * @author Ivan Shen
 */
@DS("sys")
@Service
public class LogTableManagementService {

    private static final String TABLE_NAME_PREFIX = "sys_oper_log_";
    private static final String PAYLOAD_TABLE_NAME_PREFIX = "sys_oper_log_payload_";

    @Autowired
    private MySQLMapper mySQLMapper;

    /**
     * 创建明天的操作日志表
     */
    public void createNextDayLogTable() {
        // 获取明天的日期
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String suffix = tomorrow.format(DateTimeFormatter.ofPattern("yyyy_MM_dd"));
        String mainTable = TABLE_NAME_PREFIX + suffix;
        String payloadTable = PAYLOAD_TABLE_NAME_PREFIX + suffix;

        if (mySQLMapper.checkTableExists(mainTable) == 0) {
            mySQLMapper.createOperLogTable(mainTable);
            System.out.println("成功创建主表: " + mainTable);
        } else {
            System.out.println("主表已存在，跳过创建: " + mainTable);
        }
        if (mySQLMapper.checkTableExists(payloadTable) == 0) {
            mySQLMapper.createOperLogPayloadTable(payloadTable);
            System.out.println("成功创建附表: " + payloadTable);
        } else {
            System.out.println("附表已存在，跳过创建: " + payloadTable);
        }
    }

    /**
     * 补偿当日的操作日志表
     */
    @Async
    public void ensureTodayLogTableExists() {
        // 获取当天的日期
        LocalDate today = LocalDate.now();
        String suffix = today.format(DateTimeFormatter.ofPattern("yyyy_MM_dd"));
        String mainTable = TABLE_NAME_PREFIX + suffix;
        String payloadTable = PAYLOAD_TABLE_NAME_PREFIX + suffix;

        if (mySQLMapper.checkTableExists(mainTable) == 0) {
            mySQLMapper.createOperLogTable(mainTable);
            System.out.println("成功创建主表: " + mainTable);
        }
        if (mySQLMapper.checkTableExists(payloadTable) == 0) {
            mySQLMapper.createOperLogPayloadTable(payloadTable);
            System.out.println("成功创建附表: " + payloadTable);
        }
    }

    /**
     * 清理超过7天的日志表(主表与附表)
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
            System.out.println("成功删除主表: " + tableName);
            // 同步删除对应的附表(命名规则:sys_oper_log_<date> -> sys_oper_log_payload_<date>)
            String suffix = tableName.substring(TABLE_NAME_PREFIX.length());
            String payloadTable = PAYLOAD_TABLE_NAME_PREFIX + suffix;
            mySQLMapper.dropOperLogTable(payloadTable);
            System.out.println("成功删除附表: " + payloadTable);
        }
    }
}