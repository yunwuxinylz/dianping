package com.dp.utils;

import com.dp.dto.UserDTO;
/**
 * 基于ThreadLocal封装工具类，用户保存和获取当前登录用户
 */

public class UserHolder {
    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    public static void saveUser(UserDTO user) {

        tl.set(user);
    }

    public static UserDTO getUser() {
        return tl.get();
    }

    public static void removeUser() {
        tl.remove();
    }
}
