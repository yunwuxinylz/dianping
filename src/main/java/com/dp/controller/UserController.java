package com.dp.controller;


import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dp.dto.LoginFormDTO;
import com.dp.dto.RegisterFormDTO;
import com.dp.dto.Result;
import com.dp.dto.UserDTO;
import com.dp.entity.User;
import com.dp.entity.UserInfo;
import com.dp.service.IUserInfoService;
import com.dp.service.IUserService;
import com.dp.utils.RedisConstants;
import com.dp.utils.UserHolder;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @since 2021-12-22
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private IUserInfoService userInfoService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发送手机验证码
     */
    @PostMapping("code")
    public Result sendCode(@RequestBody Map<String, String> data) {
        // 1.获取手机号
        String phone = data.get("phone");
        // 发送短信验证码并保存验证码
        return userService.sendCode(phone);
    }

    /**
     * 登录功能
     *
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginForm) {
        // 实现登录功能

        return userService.login(loginForm);
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
     * 登出功能
     *
     * @return 无
     */
    @PostMapping("/logout")
    public Result logout(@RequestParam String phone) {
        // TODO 实现登出功能
        UserHolder.removeUser();
        stringRedisTemplate.delete(RedisConstants.LOGIN_CODE_KEY + phone);
        return Result.ok("登出成功");
    }

    @GetMapping("/me")
    public Result me() {
        // 获取当前登录的用户并返回
        UserDTO user = UserHolder.getUser();
        return Result.ok(user);
    }

    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") Long userId) {
        // 查询详情
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            // 没有详情，应该是第一次查看详情
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        // 返回
        return Result.ok(info);
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
