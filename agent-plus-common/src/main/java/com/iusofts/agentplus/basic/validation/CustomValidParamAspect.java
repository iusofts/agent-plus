package com.iusofts.agentplus.basic.validation;

import com.iusofts.agentplus.basic.exception.InvalidParamException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 专门处理对象参数的@ApValidated校验
 */
@Aspect
@Component
public class CustomValidParamAspect {

    /**
     * 切点：拦截参数带@ApValidated的方法（重点处理对象参数）
     */
    @Before("execution(* *(.., @com.iusofts.agentplus.basic.validation.ApValidated (*), ..))")
    public void validateObjectParam(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        // 遍历所有参数，找到标注了@YzValidated的对象参数
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            ApValidated annotation = param.getAnnotation(ApValidated.class);
            if (annotation != null) {
                Object paramValue = args[i];
                // 1. 空值校验：如果对象本身为null，直接抛出异常
                if (paramValue == null) {
                    throw new InvalidParamException("参数不能为空");
                }

                // 2. 深度校验对象字段（核心：校验对象内的所有注解字段）
                ValidationUtils.validate(paramValue);
            }
        }
    }
}