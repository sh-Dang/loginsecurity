package com.sinse.loginsecurity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 프로젝트 전반적으로 (재)사용하는 기능, 툴 들을 모아놓음
 *
 * @since 2025/09/12
 *
 */
@Configuration
public class AppConfig {

    /**
     * 회원가입 시, 로그인 시 마다 암호화를 통한 검증로직은 구현해 놓았으므로
     *
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
