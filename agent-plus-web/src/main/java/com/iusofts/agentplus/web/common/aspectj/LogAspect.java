package com.iusofts.agentplus.web.common.aspectj;

import com.alibaba.fastjson.JSON;
import com.iusofts.agentplus.basic.enums.AsyncTaskGroup;
import com.iusofts.agentplus.basic.web.ApiUtil;
import com.iusofts.agentplus.basic.web.annotation.OperationLogExclude;
import com.iusofts.agentplus.basic.enums.BusinessStatus;
import com.iusofts.agentplus.basic.enums.OperationLogExcludeTypeEnums;
import com.iusofts.agentplus.basic.enums.OperatorType;
import com.iusofts.agentplus.basic.web.ip.IpUtils;
import com.iusofts.agentplus.basic.thread.AsyncManager;
import com.iusofts.agentplus.system.dto.OperLogAddParam;
import com.iusofts.agentplus.system.vo.BLoginUserVo;
import com.iusofts.agentplus.basic.web.vo.CLoginUserVo;
import com.iusofts.agentplus.web.common.manager.AsyncFactory;
import com.iusofts.agentplus.basic.web.ServletUtils;
import com.iusofts.agentplus.web.common.util.SessionUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * 操作日志记录处理
 *
 * @author 
 */
@Aspect
@Component
public class LogAspect {
    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    // 配置织入点
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void logPointCut() {
    }

    /**
     * 处理开始之前
     *
     * @param joinPoint
     */
    @Before("logPointCut()")
    public void doBefore(JoinPoint joinPoint) {
        OperationTimeContextHolder.setOperationTime(LocalDateTime.now());
    }

    /**
     * 处理完请求后执行
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "logPointCut()", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Object jsonResult) {
        handleLog(joinPoint, null, jsonResult);
    }

    /**
     * 拦截异常操作
     *
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(value = "logPointCut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        handleLog(joinPoint, e, null);
    }

    protected void handleLog(final JoinPoint joinPoint, final Exception e, Object jsonResult) {
        try {
            // 获得注解
            PostMapping controllerLog = getAnnotationLog(joinPoint);
            if (controllerLog == null) {
                return;
            }

            OperationLogExclude logExclude = getAnnotationLogExclude(joinPoint);

            String requestURI = ServletUtils.getRequest().getRequestURI();
            if (logExclude != null && logExclude.type() == OperationLogExcludeTypeEnums.URI) {
                return;
            }

            // *========数据库日志=========*//
            OperLogAddParam operLog = new OperLogAddParam();
            // 请求开始时间
            LocalDateTime operationStartTime = OperationTimeContextHolder.getOperationTime();
            operLog.setOperTime(operationStartTime);
            OperationTimeContextHolder.clearOperationTime();
            // 请求结束时间
            LocalDateTime operationEndTime = LocalDateTime.now();
            // 消耗时长(毫秒)
            operLog.setExecuteTime((int) ChronoUnit.MILLIS.between(operationStartTime, operationEndTime));

            operLog.setStatus(BusinessStatus.SUCCESS.ordinal());
            // 请求的地址
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            operLog.setOperIp(ip);
            // 返回参数
            if (logExclude == null || logExclude.type() != OperationLogExcludeTypeEnums.RES) {
                operLog.setJsonResult(JSON.toJSONString(jsonResult));
            }
            operLog.setOperUrl(requestURI);

            // 获取当前的用户
            // 如果当前用户为空 判断是否为后台用户
            BLoginUserVo bSessionUser = SessionUtil.getBSessionUser();
            if (bSessionUser != null) {
                operLog.setOperName(bSessionUser.getUser().getName());
                operLog.setUserId(bSessionUser.getUser().getUserId());
                operLog.setOperatorType(OperatorType.MANAGE.getCode());
                operLog.setToken(SessionUtil.getToken());
            }

            CLoginUserVo apiUser = ApiUtil.getApiUser();
            if (apiUser != null) {
                operLog.setOperName(apiUser.getName());
                operLog.setUserId(apiUser.getId());
                operLog.setOperatorType(OperatorType.CLIENT.getCode());
                operLog.setToken(ApiUtil.getToken());
            }

            if (bSessionUser == null && apiUser == null) {
                // 未登录日志暂不纪录
                return;
            }

            if (e != null) {
                operLog.setStatus(BusinessStatus.FAIL.ordinal());
                operLog.setErrorMsg(ExceptionUtils.getStackTrace(e));
            }
            // 设置方法名称
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            operLog.setMethod(className + "." + methodName + "()");
            // 设置请求方式
            operLog.setRequestMethod(ServletUtils.getRequest().getMethod());
            // 处理设置注解上的参数
            getControllerMethodDescription(joinPoint, operLog);
            // 保存数据库
            AsyncManager.me().executeVirtualTask(AsyncTaskGroup.OPERATION_LOG, AsyncFactory.recordOperVirtual(operLog));
        } catch (Exception exp) {
            // 记录本地异常日志
            log.error("==前置通知异常==");
            log.error("异常信息:{}", exp.getMessage());
            exp.printStackTrace();
        }
    }

    /**
     * 获取注解中对方法的描述信息 用于Controller层注解
     *
     * @param operLog 操作日志
     * @throws Exception
     */
    public void getControllerMethodDescription(JoinPoint joinPoint, OperLogAddParam operLog) throws Exception {
        // 设置action动作
        operLog.setBusinessType(null);
        // 设置标题：取类上的@Tag注解的name + 方法上的@Operation注解的description作为标题
        Tag classTag = getAnnotationClassTag(joinPoint);
        Operation apiOperation = getAnnotationTitle(joinPoint);

        StringBuilder titleBuilder = new StringBuilder();
        if (classTag != null && classTag.name() != null && !classTag.name().isEmpty()) {
            titleBuilder.append("[").append(classTag.name()).append("] ");
        }
        if (apiOperation != null && apiOperation.description() != null && !apiOperation.description().isEmpty()) {
            titleBuilder.append(apiOperation.description());
        }

        operLog.setTitle(titleBuilder.toString());

        OperationLogExclude logExclude = getAnnotationLogExclude(joinPoint);
        // 是否需要保存request，参数和值
        if (logExclude == null || logExclude.type() != OperationLogExcludeTypeEnums.REQ) {
            // 获取参数的信息，传入到数据库中。
            setRequestValue(joinPoint, operLog);
        }
    }

    /**
     * 获取请求的参数，放到log中
     *
     * @param operLog 操作日志
     * @throws Exception 异常
     */
    private void setRequestValue(JoinPoint joinPoint, OperLogAddParam operLog) throws Exception {
        String requestMethod = operLog.getRequestMethod();
        if (HttpMethod.PUT.name().equals(requestMethod) || HttpMethod.POST.name().equals(requestMethod)) {
            String params = argsArrayToString(joinPoint.getArgs());
            operLog.setOperParam(params);
        } else {
            Map<?, ?> paramsMap = (Map<?, ?>) ServletUtils.getRequest().getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            operLog.setOperParam(paramsMap.toString());
        }
    }

    /**
     * 是否存在注解，如果存在就获取
     */
    private PostMapping getAnnotationLog(JoinPoint joinPoint) throws Exception {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();

        if (method != null) {
            return method.getAnnotation(PostMapping.class);
        }
        return null;
    }

    /**
     * 是否存在注解，如果存在就获取
     */
    private Tag getAnnotationClassTag(JoinPoint joinPoint) throws Exception {
        Class<?> clazz = joinPoint.getTarget().getClass();

        if (clazz != null) {
            return clazz.getAnnotation(Tag.class);
        }
        return null;
    }

    /**
     * 是否存在注解，如果存在就获取
     */
    private Operation getAnnotationTitle(JoinPoint joinPoint) throws Exception {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();

        if (method != null) {
            return method.getAnnotation(Operation.class);
        }
        return null;
    }

    /**
     * 是否存在注解，如果存在就获取
     */
    private OperationLogExclude getAnnotationLogExclude(JoinPoint joinPoint) throws Exception {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();

        if (method != null) {
            return method.getAnnotation(OperationLogExclude.class);
        }
        return null;
    }

    /**
     * 参数拼装
     */
    private String argsArrayToString(Object[] paramsArray) {
        String params = "";
        if (paramsArray != null && paramsArray.length > 0) {
            for (int i = 0; i < paramsArray.length; i++) {
                if (!isFilterObject(paramsArray[i])) {
                    Object jsonObj = JSON.toJSON(paramsArray[i]);
                    if(jsonObj != null) {
                        params += jsonObj + " ";
                    }
                }
            }
        }
        return params.trim();
    }

    /**
     * 判断是否需要过滤的对象。
     *
     * @param o 对象信息。
     * @return 如果是需要过滤的对象，则返回true；否则返回false。
     */
    public boolean isFilterObject(final Object o) {
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse;
    }
}
