# 프로젝트 개발 요약: 세션 인증에서 JWT 인증으로의 전환

이 문서는 기존의 세션 기반 로그인 시스템을 현대적인 JWT(JSON Web Token) 기반의 stateless 인증 시스템으로 전환하는 전체 개발 과정을 기록합니다.

## 1단계: 초기 문제 분석 및 기반 다지기

- **초기 상태:** Spring Security의 `formLogin`을 사용하는 기본적인 세션 기반 로그인 시스템.
- **목표:** 세션을 사용하지 않는(stateless) JWT 인증 방식으로 전환.
- **문제 식별:**
    - `SecurityConfig` 내부에 `PasswordEncoder`가 정의되어 있어, 향후 다른 서비스와의 의존성 주입 시 '순환 참조'가 발생할 위험이 식별됨.

## 2단계: JWT 인프라 구축

JWT 시스템의 핵심을 이루는 기반 컴포넌트들을 준비했습니다.

### 2.1. `JwtUtil.java` 생성
- **역할:** JWT의 생성, 파싱, 유효성 검증을 담당하는 유틸리티 클래스.
- **주요 구현:**
    - `jjwt` 라이브러리를 사용하여 토큰 생성(`createJwt`), 사용자 정보 추출(`getUsername`, `getRole`), 만료 여부 확인(`isExpired`) 메서드를 구현.
    - **(중요)** `SecretKey` 생성 시, `new SecretKeySpec()` 대신 `Keys.hmacShaKeyFor()`를 사용하도록 수정하여 JJWT 라이브러리와의 호환성 및 보안을 강화함.

### 2.2. `application.properties` 설정
- **역할:** 민감한 정보 및 설정 값을 코드와 분리.
- **주요 구현:**
    - JWT 서명에 사용될 비밀키(`spring.jwt.secret`)를 추가.
    - JWT 만료 시간(`spring.jwt.expiration`)을 추가하여 유연성을 확보.

## 3단계: Spring Security와 JWT 통합

JWT 인프라를 Spring Security의 필터 체인에 통합하는 작업을 진행했습니다.

### 3.1. `JwtFilter.java` 생성
- **역할:** 클라이언트의 모든 요청을 가로채는 관문.
- **주요 구현:**
    - `OncePerRequestFilter`를 상속받아 모든 요청에 대해 한 번만 실행되도록 보장.
    - 요청의 `Authorization` 헤더에서 "Bearer " 토큰을 추출.
    - `JwtUtil`을 사용해 토큰을 검증하고, 유효한 경우 `SecurityContextHolder`에 인증 정보를 등록하여 해당 요청 동안 사용자를 '인증된 상태'로 만듦.

### 3.2. `AppConfig.java` 분리
- **역할:** `PasswordEncoder`를 `SecurityConfig`로부터 분리하여 순환 참조 문제 해결.
- **주요 구현:**
    - `@Configuration` 클래스를 새로 만들고, `PasswordEncoder`를 생성하는 `@Bean`을 이곳으로 이전.

### 3.3. `SecurityConfig.java` 재설계
- **역할:** JWT 인증 환경에 맞게 Spring Security 동작 방식을 재설정.
- **주요 구현:**
    - **Stateless 설정:** `csrf`, `formLogin`, `httpBasic` 기능을 비활성화하고, 세션 관리 정책을 `SessionCreationPolicy.STATELESS`로 변경.
    - **`AuthenticationManager` 빈 등록:** `UserController`에서 표준적인 인증 절차를 수행할 수 있도록 `AuthenticationManager`를 빈으로 노출.
    - **(핵심)** `addFilterBefore()`를 사용하여 우리가 만든 `JwtFilter`를 `UsernamePasswordAuthenticationFilter` 앞에 등록. 이를 통해 아이디/비밀번호 인증보다 JWT 검증이 먼저 일어나도록 보장.

## 4단계: API 엔드포인트 및 데이터 흐름 재구성

실제 사용자 인증을 처리하고 JWT를 발급하는 API를 구현했습니다.

### 4.1. DTO 도입 (`UserDTO.java`)
- **역할:** API 계층과 서비스/도메인 계층의 관심사 분리.
- **주요 결정:**
    - API 요청/응답 시, 데이터베이스 구조와 1:1로 대응되는 엔티티(`User`)를 직접 사용하지 않기로 결정.
    - 이는 시스템 내부 구조 노출을 방지하고, API 명세의 안정성을 높여 보안과 유지보수성을 향상시킴.
    - 로그인 요청에 필요한 `username`, `password` 필드만 가진 `UserDTO`를 생성.

### 4.2. `UserController` 로그인 API 구현
- **역할:** JWT 발급의 시작점.
- **주요 구현:**
    - `@RestController`로 변경하여 데이터(JSON)를 반환하도록 설정.
    - `POST /login` API 엔드포인트를 생성.
    - `AuthenticationManager`에 인증을 위임하여 안전하게 사용자 인증을 수행.
    - 인증 성공 시, `JwtUtil`을 호출하여 JWT를 생성하고 클라이언트에게 JSON 형태로 반환.

## 5단계: 사용자 권한 처리 완성

인증의 마지막 조각인 '권한(Authorization)' 정보를 JWT에 올바르게 담도록 수정했습니다.

### 5.1. `CustomUserDetails.java` 수정
- **역할:** DB의 사용자 정보를 Spring Security가 이해하는 `UserDetails` 형태로 변환.
- **주요 구현:**
    - 기존에 비어있던 `getAuthorities()` 메서드를 재구현.
    - `User` 엔티티가 가진 `Role` 정보를 가져와, Spring Security 표준인 `ROLE_` 접두사를 붙인 `SimpleGrantedAuthority` 객체로 변환하여 반환.
    - 계정 만료/잠김 여부 등을 반환하는 메서드들을 오버라이드하여 안정성을 높임.

## 결론

위의 단계를 통해, 프로젝트는 이제 외부 요청을 `JwtFilter`로 검증하고, `/login` API를 통해 상태 없는(stateless) JWT를 발급하는 현대적인 인증 시스템을 갖추게 되었습니다.
