package com.dhr.maven.virus_backend.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JwtUtil {

    private static final String SECRET_KEY = "your_secret_key";  // 请使用更强的密钥

    // 生成 JWT Token
    public static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)  // 设置用户名为 Token 的主体
                .setIssuedAt(new Date())  // 设置生成时间
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))  // 设置过期时间（24小时）
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)  // 使用HS256算法签名
                .compact();
    }

    // 从 JWT 中获取用户名
    public static String getUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 检查 Token 是否过期
    public static boolean isTokenExpired(String token) {
        Date expiration = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return expiration.before(new Date());
    }
}
