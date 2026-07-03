/*
 * Copyright (C) 2024 All rights reserved
 * Author: Ivan Shen
 * Date: 2026-05-08
 * Description:AiTokenServiceImpl.java
 */
package com.iusofts.agentplus.ai.service;

import com.iusofts.agentplus.ai.interfaces.IAiTokenService;
import com.iusofts.agentplus.ai.enums.AiTokenApplySource;
import com.iusofts.agentplus.ai.vo.AiTokenApplyReqVo;
import com.iusofts.agentplus.ai.vo.AiTokenVo;
import jakarta.annotation.Resource;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Ivan Shen
 */
@Service
public class AiTokenServiceImpl implements IAiTokenService {

    @Resource
    private RedissonClient redissonClient;

    private static final String RATE_LIMIT_PREFIX = "ai:token:rate:";
    private static final String TOKEN_PREFIX = "ai:token:access:";
    private static final long TOKEN_EXPIRE_SECONDS = 300;

    @Override
    public AiTokenVo applyToken(AiTokenApplyReqVo reqVo) {
        AiTokenVo result = new AiTokenVo();
        String rateKey = RATE_LIMIT_PREFIX + reqVo.getOrgId() + ":" + reqVo.getOperatorId() + ":" + reqVo.getSource();

        RBucket<String> rateBucket = redissonClient.getBucket(rateKey);
        if (rateBucket.isExists()) {
            result.setSuccess(false);
            return result;
        }

        AiTokenApplySource source = AiTokenApplySource.getByCode(reqVo.getSource());
        long rateLimitSeconds = source != null ? source.getRateLimitSeconds() : 60;
        rateBucket.set("1", rateLimitSeconds, TimeUnit.SECONDS);

        String token = UUID.randomUUID().toString().replace("-", "");
        String tokenKey = TOKEN_PREFIX + token;
        RBucket<String> tokenBucket = redissonClient.getBucket(tokenKey);
        tokenBucket.set(token, TOKEN_EXPIRE_SECONDS, TimeUnit.SECONDS);

        result.setSuccess(true);
        result.setAccessToken(token);
        return result;
    }

    @Override
    public boolean useToken(String accessToken) {
        String tokenKey = TOKEN_PREFIX + accessToken;
        RBucket<String> tokenBucket = redissonClient.getBucket(tokenKey);
        if (!tokenBucket.isExists()) {
            return false;
        }
        tokenBucket.delete();
        return true;
    }

}
