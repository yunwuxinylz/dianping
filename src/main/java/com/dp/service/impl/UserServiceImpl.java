package com.dp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dp.dto.LoginFormDTO;
import com.dp.dto.Result;
import com.dp.dto.UserDTO;
import com.dp.entity.User;
import com.dp.mapper.UserMapper;
import com.dp.service.IUserService;
import com.dp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.dp.utils.RedisConstants.*;
import static com.dp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        //校验手机号
        if (RegexUtils.isPhoneInvalid(phone)){
            //不符合，返回错误消息
            return  Result.fail("手机号格式错误！");
        }
        //符合，生成验证码
        String code = RandomUtil.randomNumbers(6);
        //保存验证码session到redis数据库中,set key value ex 120
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
        //发送验证码
        log.debug("验证码发送成功：{}",code);
        //返回ok
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm) {
        //校验手机号
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            //不符合，返回错误信息
            return Result.fail("手机号格式错误");
        }
        //符合，校验验证码
        //从redis获取验证码并校验
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.equals(code)) {
            //不一致，报错
            return Result.fail("验证码错误");
        }
        //一致，查询用户 select * from tb_user where phone = ? 并删除验证码
        //select * from tb_user where phone =?
        User user = query().eq("phone",phone).one();
        stringRedisTemplate.delete(LOGIN_CODE_KEY + phone);

        //判断用户是否存在
        if (user == null) {
            //不存在，创建新用户
            user = createUserWithPhone(phone);
        }
        
        // 获取用户id
        Long userId = user.getId();
        // 删除该用户的旧token
        String oldToken = stringRedisTemplate.opsForValue().get(LOGIN_USER_ID_KEY + userId);
        if (oldToken != null) {
            stringRedisTemplate.delete(LOGIN_USER_KEY + oldToken);
            stringRedisTemplate.delete(LOGIN_USER_ID_KEY + userId);
        }
        
        //随机生成新token
        String token = UUID.randomUUID().toString(true);
        //将User对象转为Hash存储
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        //存储用户信息
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        // 保存用户id到token的映射
        stringRedisTemplate.opsForValue().set(LOGIN_USER_ID_KEY + userId, token, LOGIN_USER_TTL, TimeUnit.HOURS);
        //设置token有效期
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);
        
        return Result.ok(token);
    }
    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        save(user);
        return user;    // 修改：返回创建的用户对象，而不是null
    }
}
