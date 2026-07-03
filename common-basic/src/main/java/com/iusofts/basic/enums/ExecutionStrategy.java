package com.iusofts.basic.enums;

/**
 * 异步任务执行策略枚举
 */
public enum ExecutionStrategy {
    /**
     * 阻塞模式：任务会一直等待直到获取到许可
     */
    BLOCKING,

    /**
     * 超时获取模式：任务在指定时间内尝试获取许可，超时则跳过任务
     */
    TRY_ACQUIRE_TIMEOUT
}