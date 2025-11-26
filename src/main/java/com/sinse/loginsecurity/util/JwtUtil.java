package com.sinse.loginsecurity.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    // Ideally, this should be loaded from application.properties
    // @Value("${jwt.secret}")
    private static final String SECRET_KEY_STRING = "thisIsMySuperSecretKeyForJWTAuthenticationAndItShouldBeLongEnough"; // 최소 256비트 (32바이트)
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8));

    // Token expiration times (milliseconds)
    public static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30; // 30 minutes
    public static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7; // 7 days

    // JWT 생성
    public String createJwt(String username, String role, Long expireTime) {
        Claims claims = Jwts.claims();
        claims.put("username", username);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // JWT에서 Claims 추출
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 토큰 만료 여부 확인
    public Boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    // JWT에서 사용자 이름 추출
    public String getUsername(String token) {
        return extractAllClaims(token).get("username", String.class);
    }

    // JWT에서 사용자 역할(Role) 추출
    public String getRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }
}
