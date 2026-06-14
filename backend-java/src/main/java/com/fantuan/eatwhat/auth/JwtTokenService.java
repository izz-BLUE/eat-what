package com.fantuan.eatwhat.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token 服务
 */
@Slf4j
@Service
public class JwtTokenService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-seconds:604800}")
    private Long expirationSeconds;

    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET 必须至少 32 字节");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 token
     */
    public String generateToken(Long userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationSeconds * 1000);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    /**
     * 解析 token，返回 userId
     *
     * @return userId，无效或过期返回 null
     */
    public Long parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String subject = claims.getSubject();
            if (subject == null || subject.isEmpty()) {
                log.debug("Token subject 为空");
                return null;
            }
            return Long.parseLong(subject);
        } catch (NumberFormatException e) {
            log.debug("Token subject 非数字");
            return null;
        } catch (ExpiredJwtException e) {
            log.debug("Token 已过期");
            return null;
        } catch (JwtException e) {
            log.debug("Token 无效: {}", e.getClass().getSimpleName());
            return null;
        }
    }

    /**
     * 获取过期时间（秒）
     */
    public Long getExpirationSeconds() {
        return expirationSeconds;
    }
}
