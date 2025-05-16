package com.dp.service.impl;

import static com.dp.utils.RedisConstants.LOGIN_CODE_TTL;
import static com.dp.utils.RedisConstants.LOGIN_USER_ID_KEY;
import static com.dp.utils.RedisConstants.LOGIN_USER_KEY;
import static com.dp.utils.RedisConstants.LOGIN_USER_TTL;
import static com.dp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

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
import com.dp.service.IUserService; // 确保这是正确的 UserService 接口
import com.dp.utils.RegexUtils;
import com.dp.utils.UserHolder;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService { // 假设继承了 Mybatis-Plus 的 ServiceImpl

    @Resource // 或者 @Autowired
    private UserMapper userMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IUserInfoService userInfoService;

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
    public Result login(LoginFormDTO loginForm) {
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

        return getTokenKey(user);
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

    private Result getTokenKey(User user) {
        // 获取用户id
        Long userId = user.getId();
        // 判断是否为管理员
        Boolean isAdmin = user.getIsAdmin();
        // 删除该用户的旧token
        String oldToken = stringRedisTemplate.opsForValue().get(LOGIN_USER_ID_KEY + userId);
        if (oldToken != null) {
            stringRedisTemplate.delete(LOGIN_USER_KEY + oldToken);
            stringRedisTemplate.delete(LOGIN_USER_ID_KEY + userId);
        }

        // 随机生成新token
        String token = UUID.randomUUID().toString(true);
        // 将User对象转为Hash存储
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        // 存储用户信息
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        // 保存用户id到token的映射
        stringRedisTemplate.opsForValue().set(LOGIN_USER_ID_KEY + userId, token, LOGIN_USER_TTL, TimeUnit.MINUTES);
        // 设置token有效期
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("isAdmin", isAdmin);
        return Result.ok(result);
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

    @Override
    public Result getCount() {
        try {
            // 从数据库获取用户总数
            // 注意：MyBatis-Plus 的 BaseMapper 默认没有 count() 方法，
            // 你可能需要使用 selectCount(null) 或者在 UserMapper 中自定义一个 count() 方法
            // 这里我们假设 UserMapper 中有一个自定义的 count() 方法
            // 或者直接使用 Mybatis-Plus 提供的方法
            long count = userMapper.selectCount(null); // 使用 Mybatis-Plus 的方法
            // 如果你自定义了 UserMapper.count()，则使用：
            // int count = userMapper.count();
            return Result.ok(count); // 修改：将 Result.success 改为 Result.ok
        } catch (Exception e) {
            // 记录日志会更好
            // log.error("获取用户总数失败", e);
            return Result.fail("获取用户总数失败"); // 修改：将 Result.error 改为 Result.fail
        }
    }
}
