package com.sinse.loginsecurity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정을 담당하는 구성 클래스입니다.
 *
 * <p>
 * - 사용자 정의 로그인 페이지를 설정합니다.
 * - 필요에 따라 인증/인가, CSRF, 세션 관리 등을 추가할 수 있습니다.
 * </p>
 * @Author : 이세형
 * @Configuration : Spring Configuration 클래스임을 나타냅니다.
 * @EnableWebSecurity : Spring Security 기능 활성화
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    /**
     * Spring Security 필터 체인을 설정하는 메서드입니다.
     *
     * <p>
     * 현재는 사용자 정의 로그인 페이지("/login")만 설정되어 있으며,
     * 향후 다른 HTTP 보안 설정(인가, CSRF 등)을 추가할 수 있습니다.
     * </p>
     *
     * @param http HttpSecurity 객체, 다양한 보안 설정 가능
     * @return SecurityWebFilterChain 생성된 Spring Security 필터 체인
     * @throws Exception 보안 설정 중 예외 발생 가능
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf ->csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/loginform.html", "/css/loginform.css", "/loginform").permitAll()
                        .anyRequest().authenticated()
                    )
                .formLogin(form -> form
                    .loginPage("/loginform")
                    .loginProcessingUrl("/login")
                    .usernameParameter("username")
                    .passwordParameter("password")
                    .defaultSuccessUrl("/main.html", true)
                    .permitAll()
                )
                .logout(logout->logout //로그아웃 설정 시작
                        .logoutUrl("/logout") //로그아웃 url
                        .logoutSuccessUrl("/loginform.html") //로그아웃 시 접속할 url
                        .invalidateHttpSession(true) //현재 세션 무효화
                        .deleteCookies("JSESSIONID") //JSESSIONID 쿠키 삭제
                );
        ;

        return http.build();
    }
}
