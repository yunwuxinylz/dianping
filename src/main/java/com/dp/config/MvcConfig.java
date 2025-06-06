package com.dp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.dp.utils.JwtUtils;
import com.dp.utils.LoginInterceptor;
import com.dp.utils.RefreshTokenInterceptor;

/**
 * 拦截器配置
 */

@Configuration
public class MvcConfig implements WebMvcConfigurer {

        private final JwtUtils jwtUtils;

        public MvcConfig(JwtUtils jwtUtils) {
                this.jwtUtils = jwtUtils;
        }

        @Override
        public void addInterceptors(@SuppressWarnings("null") InterceptorRegistry registry) {
                // 登录拦截器
                registry.addInterceptor(new LoginInterceptor())
                                .excludePathPatterns(
                                                "/shop/**",
                                                "/voucher/**",
                                                "/upload/**",
                                                "/blog/hot",
                                                "/user/code",
                                                "/user/login",
                                                "/user/logout",
                                                "/user/register",
                                                "/user/reset-password",
                                                "/user/refresh-token",
                                                "/goods/**")
                                .order(1);
                // token刷新的拦截器
                registry.addInterceptor(new RefreshTokenInterceptor(jwtUtils)).addPathPatterns("/**")
                                .order(0);
        }
}
