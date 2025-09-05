# Spring Security 로그인/로그아웃 프로젝트 요약

## 1. 프로젝트 목표
Spring Security를 활용하여 사용자 정의 로그인 및 로그아웃 기능을 구현하고, 관련 개념을 학습합니다.

## 2. 기술 스택
*   **JDK 버전:** 21
*   **데이터베이스:** MySQL
*   **ORM:** JPA/Hibernate
*   **뷰 레이어:** (현재는 정적 HTML, 향후 Vue 3.0 전환 예정)

## 3. 구현된 핵심 기능 및 학습 내용

### 3.1. 사용자 정의 로그인 폼
*   **`SecurityConfig.java` 설정:**
    *   `loginPage("/loginform")`: 사용자 정의 로그인 페이지 URL 지정.
    *   `loginProcessingUrl("/login")`: 로그인 폼 제출 시 처리될 URL 지정 (Spring Security가 내부적으로 처리).
    *   `defaultSuccessUrl("/main.html", true)`: 로그인 성공 시 리다이렉트될 페이지 지정 (이중 리다이렉트 문제 해결).
    *   `requestMatchers("/loginform.html", "/css/loginform.css", "/loginform").permitAll()`: 로그인 페이지 및 관련 리소스에 대한 접근 허용.

### 3.2. 사용자 엔티티 (`User.java`)
*   **패키지:** `com.sinse.loginsecurity.domain`
*   **JPA 매핑:** `@Entity`, `@Table(name="user")`, `@Id`, `@GeneratedValue(strategy=GenerationType.IDENTITY)`
*   **필드:** `userId` (PK), `username` (로그인 아이디, `NOT NULL`, `UNIQUE`), `password` (`NOT NULL`), `age`, `role` (`NOT NULL`, Spring Security 권한).
*   **학습 내용:**
    *   JPA 엔티티의 기본 구조 및 어노테이션.
    *   `@Column(nullable=false)`와 DB `NOT NULL` 제약조건의 동시 사용 (계층적 방어).
    *   Spring Security의 `username` 필드명 컨벤션.
    *   `ROLE_` 접두사 사용의 중요성.

### 3.3. 사용자 리포지토리 (`JpaUserRepository.java`)
*   **패키지:** `com.sinse.loginsecurity.repository`
*   **기능:** `JpaRepository<User, Integer>` 상속을 통한 기본적인 CRUD 기능 자동 제공.
*   **핵심 메소드:** `User findByUsername(String username)` (Spring Data JPA의 쿼리 메소드 기능 활용).

### 3.4. 사용자 상세 서비스 (`JpaUserDetailsService.java`)
*   **패키지:** `com.sinse.loginsecurity.service`
*   **역할:** Spring Security의 `UserDetailsService` 인터페이스 구현.
*   **핵심 메소드:** `loadUserByUsername(String username)`
    *   `JpaUserRepository`를 통해 DB에서 사용자 조회.
    *   사용자가 없을 경우 `UsernameNotFoundException` 발생.
    *   조회된 `User` 객체를 `CustomUserDetails`로 변환하여 반환.
*   **학습 내용:** Spring Security와 도메인 모델 간의 연결 고리 역할.

### 3.5. 사용자 상세 정보 어댑터 (`CustomUserDetails.java`)
*   **패키지:** `com.sinse.loginsecurity.config`
*   **역할:** `UserDetails` 인터페이스 구현. `User` 도메인 객체를 Spring Security가 이해하는 `UserDetails` 타입으로 변환하는 어댑터.
*   **핵심 구현:**
    *   `User` 객체를 필드로 가지고 생성자를 통해 주입.
    *   `getUsername()`, `getPassword()`: `user` 객체의 해당 필드 값 반환.
    *   `getAuthorities()`: `user.getRole()` 문자열을 `SimpleGrantedAuthority`로 변환하여 `Collection` 형태로 반환.

### 3.6. 비밀번호 암호화 (`PasswordEncoder`)
*   **`SecurityConfig.java` 설정:** `@Bean`으로 `BCryptPasswordEncoder` 등록.
*   **역할:** 사용자 비밀번호를 안전하게 암호화하여 저장하고, 로그인 시 입력된 비밀번호와 비교.

### 3.7. 테스트 데이터 생성 (`CommandLineRunner`)
*   **`LoginsecurityApplication.java` 설정:** `@Bean`으로 `CommandLineRunner` 등록.
*   **역할:** 애플리케이션 시작 시점에 테스트용 사용자(`lee`/`좋다`, `ROLE_USER`)를 DB에 자동 생성 (중복 방지 로직 포함).

### 3.8. 로그아웃 기능
*   **`SecurityConfig.java` 설정:**
    *   `.logout(logout -> logout ...)` 체인 추가.
    *   `logoutUrl("/logout")`: 로그아웃 요청 URL.
    *   `logoutSuccessUrl("/loginform.html")`: 로그아웃 성공 시 리다이렉트될 페이지.
    *   `invalidateHttpSession(true)`: 세션 무효화.
    *   `deleteCookies("JSESSIONID")`: 쿠키 삭제.
*   **`main.html`:** `/logout`으로 `POST` 요청을 보내는 폼 또는 링크 추가.
*   **학습 내용:** 로그아웃은 `GET`이 아닌 `POST` 요청으로 처리하는 것이 보안상 권장됨.

## 4. 향후 학습 및 개선 과제 (gemini.md 참조)
*   **`Optional` 사용 리팩토링:** `JpaUserRepository`의 `findByUsername` 메소드 등에서 `Optional`을 사용하여 Null 안정성 강화.
*   **`User` 엔티티 컨벤션 리팩토링:** PK를 `id` (`Long` 타입)로 변경하는 등 JPA 컨벤션에 맞게 `User` 엔티티 구조 개선.
*   **커스텀 `/login` 매핑 구현:** 학습 목적으로 Spring Security의 기본 처리 방식 대신 직접 `/login` 매핑을 구현해보기.
*   **Vue.js SPA 아키텍처 전환:** Spring Boot를 API 서버로 전환하고 Vue.js 프론트엔드와 연동하는 아키텍처 구현.
*   **프로젝트 개발 과정 요약 파일 생성:** (현재 이 파일)
*   **사용자 학습 수준 피드백:** (완료)
