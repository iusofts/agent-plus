package com.iusofts.agentplus.web.common.resolver;

import com.iusofts.agentplus.basic.annotation.BLoginUser;
import com.iusofts.agentplus.system.vo.BLoginUserVo;
import com.iusofts.agentplus.web.common.util.SessionUtil;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 解析@LoginUser注解的参数，返回AOP植入的登录用户信息
 */
public class BLoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * 判断是否支持解析该参数
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(BLoginUser.class)
                && parameter.getParameterType().equals(BLoginUserVo.class);
    }

    /**
     * 解析参数：从AOP上下文获取CLoginUserVo对象返回
     */
    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        return SessionUtil.getBSessionUser();
    }
}