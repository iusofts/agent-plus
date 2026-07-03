package com.iusofts.aiflow.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 发布状态枚举
 */
public enum PublishingStatusEnum {

    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    PENDING_REVIEW(2, "待审核");

    private static Map<Integer, PublishingStatusEnum> map = Arrays.stream(PublishingStatusEnum.values()).
            collect(Collectors.toMap(PublishingStatusEnum::getCode, e -> e));

    private Integer code;
    private String detail;

    PublishingStatusEnum(Integer code, String detail) {
        this.code = code;
        this.detail = detail;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public static PublishingStatusEnum getByCode(Integer value) {
        return map.get(value);
    }
}
