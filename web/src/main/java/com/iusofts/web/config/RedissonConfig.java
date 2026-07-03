package com.iusofts.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.database}")
    private int database;
    
    @Resource
    private ObjectMapper objectMapper;

    /**
     * Redisson 客户端配置（单节点模式）
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        // Redis 连接地址
        String address = "redis://" + redisHost + ":" + redisPort;

        // 单节点配置
        SingleServerConfig serverConfig = config.useSingleServer()
                .setAddress(address)
                .setDatabase(database)
                // 连接池核心参数
                .setConnectionMinimumIdleSize(2)
                .setConnectionPoolSize(24)
                .setIdleConnectionTimeout(10000)
                .setConnectTimeout(3000)
                .setTimeout(3000);

        // 仅密码非空时设置
        if (StringUtils.isNotBlank(redisPassword)) {
            serverConfig.setPassword(redisPassword);
        }

        config.setCodec(new JsonJacksonCodec(objectMapper));
        return Redisson.create(config);
    }
}