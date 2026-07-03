package com.iusofts.agentplus.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 核心修改：用allowedOriginPatterns替代allowedOrigins
        // 支持通配符（如*、http://*.yourdomain.com），且兼容allowCredentials=true
        config.addAllowedOriginPattern("*");
        
        // 允许携带凭证（保持true，业务需要的话）
        config.setAllowCredentials(true);
        
        // 允许的请求方法
        config.addAllowedMethod("*");
        
        // 允许的请求头
        config.addAllowedHeader("*");
        
        // 暴露的响应头（如需前端获取自定义头）
        config.addExposedHeader("Authorization");
        
        // 预检请求有效期（秒）
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有接口生效
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}