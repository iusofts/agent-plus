package com.iusofts.agentplus.web.common.resolver;

import com.iusofts.agentplus.basic.web.ApiUtil;
import com.iusofts.agentplus.basic.annotation.CLoginUser;
import com.iusofts.agentplus.basic.web.vo.CLoginUserVo;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 解析@LoginUser注解的参数，返回AOP植入的登录用户信息
 */
public class CLoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * 判断是否支持解析该参数（有@LoginUser注解且类型是BLoginUserVo）
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CLoginUser.class)
                && parameter.getParameterType().equals(CLoginUserVo.class);
    }

    /**
     * 解析参数：从AOP上下文获取BLoginUserVo对象返回
     */
    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        return ApiUtil.getApiUser();
    }
}