package com.iusofts.agentplus.basic.enums;

/**
 * 异步任务分组枚举
 */
public enum AsyncTaskGroup {
    /**
     * 操作日志任务组
     */
    OPERATION_LOG("OPERATION_LOG", 5, ExecutionStrategy.TRY_ACQUIRE_TIMEOUT, 5),

    /**
     * 数据库读取任务组
     */
    DB_READ("DB_READ", 4, ExecutionStrategy.BLOCKING),

    /**
     * 数据库写入任务组
     */
    DB_WRITE("DB_WRITE", 4, ExecutionStrategy.BLOCKING),

    /**
     * 文件上传任务组
     */
    FILE_UPLOAD("FILE_UPLOAD", 10, ExecutionStrategy.BLOCKING),

    /**
     * 邮件发送任务组
     */
    EMAIL_SEND("EMAIL_SEND", 5, ExecutionStrategy.BLOCKING, 10),

    /**
     * 短信发送任务组
     */
    SMS_SEND("SMS_SEND", 10, ExecutionStrategy.BLOCKING, 10),

    /**
     * 客户端消息推送任务组
     */
    APP_PUSH("APP_PUSH", 100, ExecutionStrategy.BLOCKING, 10),

    /**
     * 自定义任务组
     */
    CUSTOM("CUSTOM", 15, ExecutionStrategy.TRY_ACQUIRE_TIMEOUT, 5);

    private final String groupName;
    private final int defaultLimit;
    private final ExecutionStrategy executionStrategy;
    private int timeoutSeconds;

    AsyncTaskGroup(String groupName, int defaultLimit, ExecutionStrategy executionStrategy) {
        this.groupName = groupName;
        this.defaultLimit = defaultLimit;
        this.executionStrategy = executionStrategy;
        this.timeoutSeconds = 5; // 默认超时时间为5秒
    }

    AsyncTaskGroup(String groupName, int defaultLimit, ExecutionStrategy executionStrategy, int timeoutSeconds) {
        this.groupName = groupName;
        this.defaultLimit = defaultLimit;
        this.executionStrategy = executionStrategy;
        this.timeoutSeconds = timeoutSeconds;
    }

    public String getGroupName() {
        return groupName;
    }

    public int getDefaultLimit() {
        return defaultLimit;
    }

    public ExecutionStrategy getExecutionStrategy() {
        return executionStrategy;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }
}