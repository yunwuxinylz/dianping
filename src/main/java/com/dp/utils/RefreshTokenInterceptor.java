package com.dp.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.dp.dto.UserDTO;

import cn.hutool.core.util.StrUtil;

/**
 * 刷新token拦截器
 */
@Component
public class RefreshTokenInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    public RefreshTokenInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @SuppressWarnings("null")
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 获取请求头中的Access Token
        String accessToken = request.getHeader("Authorization");
        if (StrUtil.isBlank(accessToken)) {
            return true;
        }

        // 验证Access Token
        if (jwtUtils.validateAccessToken(accessToken)) {
            // Access Token有效，从Access Token中提取用户信息并保存到ThreadLocal
            UserDTO userDTO = jwtUtils.extractUserFromAccessToken(accessToken);
            UserHolder.saveUser(userDTO);
        }
        return true;
    }

    @SuppressWarnings("null")
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {
        UserHolder.removeUser();
    }
}
