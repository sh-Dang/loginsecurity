package com.sinse.loginsecurity.config;

import com.sinse.loginsecurity.service.JpaUserDetailsService;
import com.sinse.loginsecurity.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final JpaUserDetailsService jpaUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF(Cross-Site Request Forgery) 보호 기능을 비활성화합니다.
                // JWT를 사용하는 stateless 인증 방식에서는 서버에 인증 정보를 저장하지 않기 때문에
                // CSRF 공격에 비교적 안전하며, 매 요청마다 CSRF 토큰을 검증할 필요가 없습니다.
                .csrf(AbstractHttpConfigurer::disable)

                // Form 기반 로그인을 비활성화합니다.
                // JWT 인증 방식에서는 클라이언트가 직접 로그인 폼을 통해 API를 호출하여 토큰을 발급받으므로,
                // Spring Security가 제공하는 기본 Form 로그인 화면과 처리 로직이 필요 없습니다.
                .formLogin(AbstractHttpConfigurer::disable)

                // HTTP Basic 인증 방식을 비활성화합니다.
                // HTTP Basic 인증은 사용자 이름과 비밀번호를 Base64로 인코딩하여 매 요청 헤더에 보내는 방식입니다.
                // 우리는 Bearer 토큰 방식을 사용할 것이므로, 이 기능은 필요 없습니다.
                .httpBasic(AbstractHttpConfigurer::disable)

                // 세션 관리 정책을 STATELESS(상태 비저장)으로 설정합니다.
                // 서버가 클라이언트의 세션 상태를 저장하지 않음을 의미합니다.
                // 모든 요청은 독립적으로 처리되어야 하며, 각 요청은 헤더의 JWT를 통해 인증됩니다.
                // 이는 확장성 있는 마이크로서비스 아키텍처에 필수적인 설정입니다.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 요청별 접근 권한을 설정합니다.
                .authorizeHttpRequests(auth -> auth
                        // "/login", "/register", "/" 경로는 인증 없이 모든 사용자가 접근할 수 있도록 허용합니다.
                        // 로그인과 회원가입 기능은 인증되지 않은 사용자도 이용할 수 있어야 합니다.
                        .requestMatchers("/login", "/register", "/", "/registerform.html", "/loginform.html", "/css/*", "/reissue", "/main.html", "/info").permitAll()
                        // 위에서 허용한 경로를 제외한 모든 나머지 요청은 반드시 인증을 거쳐야 합니다.
                        .anyRequest().authenticated()
                )
                // 우리가 직접 구현한 JwtFilter를 Spring Security의 필터 체인에 추가합니다.
                // UsernamePasswordAuthenticationFilter는 Spring Security의 기본 아이디/비밀번호 로그인 처리 필터인데,
                // 이 필터가 실행되기 '전에(Before)' JwtFilter를 먼저 실행하여 JWT 토큰 기반의 인증을 우선적으로 처리하도록 합니다.
                .addFilterBefore(new JwtFilter(jwtUtil, jpaUserDetailsService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    @Bean
    public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        /**
         * "아, 개발자가 DB를 이용한 아이디/비밀번호 인증을 하려는구나! 그렇다면
         `DaoAuthenticationProvider`가 필요하겠네. 내가 대신 만들어 줘야겠다!"

         그리고는 아래와 같은 작업을 자동으로 수행합니다.

         * DaoAuthenticationProvider라는 이름의 AuthenticationProvider를 하나 생성합니다.
         따라서 우리가 AuthenticationManager를 빈으로 등록했을 때, 그 AuthenticationManager는 이미
         Spring Boot가 자동으로 만들어준 DaoAuthenticationProvider를 알고 있고, 그에게 인증 작업을
         위임할 준비를 마친 상태인 것입니다.

         언제 `AuthenticationProvider`를 직접 만드나요?
         외부 API를 통한 인증, SMS인증 등 일반 로그인과는 다른 로직이 필요한 경우
         * */
        return authenticationConfiguration.getAuthenticationManager();
    }

}