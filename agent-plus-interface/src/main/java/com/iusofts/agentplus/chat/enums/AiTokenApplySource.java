package com.iusofts.agentplus.chat.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI令牌申请来源
 */
public enum AiTokenApplySource {

    DEFAULT(0, "默认", 30),
    ;

    private static Map<Integer, AiTokenApplySource> map = Arrays.stream(AiTokenApplySource.values()).
            collect(Collectors.toMap(AiTokenApplySource::getCode, e -> e));

    private int code;
    private String detail;
    private int rateLimitSeconds;

    AiTokenApplySource(int code, String detail, int rateLimitSeconds) {
        this.code = code;
        this.detail = detail;
        this.rateLimitSeconds = rateLimitSeconds;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public int getRateLimitSeconds() {
        return rateLimitSeconds;
    }

    public void setRateLimitSeconds(int rateLimitSeconds) {
        this.rateLimitSeconds = rateLimitSeconds;
    }

    public static AiTokenApplySource getByCode(int value) {
        return map.get(value);
    }
}
