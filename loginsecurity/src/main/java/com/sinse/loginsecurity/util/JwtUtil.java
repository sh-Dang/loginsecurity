package com.sinse.loginsecurity.util;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    private SecretKey secretKey;

    public JwtUtil(@Value("${spring.jwt.secret}") String secret) {
        // 더 안전하고 표준적인 방법으로 SecretKey를 생성합니다.
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // 토큰에서 username을 추출하는 메서드 JwtFilter에서 검증로직을 통해 검증할 것임
    public String getUsername(String token) {
        log.debug("19. 받은 accessToken을 검증중 입니다. 지금 줄에서는 userName을 꺼내어 검증 하는중입니다.");
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().get("username", String.class);
    }

    // 토큰에서 role을 추출하는 메서드 JwtFilter에서 검증로직을 통해 검증할 것임
    public String getRole(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().get("role", String.class);
    }

    // 토큰이 만료되었는지 확인하는 메서드
    public Boolean isExpired(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getExpiration().before(new Date());
    }

    // JWT를 생성하는 메서드
    public String createJwt(String username, String role, Long expiredMs) {
        return Jwts.builder()
                .claim("username", username)
                .claim("role", role)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }
}