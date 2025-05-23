package com.dp.controller;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dp.dto.LoginFormDTO;
import com.dp.dto.RegisterFormDTO;
import com.dp.dto.Result;
import com.dp.entity.User;
import com.dp.entity.UserInfo;
import com.dp.service.IUserService;
import com.dp.utils.UserHolder;
import com.dp.utils.JwtUtils;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * 
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    private final IUserService userService;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate stringRedisTemplate;

    public UserController(IUserService userService, JwtUtils jwtUtils, StringRedisTemplate stringRedisTemplate) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 发送手机验证码
     */
    @PostMapping("code")
    public Result sendCode(@RequestBody Map<String, String> data) {
        // 1.获取手机号
        String phone = data.get("phone");
        if (StrUtil.isBlank(phone)) {
            return Result.fail("手机号不能为空");
        }
        // 方式
        String type = data.get("type");
        // 发送短信验证码并保存验证码
        return userService.sendCode(phone, type);
    }

    /**
     * 登录功能
     *
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginForm, HttpServletResponse response,
            HttpServletRequest request) {
        // 实现登录功能

        return userService.login(loginForm, response, request);
    }

    /**
     * 刷新accessToken
     *
     * @param request
     * @param response
     * @return
     */
    @GetMapping("/refresh-token")
    public Result refreshToken(HttpServletRequest request, HttpServletResponse response) {
        // 实现刷新accessToken功能
        return userService.refreshToken(request, response);
    }

    /**
     * 注册功能
     *
     * @param registerForm 注册参数，包含手机号、验证码、密码
     */
    @PostMapping("/register")
    public Result register(@RequestBody RegisterFormDTO registerForm) {
        // 实现注册功能
        return userService.register(registerForm);
    }

    /**
     * 忘记密码功能
     *
     * @param phone
     * @param password
     * @return
     */
    @PutMapping("/reset-password")
    public Result resetPassword(@RequestBody Map<String, String> data) {
        // 实现忘记密码功能
        String phone = data.get("phone");
        String code = data.get("code");
        String password = data.get("password");
        return userService.resetPassword(phone, code, password);
    }

    /**
     * 登出功能
     *
     * @return 无
     */
    @PostMapping("/logout")
    public Result logout(HttpServletRequest request, HttpServletResponse response) {
        // 获取当前登录的用户信息
        Long userId = UserHolder.getUser().getId();

        // 判断是否是该用户
        if (userId == null) {
            return Result.fail("用户未登录");
        }

        // 清除设备指纹
        String deviceId = request.getHeader("X-Device-ID");
        String userAgent = request.getHeader("User-Agent");
        String deviceFingerprint = jwtUtils.generateDeviceFingerprint(deviceId, userAgent, request.getRemoteAddr());

        // 从设备列表中删除
        String deviceKey = "devices:" + userId;
        stringRedisTemplate.opsForHash().delete(deviceKey, deviceFingerprint);

        // 清除Refresh Token Cookie
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setMaxAge(0);
        cookie.setPath("/api/user/refresh-token");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);

        return Result.ok("登出成功");
    }

    @GetMapping("/info")
    public Result info() {
        // 获取当前登录的用户信息
        return userService.getInfoDTO();
    }

    @PutMapping("/info")
    public Result update(@RequestBody UserInfo userInfo) {
        // 更新用户信息
        return userService.update(userInfo);
    }

    // 头像和昵称在user表里，修改头像和昵称
    @PutMapping("/update")
    public Result update(@RequestBody User user) {
        // 更新用户信息
        return userService.updateUser(user);
    }

}
