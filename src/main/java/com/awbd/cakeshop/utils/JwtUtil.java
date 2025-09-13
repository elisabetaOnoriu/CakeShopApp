package com.awbd.cakeshop.utils;

import com.awbd.cakeshop.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private final TokenBlacklistService tokenBlacklistService;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.ttl-ms:36000000}") // 10h
    private long tokenTtlMs;

    private volatile SecretKey cachedKey;

    public JwtUtil(TokenBlacklistService tokenBlacklistService) {
        this.tokenBlacklistService = tokenBlacklistService;
    }

    // Validează & construiește cheia la prima utilizare (și o cache-uiește)
    private SecretKey signingKey() {
        SecretKey k = cachedKey;
        if (k == null) {
            synchronized (this) {
                k = cachedKey;
                if (k == null) {
                    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
                    if (keyBytes.length < 32) {
                        throw new IllegalStateException("jwt.secret trebuie să aibă minim 32 de caractere (HS256).");
                    }
                    cachedKey = k = Keys.hmacShaKeyFor(keyBytes); // RAW, nu Base64
                }
            }
        }
        return k;
    }

    public String generateToken(String username, User.Role role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role.name());
        Date now = new Date();
        Date exp = new Date(now.getTime() + tokenTtlMs);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(exp)
                .signWith(signingKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        try { return parseClaims(token).getSubject(); } catch (Exception e) { return null; }
    }

    public User.Role getRoleFromToken(String token) {
        try {
            Object value = parseClaims(token).get("role");
            return value != null ? User.Role.valueOf(value.toString()) : null;
        } catch (Exception e) { return null; }
    }

    public boolean validateToken(String token) {
        try {
            if (isTokenBlacklisted(token)) return false;
            Claims claims = parseClaims(token);
            Date exp = claims.getExpiration();
            return exp != null && exp.after(new Date());
        } catch (Exception e) { return false; }
    }

    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistService.esteInvalid(token);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
