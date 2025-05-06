package com.dp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dp.dto.LoginFormDTO;
import com.dp.dto.RegisterFormDTO;
import com.dp.dto.Result;
import com.dp.dto.UserDTO;
import com.dp.entity.User;
import com.dp.entity.UserInfo;


/**
 * <p>
 * 服务类
 * </p>
 *
 * @since 2021-12-22
 */
public interface IUserService extends IService<User> {

    Result sendCode(String phone, String type);

    Result login(LoginFormDTO loginForm);

    Result register(RegisterFormDTO registerForm);

    Result update(UserInfo userInfo);

    Result updateUser(User user);

    Result getInfoDTO();

    Result resetPassword(String phone, String code, String password);
}
