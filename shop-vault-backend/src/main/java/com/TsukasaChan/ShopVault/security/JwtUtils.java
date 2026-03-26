package com.TsukasaChan.ShopVault.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Getter
@SuppressWarnings("null")
public class JwtUtils {

    @Value("${shop-vault.jwt.secret}")
    private String secret;

    @Value("${shop-vault.jwt.expiration}")
    private long expiration;

    @Value("${shop-vault.jwt.refresh-expiration:604800000}")
    private long refreshExpiration;

    @Value("${shop-vault.jwt.min-secret-length:32}")
    private int minSecretLength;

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String REFRESH_TOKEN_KEY = "refresh_token:";

    public JwtUtils(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void validateSecretKey() {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException("JWT密钥未配置！请设置环境变量 JWT_SECRET");
        }
        int actualLength = secret.getBytes(StandardCharsets.UTF_8).length;
        if (actualLength < minSecretLength) {
            throw new IllegalStateException(
                String.format("JWT密钥长度不足！当前使用HS256算法，建议至少%d字节(当前: %d字节)。可通过配置 shop-vault.jwt.min-secret-length 调整最小长度要求。", 
                    minSecretLength, actualLength));
        }
        log.info("JWT密钥校验通过，长度: {} 字节", actualLength);
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(String username) {
        String refreshToken = UUID.randomUUID().toString().replace("-", "");
        String key = buildRefreshTokenKey(refreshToken);
        
        Map<String, String> tokenData = createTokenData(username);
        Objects.requireNonNull(redisTemplate.opsForValue(), "Redis operations not available");
        redisTemplate.opsForValue().set(Objects.requireNonNull(key), (Object) tokenData, refreshExpiration, TimeUnit.MILLISECONDS);
        
        return refreshToken;
    }

    public String refreshAccessToken(String refreshToken) {
        Map<String, String> tokenData = getAndDeleteTokenData(refreshToken);
        
        if (tokenData == null) {
            return null;
        }
        
        String username = tokenData.get("username");
        if (username == null) {
            return null;
        }
        
        generateRefreshToken(username);
        
        return generateToken(username);
    }

    public String getNewRefreshToken(String oldRefreshToken) {
        Map<String, String> tokenData = getAndDeleteTokenData(oldRefreshToken);
        
        if (tokenData == null) {
            return null;
        }
        
        String username = tokenData.get("username");
        if (username == null) {
            return null;
        }
        
        return generateRefreshToken(username);
    }

    public boolean validateRefreshToken(String refreshToken) {
        String key = buildRefreshTokenKey(refreshToken);
        Boolean hasKey = redisTemplate.hasKey(Objects.requireNonNull(key));
        return Boolean.TRUE.equals(hasKey);
    }

    public void invalidateRefreshToken(String refreshToken) {
        String key = buildRefreshTokenKey(refreshToken);
        Boolean deleted = redisTemplate.delete(Objects.requireNonNull(key));
        if (!Boolean.TRUE.equals(deleted)) {
            throw new IllegalStateException("Failed to invalidate refresh token");
        }
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, String username) {
        String extractedUsername = extractUsername(token);
        return extractedUsername.equals(username) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private String buildRefreshTokenKey(String refreshToken) {
        return REFRESH_TOKEN_KEY + refreshToken;
    }

    private Map<String, String> createTokenData(String username) {
        Map<String, String> tokenData = new HashMap<>();
        tokenData.put("username", username);
        tokenData.put("createdAt", String.valueOf(System.currentTimeMillis()));
        return tokenData;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getAndDeleteTokenData(String refreshToken) {
        String key = buildRefreshTokenKey(refreshToken);
        Object value = redisTemplate.opsForValue().get(Objects.requireNonNull(key));
        if (value == null) {
            return null;
        }
        
        Map<String, String> tokenData = (Map<String, String>) value;
        Boolean deleted = redisTemplate.delete(Objects.requireNonNull(key));
        if (!Boolean.TRUE.equals(deleted)) {
            throw new IllegalStateException("Failed to delete token data");
        }
        
        return tokenData;
    }
}
