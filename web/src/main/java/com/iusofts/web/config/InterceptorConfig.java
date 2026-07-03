package com.iusofts.web.config;

import com.iusofts.basic.company.CompanyIdentityContextHolder;
import com.iusofts.basic.exception.SystemBusinessException;
import com.iusofts.basic.web.annotation.Permission;
import com.iusofts.system.vo.BLoginUserVo;
import com.iusofts.web.common.util.PermissionUtil;
import com.iusofts.web.common.util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Objects;


@Component
public class InterceptorConfig implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(InterceptorConfig.class);

    /**
     * 进入controller层之前拦截请求
     *
     * @param httpServletRequest
     * @param httpServletResponse
     * @param handler
     * @return
     */
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler) {
        log.debug("---------------------开始进入请求地址拦截----------------------------");
        BLoginUserVo bSessionUser = SessionUtil.getBSessionUser();
        if (Objects.isNull(bSessionUser)) {
            log.info("---------------------未登录拦截----------------------------");
            throw new SystemBusinessException(401, "用户未登录");
        }

        // 权限校验
        if (handler instanceof HandlerMethod handlerMethod) {
            Permission permission = handlerMethod.getMethodAnnotation(Permission.class);
            if (permission != null) {
                if (!PermissionUtil.checkPermission(permission)) {
                    log.info("---------------------权限不足拦截----------------------------");
                    throw new SystemBusinessException(403, "没有操作权限");
                }
            }
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        // 清空线程变量，防止内存泄漏
        CompanyIdentityContextHolder.clearCompanyId();
        log.debug("--------------处理请求完成后视图渲染之前的处理操作---------------");
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        log.debug("---------------视图渲染之后的操作-------------------------0");
    }

}
