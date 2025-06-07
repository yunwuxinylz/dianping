package com.dp.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import com.dp.dto.UserDTO;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {

    // 用于签名的密钥
    private final static SecretKey ACCESS_TOKEN_SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final static SecretKey REFRESH_TOKEN_SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Access Token有效期: 15分钟
    public static final long ACCESS_TOKEN_EXPIRATION = 10 * 60 * 1000; // 10分钟

    // Refresh Token有效期: 15天
    public static final long REFRESH_TOKEN_EXPIRATION = 15 * 24 * 60 * 60 * 1000;

    // 创建AccessToken
    public String generateAccessToken(UserDTO userDTO) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userDTO.getId());
        claims.put("nickName", userDTO.getNickName());
        claims.put("icon", userDTO.getIcon());
        claims.put("isAdmin", userDTO.getIsAdmin());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDTO.getId().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(ACCESS_TOKEN_SECRET_KEY)
                .compact();
    }

    // 创建带版本号的RefreshToken
    public String generateRefreshToken(Long userId, String version) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("version", version);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(REFRESH_TOKEN_SECRET_KEY)
                .compact();
    }

    // 从RefreshToken中提取版本号
    public String extractVersionFromRefreshToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(REFRESH_TOKEN_SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return (String) claims.get("version");
    }

    // 从AccessToken中提取UserDTO
    public UserDTO extractUserFromAccessToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(ACCESS_TOKEN_SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();

        UserDTO userDTO = new UserDTO();
        userDTO.setId(Long.valueOf(claims.getSubject()));
        userDTO.setNickName((String) claims.get("nickName"));
        userDTO.setIcon((String) claims.get("icon"));
        userDTO.setIsAdmin((Boolean) claims.get("isAdmin"));
        return userDTO;
    }

    // 从RefreshToken中提取userId
    public Long extractUserIdFromRefreshToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(REFRESH_TOKEN_SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.valueOf(claims.getSubject());
    }

    // 验证AccessToken是否有效
    public boolean validateAccessToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(ACCESS_TOKEN_SECRET_KEY).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 验证RefreshToken是否有效
    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(REFRESH_TOKEN_SECRET_KEY).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 生成设备指纹
    public String generateDeviceFingerprint(String deviceId, String userAgent, String ipAddress) {
        // 组合设备信息并哈希
        String deviceInfo = deviceId + ":" + userAgent + ":" + ipAddress;
        return DigestUtils.md5DigestAsHex(deviceInfo.getBytes());
    }

    // 生成随机版本号
    public String generateTokenVersion() {
        return DigestUtils.md5DigestAsHex(String.valueOf(System.currentTimeMillis()).getBytes());
    }
}
