package com.sinse.loginsecurity.controller;

import com.sinse.loginsecurity.config.CustomUserDetails;
import com.sinse.loginsecurity.dto.UserDTO;
import com.sinse.loginsecurity.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody UserDTO userDTO) {

        // 1. 사용자 인증을 시도합니다.
        //    UsernamePasswordAuthenticationToken은 인증 요청을 나타내는 객체입니다.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDTO.getUsername(), userDTO.getPassword()));

        // 2. 인증에 성공하면, 인증된 사용자의 상세 정보를 가져옵니다.
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        // 3. 사용자의 권한(Role) 정보를 추출합니다.
        //    (참고: 현재 CustomUserDetails가 권한을 제대로 반환하지 않으므로, 이 부분은 추후 수정이 필요합니다.)
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        String role = null;
        if (iterator.hasNext()) {
            GrantedAuthority auth = iterator.next();
            role = auth.getAuthority();
        }

        // 4. JwtUtil을 사용하여 JWT를 생성합니다. (유효시간: 1시간)
        String token = jwtUtil.createJwt(username, role, 60 * 60 * 1000L);

        // 5. 생성된 토큰을 "token"이라는 키와 함께 JSON 형태로 클라이언트에게 반환합니다.
        return Map.of("token", token);
    }
}