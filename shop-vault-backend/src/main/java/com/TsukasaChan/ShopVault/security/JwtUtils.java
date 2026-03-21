package com.TsukasaChan.ShopVault.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Getter
public class JwtUtils {

    @Value("${shop-vault.jwt.secret}")
    private String secret;

    @Value("${shop-vault.jwt.expiration}")
    private long expiration;

    @Value("${shop-vault.jwt.refresh-expiration:604800000}")
    private long refreshExpiration;

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String REFRESH_TOKEN_KEY = "refresh_token:";

    public JwtUtils(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
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
        redisTemplate.opsForValue().set(key, tokenData, refreshExpiration, TimeUnit.MILLISECONDS);
        
        return refreshToken;
    }

    public String refreshAccessToken(String refreshToken) {
        Map<String, String> tokenData = getAndDeleteTokenData(refreshToken);
        
        if (tokenData == null) {
            return null;
        }
        
        String username = tokenData.get("username");
        
        generateRefreshToken(username);
        
        return generateToken(username);
    }

    public String getNewRefreshToken(String oldRefreshToken) {
        Map<String, String> tokenData = getAndDeleteTokenData(oldRefreshToken);
        
        if (tokenData == null) {
            return null;
        }
        
        String username = tokenData.get("username");
        
        return generateRefreshToken(username);
    }

    public boolean validateRefreshToken(String refreshToken) {
        String key = buildRefreshTokenKey(refreshToken);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void invalidateRefreshToken(String refreshToken) {
        String key = buildRefreshTokenKey(refreshToken);
        redisTemplate.delete(key);
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
        Map<String, String> tokenData = (Map<String, String>) redisTemplate.opsForValue().get(key);
        
        if (tokenData != null) {
            redisTemplate.delete(key);
        }
        
        return tokenData;
    }
}
