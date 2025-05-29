package com.dp.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.redis.core.StringRedisTemplate;
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
import com.dp.utils.CaptchaUtils;
import com.dp.utils.JwtUtils;
import com.dp.utils.UserHolder;

import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

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
@Tag(name = "用户管理", description = "用户相关的API接口")
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
    @Operation(summary = "发送手机验证码", description = "发送手机验证码用于登录或注册")
    @ApiResponse(responseCode = "200", description = "发送成功", content = @Content(schema = @Schema(implementation = Result.class)))
    public Result sendCode(
            @Parameter(description = "包含手机号和验证码类型的数据") @RequestBody Map<String, String> data) {
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
    @Operation(summary = "用户登录", description = "用户登录功能，支持验证码或密码登录")
    @ApiResponse(responseCode = "200", description = "登录成功", content = @Content(schema = @Schema(implementation = Result.class)))
    public Result login(
            @Parameter(description = "登录表单，包含手机号和验证码或密码") @RequestBody LoginFormDTO loginForm,
            HttpServletResponse response,
            HttpServletRequest request) {
        // 获取设备标识
        String deviceId = request.getHeader("X-Device-ID");
        String userAgent = request.getHeader("User-Agent");
        String deviceFingerprint = jwtUtils.generateDeviceFingerprint(deviceId, userAgent, request.getRemoteAddr());

        // 验证图形验证码
        String captchaKey = "captcha:" + deviceFingerprint;
        String storedCaptcha = stringRedisTemplate.opsForValue().get(captchaKey);

        // 验证码无效或不匹配
        if (storedCaptcha == null || !storedCaptcha.equalsIgnoreCase(loginForm.getImageCaptcha())) {
            return Result.fail("图形验证码错误");
        }

        // 验证成功后删除缓存中的验证码，防止重复使用
        stringRedisTemplate.delete(captchaKey);

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
    @Operation(summary = "刷新令牌", description = "刷新用户访问令牌")
    @ApiResponse(responseCode = "200", description = "刷新成功", content = @Content(schema = @Schema(implementation = Result.class)))
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
    @Operation(summary = "用户注册", description = "用户注册功能")
    @ApiResponse(responseCode = "200", description = "注册成功", content = @Content(schema = @Schema(implementation = Result.class)))
    public Result register(
            @Parameter(description = "注册表单，包含手机号、验证码和密码") @RequestBody RegisterFormDTO registerForm) {
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
    @Operation(summary = "重置密码", description = "忘记密码功能，通过验证码重置密码")
    @ApiResponse(responseCode = "200", description = "重置成功", content = @Content(schema = @Schema(implementation = Result.class)))
    public Result resetPassword(
            @Parameter(description = "包含手机号、验证码和新密码的数据") @RequestBody Map<String, String> data) {
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
    @Operation(summary = "用户登出", description = "用户退出登录")
    @ApiResponse(responseCode = "200", description = "登出成功", content = @Content(schema = @Schema(implementation = Result.class)))
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
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    @ApiResponse(responseCode = "200", description = "获取成功", content = @Content(schema = @Schema(implementation = Result.class)))
    public Result info() {
        // 获取当前登录的用户信息
        return userService.getInfoDTO();
    }

    @PutMapping("/info")
    @Operation(summary = "更新用户详细信息", description = "更新用户的详细个人信息")
    @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = Result.class)))
    public Result update(
            @Parameter(description = "用户详细信息") @RequestBody UserInfo userInfo) {
        // 更新用户信息
        return userService.update(userInfo);
    }

    // 头像和昵称在user表里，修改头像和昵称
    @PutMapping("/update")
    @Operation(summary = "更新用户基本信息", description = "更新用户的基本信息，如头像和昵称")
    @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = Result.class)))
    public Result update(
            @Parameter(description = "用户基本信息") @RequestBody User user) {
        // 更新用户信息
        return userService.updateUser(user);
    }

    @GetMapping("/captcha")
    @Operation(summary = "获取图形验证码", description = "生成图形验证码并返回Base64图片数据")
    @ApiResponse(responseCode = "200", description = "生成成功", content = @Content(schema = @Schema(implementation = Result.class)))
    public Result getCaptcha(HttpServletRequest request) {
        // 获取设备标识
        String deviceId = request.getHeader("X-Device-ID");
        String userAgent = request.getHeader("User-Agent");
        String deviceFingerprint = jwtUtils.generateDeviceFingerprint(deviceId, userAgent, request.getRemoteAddr());

        // 生成验证码
        Map<String, String> captchaMap = CaptchaUtils.generateCaptcha(120, 40, 4);
        String captchaText = captchaMap.get("captchaText");
        String captchaImage = captchaMap.get("captchaImage");

        // 生成唯一标识符，可防止重放攻击
        String captchaId = UUID.randomUUID().toString();

        // 将验证码存入Redis，设置过期时间5分钟
        String captchaKey = "captcha:" + deviceFingerprint;
        stringRedisTemplate.opsForValue().set(captchaKey, captchaText, 5, TimeUnit.MINUTES);

        // 记录验证码请求日志（可选）
        log.debug("生成验证码: {} 设备: {}", captchaId, deviceFingerprint);

        Map<String, String> result = new HashMap<>();
        result.put("captchaImage", captchaImage);
        result.put("captchaId", captchaId); // 返回ID便于前端引用

        return Result.ok(result);
    }

    // @GetMapping("/count")
    // public Result getCount() {
    // return userService.getCount();
    // }
}
