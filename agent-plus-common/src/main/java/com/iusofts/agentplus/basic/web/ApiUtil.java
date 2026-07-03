package com.iusofts.agentplus.basic.web;

import com.iusofts.agentplus.basic.utils.JsonUtils;
import com.iusofts.agentplus.basic.web.vo.CLoginUserVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

import java.util.concurrent.TimeUnit;

import static com.iusofts.agentplus.basic.constants.CacheConstants.C_LOGIN_TOKEN_HEAD;
import static com.iusofts.agentplus.basic.constants.CacheConstants.C_LOGIN_TOKEN_KEY;

/**
 * Api工具
 *
 * @author
 * @date 2019/11/6
 */
@Slf4j
public class ApiUtil {

    private static RedissonClient redissonClient = BeanLocatorUtil.getBean(RedissonClient.class);

    public static CLoginUserVo getApiUser() {
        String token = getToken();
        return getApiUser(token);
    }

    public static CLoginUserVo getApiUser(String token) {
        //log.info("token:{}", token);
        if (StringUtils.isBlank(token)) {
            return null;
        }

        CLoginUserVo loginInfoDto = null;
        String value = (String) redissonClient.getBucket(C_LOGIN_TOKEN_KEY + token, StringCodec.INSTANCE).get();
        if (StringUtils.isNotBlank(value)) {
            loginInfoDto = JsonUtils.json2obj(value, CLoginUserVo.class);
        }

        if (loginInfoDto != null) {
            redissonClient.getKeys().expire(token, 60 * 60 * 24 * 30, TimeUnit.SECONDS);
        }

        return loginInfoDto;
    }
    
    public static Integer getOrgId() {
        return getApiUser().getOrgId();
    }

    public static Long getUserId() {
        CLoginUserVo apiUser = getApiUser();
        return apiUser!=null ? apiUser.getId() : null;
    }

    public static void invalidate() {
        String token = getToken();
        if (StringUtils.isNotBlank(token)) {
            redissonClient.getKeys().delete(C_LOGIN_TOKEN_KEY + token);
        }
    }

    public static String getToken() {
        /*Enumeration<String> headerNames = ServletUtils.getRequest().getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = ServletUtils.getRequest().getHeader(key);
            log.info("header key:{},value:{}", key, value);
        }*/
        return ServletUtils.getRequest().getHeader(C_LOGIN_TOKEN_HEAD);
    }

}
