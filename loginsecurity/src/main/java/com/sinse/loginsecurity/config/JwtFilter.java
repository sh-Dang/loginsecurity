package com.sinse.loginsecurity.config;

import com.sinse.loginsecurity.service.JpaUserDetailsService;
import com.sinse.loginsecurity.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final JpaUserDetailsService jpaUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. "Authorization" 헤더에서 토큰을 가져옵니다.
        String authorization = request.getHeader("Authorization");

        // 2. 토큰이 없거나, "Bearer "로 시작하지 않으면 인증을 시도하지 않고 다음 필터로 넘어갑니다.
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. "Bearer " 부분을 제거하고 순수 토큰만 추출합니다.
        String token = authorization.split(" ")[1];

        // 4. 토큰이 만료되었는지 확인하고, 만료되었다면 인증을 진행하지 않습니다.
        if (jwtUtil.isExpired(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 5. 토큰에서 username을 추출합니다.
        String username = jwtUtil.getUsername(token);

        // 6. username으로 UserDetails 객체(사용자 정보)를 조회합니다.
        UserDetails userDetails = jpaUserDetailsService.loadUserByUsername(username);

        // 7. Spring Security가 이해할 수 있는 인증 토큰(Authentication)을 생성합니다.
        UsernamePasswordAuthenticationToken authToken = new
                UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        // 8. SecurityContext에 위에서 만든 인증 정보를 설정합니다.
        //    이 시점부터 해당 사용자는 '인증된' 상태가 됩니다.
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // 9. 다음 필터로 요청을 전달합니다.
        filterChain.doFilter(request, response);
    }
}