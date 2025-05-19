package com.dp.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoder {

    private static final BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();

    public static String encode(String password) {
        return bcryptEncoder.encode(password);
    }

    public static Boolean matches(String rawPassword, String encodedPassword) {
        if (encodedPassword == null || rawPassword == null) {
            return false;
        }

        // 使用BCrypt验证
        return bcryptEncoder.matches(rawPassword, encodedPassword);
    }
}
