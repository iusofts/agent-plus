package com.iusofts.agentplus.web.common.aspectj;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * 操作时间上下文线程变量
 *
 * @author Ivan
 */
@Slf4j
public class OperationTimeContextHolder {

    private static final ThreadLocal<LocalDateTime> CONTEXT_HOLDER = new ThreadLocal<>();

    public static void setOperationTime(LocalDateTime operationTime) {
        CONTEXT_HOLDER.set(operationTime);
    }

    public static LocalDateTime getOperationTime() {
        return CONTEXT_HOLDER.get();
    }

    public static void clearOperationTime() {
        CONTEXT_HOLDER.remove();
    }

}
