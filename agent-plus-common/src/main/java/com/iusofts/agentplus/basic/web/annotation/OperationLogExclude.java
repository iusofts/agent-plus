package com.iusofts.agentplus.basic.web.annotation;

import com.iusofts.agentplus.basic.enums.OperationLogExcludeTypeEnums;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志排除
 *
 * @author Ivan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OperationLogExclude {
    /**
     * 排除类型
     *
     * @return
     */
    OperationLogExcludeTypeEnums type() default OperationLogExcludeTypeEnums.URI;
}
