# Access & Refresh Token Backend Implementation Summary

## 개요 (Overview)
이 문서는 JWT(JSON Web Token) 기반 인증 시스템에서 Access Token과 Refresh Token의 백엔드 발급 및 처리 로직을 요약합니다. 현재 구현은 토큰 발급과 Access Token을 이용한 기본적인 인증 필터링에 중점을 둡니다.

## 주요 구성 요소 (Key Components)

*   **`JwtUtil.java`**:
    *   JWT 생성 (Access Token 및 Refresh Token).
    *   토큰 유효성 검사.
    *   토큰에서 사용자 이름, 만료 시간 등 정보 추출.
    *   `HS256` 알고리즘과 시크릿 키를 사용하여 토큰 서명.

*   **`UserController.java`**:
    *   사용자 로그인 (`/login`) 및 회원가입 (`/register`) 요청 처리.
    *   로그인 성공 시, `JwtUtil`을 사용하여 Access Token과 Refresh Token을 생성하고, 이를 HTTP 응답 헤더에 담아 클라이언트에게 전송합니다.

*   **`SecurityConfig.java`**:
    *   Spring Security의 전반적인 설정을 담당합니다.
    *   `JwtFilter`를 `UsernamePasswordAuthenticationFilter` 이전에 추가하여 모든 요청에 대해 JWT 기반 인증을 수행하도록 구성합니다.
    *   세션 관리를 `STATELESS`로 설정하여 JWT의 무상태(stateless) 특성을 활용합니다.
    *   로그인 및 회원가입 엔드포인트 (`/login`, `/register`)는 인증 없이 접근 가능하도록 허용합니다.

*   **`JwtFilter.java`**:
    *   모든 HTTP 요청을 가로채어 요청 헤더에서 JWT(Access Token)를 추출합니다.
    *   `JwtUtil`을 사용하여 추출된 토큰의 유효성을 검사합니다.
    *   토큰이 유효하고 만료되지 않았다면, 토큰에서 사용자 정보를 추출하여 `UsernamePasswordAuthenticationToken` 객체를 생성하고 `SecurityContextHolder`에 설정하여 현재 요청에 대한 인증 정보를 제공합니다.

## 토큰 발급 흐름 (Token Issuance Flow)

1.  **로그인 요청**: 사용자가 `/login` 엔드포인트로 사용자 이름과 비밀번호를 포함한 로그인 요청을 보냅니다.
2.  **인증 처리**: `UserController`에서 사용자 인증을 시도합니다.
3.  **토큰 생성**: 인증이 성공하면, `JwtUtil`의 `createJwt` 메서드를 사용하여 Access Token과 Refresh Token을 생성합니다. Access Token은 짧은 유효 기간을 가지며, Refresh Token은 더 긴 유효 기간을 가집니다.
4.  **토큰 응답**: 생성된 Access Token과 Refresh Token은 HTTP 응답 헤더 (`Authorization`, `Refresh-Token`)에 담겨 클라이언트에게 전송됩니다.

## 토큰 검증 흐름 (Token Validation Flow)

1.  **보호된 리소스 요청**: 클라이언트가 발급받은 Access Token을 HTTP `Authorization` 헤더에 `Bearer` 접두사와 함께 포함하여 보호된 리소스에 요청을 보냅니다.
2.  **필터링**: `JwtFilter`가 모든 들어오는 요청을 가로챕니다.
3.  **토큰 추출**: `JwtFilter`는 요청 헤더에서 Access Token을 추출합니다.
4.  **토큰 유효성 검사**: `JwtUtil`의 `isExpired` 및 `getUserId` 메서드를 사용하여 토큰의 유효성(만료 여부, 서명 유효성)을 검사합니다.
5.  **인증 정보 설정**: 토큰이 유효하면, `JwtUtil`에서 추출한 사용자 ID를 기반으로 `UserDetails` 객체를 로드하고, 이를 사용하여 `UsernamePasswordAuthenticationToken`을 생성한 후 `SecurityContextHolder`에 설정합니다.
6.  **요청 처리**: 인증 정보가 설정된 후, 요청은 다음 필터 체인으로 진행되어 보호된 리소스에 접근할 수 있게 됩니다.

## 사용된 라이브러리 (Libraries Used)

*   `io.jsonwebtoken:jjwt-api:0.11.5`
*   `io.jsonwebtoken:jjwt-impl:0.11.5`
*   `io.jsonwebtoken:jjwt-jackson:0.11.5`
*   `org.springframework.boot:spring-boot-starter-security`
*   `org.springframework.boot:spring-boot-starter-data-jpa`
*   `mysql:mysql-connector-java`
*   `org.springframework.boot:spring-boot-starter-web`

## 현재 상태 및 다음 단계 (Current Status & Next Steps)

*   **현재 상태**: 백엔드에서 Access Token과 Refresh Token을 성공적으로 발급하고, Access Token을 이용한 기본적인 인증 필터링이 구현되어 있습니다.
*   **미구현**: Refresh Token을 이용한 Access Token 재발급 로직 (특히 Redis를 활용한 Refresh Token 저장 및 관리)은 아직 구현되지 않았습니다.
*   **클라이언트**: 클라이언트(`main.html`)에서 발급받은 토큰을 저장하고, 요청 시 헤더에 포함하여 보내는 프론트엔드 로직은 현재 제거된 상태입니다. 이 부분은 다음 단계에서 다시 구현해야 합니다.
