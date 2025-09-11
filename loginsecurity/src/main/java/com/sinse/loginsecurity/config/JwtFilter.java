package com.sinse.loginsecurity.config;

import com.sinse.loginsecurity.service.JpaUserDetailsService;
import com.sinse.loginsecurity.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final JpaUserDetailsService jpaUserDetailsService;

    @Override
    //Filter의 doFilter 메서드는 어떤 요청이 오든 다 필터링 하게끔 설계되어 있음.
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        log.debug("8. 요청이 들어온 이후 가동된 필터 입니다 ");
        // 1. "Authorization" 헤더에서 토큰을 가져옵니다.
        String authorization = request.getHeader("Authorization");
        log.debug("9. authorization은 무엇인가요????"+authorization);

        // 2. 토큰이 없거나, "Bearer "로 시작하지 않으면 인증을 시도하지 않고 다음 필터로 넘어갑니다.
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        log.debug("10. 'Bearer '로 시작하는 토큰이 존재하는 것을 확인 했습니다. 다음 단계로 넘어갑니다.");

        // 3. "Bearer " 부분을 제거하고 순수 토큰만 추출합니다.
        String token = authorization.split(" ")[1];
        log.debug("11. 'Bearer '를 제거하고 순수하게 추출한 토큰 값은====="+token);

        // 4. 토큰이 만료되었는지 확인하고, 만료되었다면 인증을 진행하지 않습니다.
        if (jwtUtil.isExpired(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 5. 토큰에서 username을 추출합니다.
        String username = jwtUtil.getUsername(token);

        // 6. username으로 UserDetails 객체(사용자 정보)를 조회합니다.
        UserDetails userDetails = jpaUserDetailsService.loadUserByUsername(username);
        log.debug("16. userDetails를 DB에서 잘 꺼내왔습니다. 이 로그는 한번만 출력되어야 합니다.");

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