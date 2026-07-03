package com.iusofts.web.common.aspectj;

import com.iusofts.basic.web.ApiUtil;
import com.iusofts.basic.annotation.BLoginUser;
import com.iusofts.basic.annotation.CLoginUser;
import com.iusofts.web.common.util.SessionUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 登录用户参数注入
 */
@Aspect
@Component
public class LoginUserParamAspect {

    @Before("execution(* *(.., @com.iusofts.basic.annotation.CLoginUser (*), ..))")
    public void cLoginUserParam(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        // 遍历所有参数，找到标注了@CustomValidParam的对象参数
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            CLoginUser annotation = param.getAnnotation(CLoginUser.class);
            if (annotation != null) {
                args[i] = ApiUtil.getApiUser();
            }
        }
    }

    @Before("execution(* *(.., @com.iusofts.basic.annotation.BLoginUser (*), ..))")
    public void bLoginUserParam(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        // 遍历所有参数，找到标注了@CustomValidParam的对象参数
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            BLoginUser annotation = param.getAnnotation(BLoginUser.class);
            if (annotation != null) {
                args[i] = SessionUtil.getBSessionUser();
            }
        }
    }
}