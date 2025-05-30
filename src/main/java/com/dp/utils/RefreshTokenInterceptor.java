package com.dp.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.dp.dto.UserDTO;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 刷新token拦截器
 */
@Slf4j
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
            log.debug("用户[{}]的AccessToken有效，已设置用户信息", userDTO.getId());
        } else {
            log.debug("请求中的AccessToken无效或已过期");
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
