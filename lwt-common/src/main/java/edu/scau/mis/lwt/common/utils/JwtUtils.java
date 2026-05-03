package edu.scau.mis.lwt.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 用于生成、解析和验证JWT令牌
 * JWT令牌用于用户登录后的身份认证
 */
public class JwtUtils {

    /**
     * JWT签名密钥（生产环境应放在配置文件中）
     */
    private static final String SECRET = "lwt-secret-key-2026-very-long-string-for-security";

    /**
     * 令牌过期时间（24小时，单位：毫秒）
     */
    private static final long EXPIRATION = 86400000;

    /**
     * 获取签名密钥
     * @return SecretKey 用于JWT签名的密钥
     */
    private static SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT令牌
     * @param userId 用户ID
     * @param username 用户名
     * @return String 生成的JWT令牌
     */
    public static String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析JWT令牌
     * @param token JWT令牌
     * @return Claims 令牌中的数据载荷
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从令牌中获取用户ID
     * @param token JWT令牌
     * @return Long 用户ID
     */
    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从令牌中获取用户名
     * @param token JWT令牌
     * @return String 用户名
     */
    public static String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 检查令牌是否过期
     * @param token JWT令牌
     * @return boolean true-已过期，false-未过期
     */
    public static boolean isTokenExpired(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration().before(new Date());
    }
}
