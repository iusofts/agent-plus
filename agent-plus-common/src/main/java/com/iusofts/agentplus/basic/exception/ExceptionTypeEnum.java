package com.iusofts.agentplus.basic.exception;

import java.util.Arrays;
import java.util.List;


public enum ExceptionTypeEnum {
    /**
     * 业务异常
     */
    BUSINESS_EXCEPTION(1000, "业务异常"),

    AUTH_MISSING_EXCEPTION(4000, "缺少auth信息"),

    /**
     * 用户状态异常
     */
    USER_STATUS_EXCEPTION(4002, "用户状态异常"),

    /**
     * 登录异常
     */
    LOGIN_INVALID_EXCEPTION(4001, "登录异常"),

    TOKEN_INVALID_EXCEPTION(4003, "token无效或已过期"),

    LICENSE_ACTIVE_FAILURE_EXCEPTION(4004, "license激活失败"),

    /**
     * 访问权限异常
     */
    INVALID_PERMISSION_EXCEPTION(5000, "访问权限异常"),

    /**
     * 参数异常
     */
    ILLEGAL_ARGUMENT_EXCEPTION(6000, "参数异常"),

    /**
     * 网络异常
     */
    NETWORK_EXCEPTION(7000, "网络异常"),

    /**
     * 未知异常
     */
    UNKNOWN_EXCEPTION(10000, "该功能暂时不可用，请您稍后再试，如有需要请联系管理员。");

    private Integer code;
    private String detail;

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

    private ExceptionTypeEnum(Integer code, String detail) {
        this.code = code;
        this.detail = detail;
    }

    public static String getName(Integer code, String detail) {
        for (ExceptionTypeEnum exceptionTypeEnum : ExceptionTypeEnum.values()) {
            if (exceptionTypeEnum.getCode().equals(code)) {
                return exceptionTypeEnum.getDetail();
            }
        }
        return null;
    }

    public static boolean contains(Integer code) {
        for (ExceptionTypeEnum exceptionTypeEnum : ExceptionTypeEnum.values()) {
            if (exceptionTypeEnum.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> getNameList() {
        return Arrays.asList("BusinessException", "UserStatusExceptoin", "InvalidPermissionException", "IllegalArgumentException");
    }
}
