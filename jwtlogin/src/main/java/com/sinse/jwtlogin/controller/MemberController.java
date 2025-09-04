package com.sinse.jwtlogin.controller;

import com.sinse.jwtlogin.domain.CustomUserDetails;
import com.sinse.jwtlogin.model.member.CustomUserDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@Slf4j
public class MemberController {

    private CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    public MemberController(CustomUserDetailsService customUserDetailsService, PasswordEncoder passwordEncoder) {
        this.customUserDetailsService = customUserDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/member/login")
    public ResponseEntity<?> login(String username, String password) {
        log.debug("로그인요청받음");
        CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(username);
        //추출한 user의 비밀번호와 파라미터로 넘어온 유저의 비밀번호를 비교
        if(userDetails != null && passwordEncoder.matches(password, userDetails.getPassword())) {
            //로그인 성공하였으므로. JWT 발급
            log.debug("로그인이 완료됐습니다.");
        }
        return null;
    }

}
