package com.dp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;
import com.dp.utils.LoginInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .excludePathPatterns(
                    "/mq/direct",
                    "/mq/topic",
                    "/mq/fanout",
                    "/mq/headers",
                    "/user/code",
                    "/user/login",
                    "/blog/hot",
                    "/shop/**",
                    "/shop-type/**",
                    "/upload/**",
                    "/voucher/**"
                );
    }
}