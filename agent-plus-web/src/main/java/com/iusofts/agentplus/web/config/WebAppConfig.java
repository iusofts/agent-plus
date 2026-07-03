package com.iusofts.agentplus.web.config;

import com.iusofts.agentplus.web.common.resolver.BLoginUserArgumentResolver;
import com.iusofts.agentplus.web.common.resolver.CLoginUserArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 注册拦截器
 */
@Configuration
public class WebAppConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //注册自定义拦截器，添加拦截路径和排除拦截路径
        registry.addInterceptor(new InterceptorConfig())
                .addPathPatterns("/**")
                .excludePathPatterns("/error")
                .excludePathPatterns("/index.html")
                .excludePathPatterns("/swagger-resources/**")
                .excludePathPatterns("/v3/api-docs/**")
                .excludePathPatterns("/webjars/**")
                .excludePathPatterns("/swagger-ui.html")
                .excludePathPatterns("/swagger-ui/**")
                .excludePathPatterns("/doc.html")
                .excludePathPatterns("/bapi/login")
                .excludePathPatterns("/bapi/oss/getOssToken")
                .excludePathPatterns("/bapi/imageVerifyCode")
                .excludePathPatterns("/bapi/appVersion/**")
                .excludePathPatterns("/bapi/bsStatistics/**")
                .excludePathPatterns("/api/**");
        

    }

    /**
     * 注册自定义参数解析器
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new BLoginUserArgumentResolver());
        resolvers.add(new CLoginUserArgumentResolver());
    }

}
