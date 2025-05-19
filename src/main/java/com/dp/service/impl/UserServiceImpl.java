package com.dp.service.impl;

import static com.dp.utils.RedisConstants.LOGIN_CODE_TTL;
import static com.dp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dp.dto.LoginFormDTO;
import com.dp.dto.RegisterFormDTO;
import com.dp.dto.Result;
import com.dp.dto.UserDTO;
import com.dp.dto.UserInfoDTO;
import com.dp.entity.User;
import com.dp.entity.UserInfo;
import com.dp.mapper.UserMapper;
import com.dp.service.IUserInfoService;
import com.dp.service.IUserService;
import com.dp.utils.JwtUtils;
import com.dp.utils.RegexUtils;
import com.dp.utils.UserHolder;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IUserInfoService userInfoService;

    private final JwtUtils jwtUtils;

    public UserServiceImpl(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public Result sendCode(String phone, String type) {
        // 校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 不符合，返回错误消息
            return Result.fail("手机号格式错误！");
        }
        // 符合，生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 保存验证码
        stringRedisTemplate.opsForValue().set("code:" + type + ":" + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
        // 发送验证码
        log.debug("验证码发送成功：{}", code);
        // 返回ok
        return Result.ok(code);
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpServletResponse response) {
        // 校验手机号
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 不符合，返回错误信息
            return Result.fail("手机号格式错误");
        }

        User user = query().eq("phone", phone).one();

        // 判断用户是否存在
        if (user == null) {
            // 不存在，创建新用户
            return Result.fail("用户不存在，请注册");
        }

        String code = loginForm.getCode();
        String password = loginForm.getPassword();

        if (password != null) {
            // 校验密码
            String userPassword = user.getPassword();
            if (!userPassword.equals(password)) {
                return Result.fail("密码错误");
            }
        }

        if (code != null) {
            // 校验验证码
            String cacheCode = stringRedisTemplate.opsForValue().get("code:login:" + phone);
            if (cacheCode == null || !cacheCode.equals(code)) {
                return Result.fail("验证码错误");
            }
        }
        // 删除验证码
        stringRedisTemplate.delete("code:login:" + phone);

        // 生成双token
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Boolean isAdmin = user.getIsAdmin();
        String accessToken = jwtUtils.generateAccessToken(userDTO);
        String refreshToken = jwtUtils.generateRefreshToken(userDTO.getId());

        // 创建HttpOnly Cookie存储Refresh Token
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true); // 仅HTTPS
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (JwtUtils.REFRESH_TOKEN_EXPIRATION / 1000)); // 设置刷新token的过期时间15天
        response.addCookie(refreshTokenCookie);

        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("isAdmin", isAdmin);
        return Result.ok(result);
    }

    // 刷新accessToken
    @Override
    public Result refreshToken(HttpServletRequest request, HttpServletResponse response) {
        // 从Cookie中获取Refresh Token
        Cookie[] cookies = request.getCookies();
        String refreshToken = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (StrUtil.isBlank(refreshToken)) {
            return Result.fail("未找到刷新令牌");
        }

        // 验证Refresh Token
        if (!jwtUtils.validateRefreshToken(refreshToken)) {
            return Result.fail("刷新令牌已过期，请重新登录");
        }

        // 从Refresh Token中提取用户ID
        Long userId = jwtUtils.extractUserIdFromRefreshToken(refreshToken);

        // 获取用户信息
        User user = getById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }

        // 生成新的Access Token
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        String accessToken = jwtUtils.generateAccessToken(userDTO);

        // 返回新的Access Token
        return Result.ok(accessToken);
    }

    // 注册
    @Override
    public Result register(RegisterFormDTO registerForm) {
        // 校验手机号
        String phone = registerForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }
        // 是否注册过
        User user = query().eq("phone", phone).one();
        if (user != null) {
            return Result.fail("用户已存在");
        }
        // 校验验证码
        String code = registerForm.getCode();
        String cacheCode = stringRedisTemplate.opsForValue().get("code:register:" + phone);
        if (cacheCode == null || !cacheCode.equals(code)) {
            return Result.fail("验证码错误");
        }
        // 创建用户
        user = createUserWithPhone(registerForm);
        // 删除验证码
        stringRedisTemplate.delete("code:register:" + phone);
        return Result.ok("注册成功");
    }

    private User createUserWithPhone(RegisterFormDTO registerForm) {
        User user = new User();
        user.setPhone(registerForm.getPhone());
        user.setPassword(registerForm.getPassword());
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        save(user);
        return user; // 修改：返回创建的用户对象，而不是null
    }

    /**
     * 忘记密码功能
     *
     * @param phone
     * @param password
     */
    @Override
    @Transactional
    public Result resetPassword(String phone, String code, String password) {
        // 校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }
        // 校验验证码
        String redisCode = stringRedisTemplate.opsForValue().get("code:reset:" + phone);
        if (code == null || !code.equals(redisCode)) {
            return Result.fail("验证码错误");
        }
        // 根据手机号查询用户
        User user = query().eq("phone", phone).one();
        if (user == null) {
            return Result.fail("用户不存在");
        }
        // 更新用户密码
        user.setPassword(password);
        updateById(user);
        // 删除验证码
        stringRedisTemplate.delete("code:reset:" + phone);
        return Result.ok("密码修改成功");
    }

    @Override
    @Transactional
    public Result update(UserInfo userInfo) {
        // 1. 获取当前登录用户
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.fail("用户未登录");
        }

        // 2. 验证用户身份
        if (userInfo.getUserId() == null) {
            // 如果前端没有提供用户ID，则使用当前登录用户的ID
            userInfo.setUserId(currentUser.getId());
        } else if (!userInfo.getUserId().equals(currentUser.getId())) {
            // 不允许修改其他用户的信息
            return Result.fail("无权修改其他用户信息");
        }

        // 3. 校验字段合法性

        // 3.1 设置不允许用户修改的字段为null，防止恶意篡改
        userInfo.setFans(null); // 粉丝数不允许自行修改
        userInfo.setFollowee(null); // 关注数不允许自行修改
        userInfo.setCredits(null); // 积分不允许自行修改
        userInfo.setLevel(null); // 会员级别不允许自行修改

        // 4. 查询用户是否存在
        UserInfo existingUserInfo = userInfoService.getById(userInfo.getUserId());

        try {
            // 5. 更新用户信息
            if (existingUserInfo == null) {
                // 如果不存在，则创建新的用户信息记录
                userInfo.setCreateTime(LocalDateTime.now());
                userInfo.setUpdateTime(LocalDateTime.now());
                userInfoService.save(userInfo);
            } else {
                // 如果存在，则更新
                userInfo.setUpdateTime(LocalDateTime.now());
                userInfoService.updateById(userInfo);
            }
            return Result.ok("更新成功");
        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            return Result.fail("更新失败：" + e.getMessage());
        }
    }

    @Override
    public Result updateUser(User user) {
        // 1. 获取当前登录用户
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.fail("用户未登录");
        }

        // 2. 更新用户信息
        user.setId(currentUser.getId());
        updateById(user);

        return Result.ok("更新成功");
    }

    @Override
    public Result getInfoDTO() {
        // 获取当前登录的用户id
        UserDTO userDTO = UserHolder.getUser();
        // 查询详情
        UserInfo info = userInfoService.getById(userDTO.getId());
        UserInfoDTO infoDTO = new UserInfoDTO();
        if (info != null) {
            infoDTO = BeanUtil.copyProperties(info, UserInfoDTO.class);
        }
        infoDTO.setId(userDTO.getId());
        infoDTO.setNickName(userDTO.getNickName());
        infoDTO.setIcon(userDTO.getIcon());
        return Result.ok(infoDTO);
    }

}
