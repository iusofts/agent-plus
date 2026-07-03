package com.iusofts.agentplus.web.common.util;

import com.iusofts.agentplus.basic.web.BeanLocatorUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

import java.time.Duration;

/**
 * 图形验证码工具
 *
 * @author Ivan
 * @date 2022/01/18
 */
public class CodesUtil {

    public static final String SALER_CODES = "MARKETING_SALER_CODES_";
    private static RedissonClient redissonClient = BeanLocatorUtil.getBean(RedissonClient.class);

    public static String getCodes(String token) {
        return (String) redissonClient.getBucket(SALER_CODES + token, StringCodec.INSTANCE).get();
    }

    public static void setCodes(String token, String code) {
        if (StringUtils.isNotBlank(token) && StringUtils.isNotBlank(code)) {
            redissonClient.getBucket(SALER_CODES + token, StringCodec.INSTANCE)
                    .set(code, Duration.ofSeconds(60 * 5));
        }
    }

    public static void invalidate(String token) {
        if (StringUtils.isNotBlank(token)) {
            redissonClient.getKeys().delete(SALER_CODES + token);
        }
    }
}
