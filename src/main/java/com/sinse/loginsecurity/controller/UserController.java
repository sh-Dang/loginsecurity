package com.sinse.loginsecurity.controller;

import com.sinse.loginsecurity.config.CustomUserDetails;
import com.sinse.loginsecurity.domain.Role;
import com.sinse.loginsecurity.domain.User;
import com.sinse.loginsecurity.dto.UserDTO;
import com.sinse.loginsecurity.repository.JpaRoleRepository;
import com.sinse.loginsecurity.util.JwtUtil;
import com.sinse.loginsecurity.util.LogCounter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import com.sinse.loginsecurity.repository.JpaUserRepository;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final LogCounter logCounter;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final JpaUserRepository jpaUserRepository;
    private final JpaRoleRepository jpaRoleRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * login 로직을 구현한 메서드
     * UserDTO의 멤버변수와 html단에서 JSON으로 매핑된 key값이 일치하도록 매핑해 주어야 함
     * 아니라면 @JsonProperty 사용필요
     * +)현재 넘어오는 JSON객체 양식
     * body: JSON.stringify({ username: username, password: password }
     *
     * @return Map을 반환. -> 내부에 더 다양한 정보를 담기위해 ResponseEntity반환
     * @author 이세형
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody UserDTO userDTO, HttpServletResponse response) {
        log.debug("현재까지 찍혀야 하는 log의 마지막 번호는" + logCounter.getCount());
        log.debug("1. 들어와서 userDTO에 저장된 정보의 정체는 " + userDTO.toString());
        log.debug("2. 들어와서 userDTO에 저장된 유저ID는 " + userDTO.getUsername());
        log.debug("3. 들어와서 userDTO에 저장된 유저의 비밀번호는 " + userDTO.getPassword());

        // 1. 사용자 인증을 시도합니다.
        //    UsernamePasswordAuthenticationToken은 인증 요청을 나타내는 객체입니다.
        // 이 곳애서 authenticationManager가 내부적으로 비밀번호 인증로직을 실행하여 hash값으로 생성
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDTO.getUsername(), userDTO.getPassword()));
        log.debug("4. authenticationManager로 검증해서 가져온 객체 ===>" + authentication);

        // 2. 인증에 성공하면, 인증된 사용자의 상세 정보를 가져옵니다.
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        log.debug("5. getPrincipal()로 가져와 담아낸 userDetails 정보는" + userDetails);
        String username = userDetails.getUsername();

        // 3. 사용자의 권한(Role) 정보를 추출합니다.
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        String role = null;
        if (iterator.hasNext()) {
            GrantedAuthority auth = iterator.next();
            role = auth.getAuthority();
        }
        log.debug("6.로그인 정보가 담긴 UserDetails객체에서 추출해낸 role은" + role);

        // 4. JwtUtil을 사용하여 JWT(accessToken)를 생성합니다. (유효시간 : 15분)
        String accessToken = jwtUtil.createJwt(username, role, 1 * 15 * 1000L);
        log.debug("7. 로그인 정보를 사용(JwtUtil객체를 통해)해 만든 accessToken 문자열은 == " + accessToken + "\n이 토큰을 client에게 반환합니다.");

        // 4.1. JWTUtil을 사용하여 RefreshToken을 생성합니다. (유효시간 : 24시간)
        String refreshToken = jwtUtil.createJwt(username, role, 24 * 60 * 60 * 1000L);
        log.debug("17. 로그인 정보를 사용해 만든 refreshToken 문자열은 === " + refreshToken + "\n이 문자열은 반영구 쿠키에 저장됩니다.");

        // 4.1.1 쿠키 생성 및 설정
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setMaxAge(24 * 60 * 60); //쿠키 유효시간
        cookie.setHttpOnly(true); // JavaScript 접근 방지
        cookie.setPath("/"); //전체 경로에서 쿠키 사용
        //HTTPS 환경에서만 쿠키전송 (배포시 활성화)
//        cookie.setSecure(true);

        // 4.1.2 응답에 쿠키 추가
        response.addCookie(cookie);
        log.debug("18. 응답에 RefreshToken 쿠키 추가(영구)");

        //4.2 Redis에 Refresh Token 저장(유효시간 : 24시간)
        redisTemplate.opsForValue().set(
                username,
                refreshToken,
                24, // 만료시간
                java.util.concurrent.TimeUnit.HOURS // 시간단위
        );
        // logger문법도 하나 더 배웠음
        log.debug("Redis에 RefreshToken을 저장했습니다. Key: {}, TTL: 24시간", username);

        // 5. 생성된 토큰을 "token"이라는 키와 함께 JSON 형태로 클라이언트에게 반환합니다.
        return ResponseEntity.ok(Map.of("token", accessToken));
    }

    /**
     * 액세스, 리프레시 토큰 재발급 요청을 구현한 메서드
     * +)HttpServletResponse객체를 다루는 이유는 Cookie를 컨트롤 해야하기 때문
     *
     * @return Map을 반환. -> 내부에 더 다양한 정보를 담기위해 ResponseEntity반환
     * @author 이세형
     *
     */
    @PostMapping("/reissue")
    public ResponseEntity<Map<String, String>> reissue(@CookieValue("refreshToken") String oldRefreshToken, HttpServletResponse response) {
        log.debug("20. 액세스(리프레시) 토큰 재발급 요청이 들어왔습니다.");

        // 1. 리프레시 토큰 검증
        if (oldRefreshToken == null || oldRefreshToken.isEmpty()) {
            log.debug("21. 리프레시 토큰 검증에 실패 했습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "리프레시 토큰이 없습니다."));
        }

        // 2. 리프레시토큰 만료여부 확인 및 현존 쿠키 삭제
        try {
            if (jwtUtil.isExpired(oldRefreshToken)) {
                log.warn("이미 만료된 리프레시 토큰 입니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "이미 만료된 리프레시 토큰입니다."));
            }
        } catch (Exception e) {
            log.warn("리프레시 토큰 검증 중 오류발생 : {}", e.getMessage());
            // 쿠키를 삭제. 클라이언트의 재 로그인 유도
            Cookie cookie = new Cookie("refreshToken", null);
            cookie.setMaxAge(0);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            response.addCookie(cookie);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "유효하지 않은 토큰"));
        }

        // 3. 토큰에서 username과 role 추출
        String username = jwtUtil.getUsername(oldRefreshToken);
        String role = jwtUtil.getRole(oldRefreshToken);
        log.debug("22. 리프레시 토큰에서 추출해온 값이 어떻게 들어왔냐면 username === {}, role === {}", username, role);

        /* 4. Redis에서 저장된 리프레시 토큰 조회
            Redis문법 SETEX 'username', {시간}, '토큰의 값' 을 넣어서
            username : 토큰의값이 들어갔기 때문에 바로 토큰 조회라고 볼 수 있는 것
        */
        String savedRefreshToken = (String) redisTemplate.opsForValue().get(username);

        //5. Redis에 토큰이 없거나, 요청된 초큰과 일치하지 않는 경우 (보안 위협)
        if (savedRefreshToken == null || !savedRefreshToken.equals(oldRefreshToken)) {
            log.warn("Redis에 저장된 토큰과 가진 토큰이 일치하지 않습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "리프레시 토큰 정보가 유효하지 않습니다."));
        }

        // 5.1 토큰이 유효(일치)하는경우 새로운 토큰들을 생성
        log.debug("23. 토큰들이 모두 유효합니다. 새로운 토큰들 발급 프로세스에 진입합니다.");

        // 5.1.1 새로운 액세스토큰 생성
        String newAccessToken = jwtUtil.createJwt(username, role, 15 * 60 * 1000L);

        // 5.1.2 새로운 리프레시 토큰 생성
        String newRefreshToken = jwtUtil.createJwt(username, role, 24 * 60 * 60 * 1000L);

        // 5.2 새로운 리프레시 토큰을 Redis, Cookie에 업데이트
        //Redis
        redisTemplate.opsForValue().set(username, newRefreshToken, 24, java.util.concurrent.TimeUnit.HOURS);
        //Cookie
        Cookie cookie = new Cookie("refreshToken", newRefreshToken);
        cookie.setMaxAge(24 * 60 * 60);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);


        // 6. 새로 생성된 액세스 토큰 반환
        return ResponseEntity.ok(Map.of("token", newAccessToken));
    }


    /**
     * 회원가입(register) 로직을 구현한 메서드
     * 아니라면 @JsonProperty 사용필요
     *
     * @return ResponseEntity-> 회원가입 완료 여부를 ok(200)으로 반환
     * @author 이세형
     */
    @PostMapping("/register")
    @Transactional
    public ResponseEntity<String> register(@RequestBody UserDTO userDTO) {
        log.debug("13. 회원가입 요청 들어옴======");
        // 1. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(userDTO.getPassword());

        // 2. Role 존재 여부 확인 및 예외 처리
        Role role = jpaRoleRepository.findByRoleName(userDTO.getRole())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 역할입니다."));
        log.debug("14. USER값을 가진 role 객체는?" + role.toString());

        // 3. User 객체 생성 및 데이터 설정
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(encodedPassword); // 암호화된 비밀번호 설정
        user.setAge(userDTO.getAge()); // DTO에서 age 꺼내옴
        user.setRole(role); // 조회된 Role 설정

        // 4. DB에 User 저장
        jpaUserRepository.save(user);

        // 5. 성공 응답 반환
        return ResponseEntity.ok("회원가입이 성공적으로 완료되었습니다.");
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info(Authentication authentication) {
        log.debug("24. 내 정보 가져오기 메서드를 호출합니다(매핑이 잘 되었는지? O)");
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        Map<String, Object> userInfo = Map.of("username", username,
                "role", authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        return ResponseEntity.ok(userInfo);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(Authentication authentication, HttpServletResponse response) {
        log.debug("25. 로그아웃 메서드를 호출합니다(매핑이 잘 되었는지? O)");
        // 1. Redis에서 Refresh Token 삭제
        if (authentication != null) {
            String username = authentication.getName();
            redisTemplate.delete(username);
            log.debug("Redis에서 사용자 '{}'의 리프레시 토큰을 삭제했습니다.", username);
        }

        // 2. 브라우저의 Refresh Token 쿠키 삭제
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.ok("success");
    }
}
