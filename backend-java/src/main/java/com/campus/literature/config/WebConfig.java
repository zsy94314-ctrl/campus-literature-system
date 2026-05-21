package com.campus.literature.config;

import com.campus.literature.security.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 所有 /api/** 请求都经过 JwtInterceptor，由拦截器内部判断是否为公开接口
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**");
    }
}
