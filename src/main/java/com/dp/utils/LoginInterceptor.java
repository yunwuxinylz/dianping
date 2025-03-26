package com.dp.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import cn.hutool.core.util.StrUtil;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Allow RabbitMQ test endpoints
        String uri = request.getRequestURI();
        if (uri.startsWith("/mq/")) {
            return true;
        }
        
        // Existing authentication logic
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)) {
            response.setStatus(401);
            return false;
        }

        return true;
    }
}

