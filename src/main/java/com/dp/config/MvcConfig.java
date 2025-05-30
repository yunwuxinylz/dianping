package com.dp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.dp.utils.JwtUtils;
import com.dp.utils.LoginInterceptor;
import com.dp.utils.PaymentVerificationInterceptor;
import com.dp.utils.RefreshTokenInterceptor;

/**
 * 拦截器配置
 */

@Configuration
public class MvcConfig implements WebMvcConfigurer {

        private final JwtUtils jwtUtils;
        private final StringRedisTemplate stringRedisTemplate;

        public MvcConfig(JwtUtils jwtUtils, StringRedisTemplate stringRedisTemplate) {
                this.jwtUtils = jwtUtils;
                this.stringRedisTemplate = stringRedisTemplate;
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
                                                "/user/captcha",
                                                "/goods/**",
                                                "/swagger-ui.html",
                                                "/swagger-ui/**",
                                                "/api-docs/**",
                                                "/v3/api-docs/**",
                                                "/swagger-resources/**",
                                                "/webjars/**")
                                .order(2);

                // 支付验证拦截器，只拦截支付相关的路径
                registry.addInterceptor(new PaymentVerificationInterceptor(jwtUtils, stringRedisTemplate))
                                .addPathPatterns("/order/pay/**")
                                .order(1);

                // token的拦截器
                registry.addInterceptor(new RefreshTokenInterceptor(jwtUtils)).addPathPatterns("/**")
                                .order(0);
        }
}
