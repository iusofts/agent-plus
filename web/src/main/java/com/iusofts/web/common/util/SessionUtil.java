package com.iusofts.web.common.util;

import com.iusofts.basic.utils.BeanLocatorUtil;
import com.iusofts.basic.utils.JsonUtils;
import com.iusofts.basic.utils.StringUtils;
import com.iusofts.basic.web.ServletUtils;
import com.iusofts.system.vo.BLoginUserVo;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

import java.util.concurrent.TimeUnit;

import static com.iusofts.basic.constants.CacheConstants.LOGIN_TOKEN_HEAD;
import static com.iusofts.basic.constants.CacheConstants.LOGIN_TOKEN_KEY;

/**
 * 当前已登录用户Session工具
 *
 * @author Ivan
 */
public class SessionUtil {

    private static RedissonClient redissonClient = BeanLocatorUtil.getBean(RedissonClient.class);

    public static String getUsername() {
        BLoginUserVo bSessionUser = getBSessionUser();
        return bSessionUser.getUser().getUsername();
    }

    public static Long getUserId() {
        BLoginUserVo bSessionUser = getBSessionUser();
        return bSessionUser.getUser().getUserId();
    }

    public static BLoginUserVo getBSessionUser() {
        String token = getToken();
        BLoginUserVo bLoginUserVo = null;
        String value = (String) redissonClient.getBucket(LOGIN_TOKEN_KEY + token, StringCodec.INSTANCE).get();
        if (StringUtils.isNotBlank(value)) {
            bLoginUserVo = JsonUtils.json2obj(value, BLoginUserVo.class);
        }
        if (bLoginUserVo != null) {
            redissonClient.getKeys().expire(LOGIN_TOKEN_KEY + token, 60 * 60 * 24 * 3, TimeUnit.SECONDS);
        }
        return bLoginUserVo;
    }

    public static void invalidate() {
        redissonClient.getKeys().delete(LOGIN_TOKEN_KEY + getToken());
    }

    public static String getToken() {
        String token = ServletUtils.getRequest().getHeader(LOGIN_TOKEN_HEAD);
        /*Cookie[] cookies = ServletUtils.getRequest().getCookies();
        String token = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("token")) {
                    token = cookie.getValue();
                }
            }
        }*/
        return token;
    }
}