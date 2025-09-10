package com.sinse.loginsecurity.controller;

import com.sinse.loginsecurity.config.CustomUserDetails;
import com.sinse.loginsecurity.dto.UserDTO;
import com.sinse.loginsecurity.util.JwtUtil;
import com.sinse.loginsecurity.util.LogCounter;
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
    private final LogCounter logCounter;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
        /*UserDTO의 멤버변수와 html단에서 JSON으로 매핑된 key값이 일치하도록 매핑해 주어야 함
            아니라면 @JsonProperty 사용필요
          현재 넘어오는 JSON객체 양식
          body: JSON.stringify({ username: username, password: password }
            */
    public Map<String, String> login(@RequestBody UserDTO userDTO) {
        log.debug("현재까지 찍혀야 하는 log의 마지막 번호는"+logCounter.getCount());
        log.debug("1. 들어와서 userDTO에 저장된 정보의 정체는 " + userDTO.toString());
        log.debug("2. 들어와서 userDTO에 저장된 유저ID는 " + userDTO.getUsername());
        log.debug("3. 들어와서 userDTO에 저장된 유저의 비밀번호는 " + userDTO.getPassword());

        // 1. 사용자 인증을 시도합니다.
        //    UsernamePasswordAuthenticationToken은 인증 요청을 나타내는 객체입니다.
        // 이 곳애서 authenticationManager가 내부적으로 비밀번호 인증로직을 실행하여 hash값으로 생성
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDTO.getUsername(), userDTO.getPassword()));
        log.debug("4. authenticationManager로 검증해서 가져온 객체 ===>"+authentication);

        // 2. 인증에 성공하면, 인증된 사용자의 상세 정보를 가져옵니다.
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        log.debug("5. getPrincipal()로 가져와 담아낸 userDetails 정보는" +userDetails);
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
        log.debug("6.로그인 정보가 담긴 UserDetails객체에서 추출해낸 role은"+role);
        // 4. JwtUtil을 사용하여 JWT를 생성합니다. (유효시간: 1시간)
        String token = jwtUtil.createJwt(username, role, 60 * 60 * 1000L);
        log.debug("7. 로그인 정보를 사용(JwtUtil객체를 통해)해 만든 token 문자열은 == "+token+"\n이 토큰을 client에게 반환합니다.");
        // 5. 생성된 토큰을 "token"이라는 키와 함께 JSON 형태로 클라이언트에게 반환합니다.
        return Map.of("token", token);
    }
}