package com.dp.utils;

import static com.dp.utils.RedisConstants.REFRESH_TOKEN_VERSION_KEY;
import static com.dp.utils.RedisConstants.USER_DEVICES_KEY;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import com.dp.dto.UserDTO;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 支付验证拦截器
 * 验证用户在进行支付操作时必须持有有效的refresh token
 */
@Slf4j
public class PaymentVerificationInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final StringRedisTemplate stringRedisTemplate;

    public PaymentVerificationInterceptor(JwtUtils jwtUtils, StringRedisTemplate stringRedisTemplate) {
        this.jwtUtils = jwtUtils;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @SuppressWarnings("null")
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 获取用户信息，如果用户未登录，拒绝访问
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            response.setStatus(403);
            return false;
        }

        // 从Cookie中获取Refresh Token
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        // 如果没有找到refreshToken，拒绝访问
        if (StrUtil.isBlank(refreshToken)) {
            log.warn("支付操作未找到刷新令牌，用户ID: {}", user.getId());
            response.setStatus(403);
            return false;
        }

        // 验证refreshToken有效性
        if (!jwtUtils.validateRefreshToken(refreshToken)) {
            log.warn("支付操作刷新令牌无效，用户ID: {}", user.getId());
            response.setStatus(403);
            return false;
        }

        // 获取设备指纹
        String deviceId = request.getHeader("X-Device-ID");
        String userAgent = request.getHeader("User-Agent");
        String deviceFingerprint = jwtUtils.generateDeviceFingerprint(deviceId, userAgent, request.getRemoteAddr());

        // 检查设备是否已授权
        Boolean isKnownDevice = stringRedisTemplate.opsForHash().hasKey(USER_DEVICES_KEY + user.getId(),
                deviceFingerprint);
        if (Boolean.FALSE.equals(isKnownDevice)) {
            log.warn("支付操作使用未授权设备，用户ID: {}, 指纹: {}", user.getId(), deviceFingerprint);
            response.setStatus(403);
            return false;
        }

        // 验证版本号
        String storedVersionKey = REFRESH_TOKEN_VERSION_KEY + user.getId() + ":" + deviceFingerprint;
        String storedVersion = stringRedisTemplate.opsForValue().get(storedVersionKey);

        if (storedVersion == null) {
            log.warn("支付操作版本号不存在，用户ID: {}", user.getId());
            response.setStatus(403);
            return false;
        }

        // 从刷新令牌中提取版本号
        String tokenVersion;
        try {
            tokenVersion = jwtUtils.extractVersionFromRefreshToken(refreshToken);
        } catch (Exception e) {
            log.error("从刷新令牌中提取版本号失败", e);
            response.setStatus(403);
            return false;
        }

        // 验证版本号是否匹配
        if (!storedVersion.equals(tokenVersion)) {
            log.warn("支付操作刷新令牌版本号不匹配: userId={}, expected={}, actual={}",
                    user.getId(), storedVersion, tokenVersion);
            response.setStatus(403);
            return false;
        }

        // 通过所有验证
        return true;
    }
}