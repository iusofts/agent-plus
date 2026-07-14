package com.iusofts.agentplus.web.monitor.controller;

import com.iusofts.agentplus.basic.utils.StringUtils;
import com.iusofts.agentplus.basic.web.annotation.OperationLogExclude;
import com.iusofts.agentplus.basic.web.annotation.Permission;
import com.iusofts.agentplus.web.common.controller.BApiController;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import static com.iusofts.agentplus.basic.enums.OperationLogExcludeTypeEnums.RES;

/**
 * 缓存监控
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/bapi/monitor/cache")
public class CacheController extends BApiController {

    private final RedissonConnectionFactory connectionFactory;

    /**
     * 获取缓存监控列表
     */
    @Permission("monitor:cache:list")
    @OperationLogExclude(type = RES)
    @Operation(summary = "获取缓存监控列表")
    @GetMapping()
    public CacheListInfoVo getInfo() throws Exception {
        RedisConnection connection = connectionFactory.getConnection();
        try {
            Properties commandStats = connection.commands().info("commandstats");
            List<Map<String, String>> pieList = new ArrayList<>();
            if (commandStats != null) {
                commandStats.stringPropertyNames().forEach(key -> {
                    Map<String, String> data = new HashMap<>(2);
                    String property = commandStats.getProperty(key);
                    data.put("name", StringUtils.removeStart(key, "cmdstat_"));
                    data.put("value", StringUtils.substringBetween(property, "calls=", ",usec"));
                    pieList.add(data);
                });
            }
            return new CacheListInfoVo(
                connection.commands().info(),
                connection.commands().dbSize(), pieList);
        } finally {
            // 归还连接给连接池
            RedisConnectionUtils.releaseConnection(connection, connectionFactory);
        }
    }

    /**
     * 缓存监控列表信息
     *
     * @param info         信息
     * @param dbSize       数据库
     * @param commandStats 命令统计
     */
    public record CacheListInfoVo(Properties info, Long dbSize, List<Map<String, String>> commandStats) {}

}
