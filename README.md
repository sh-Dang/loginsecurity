# 로그인 보안 프로젝트 (JWT & Redis 기반)

## 1. 프로젝트 개요

이 프로젝트는 기존의 세션 기반 로그인 시스템을 현대적인 JWT(JSON Web Token) 기반의 인증 시스템으로 전환합니다. 또한, Refresh Token 관리에 Redis를 도입하여 확장성과 보안을 강화하고, 안전한 회원가입 기능을 통해 사용자 인증의 전반적인 흐름을 구축하는 것을 목표로 합니다.

## 2. 주요 기술 스택

*   **백엔드:**
    *   Java 21
    *   Spring Boot
    *   Spring Security (6.x)
    *   JPA / Hibernate
    *   **Redis (for Token Management)**
    *   Lombok
*   **프론트엔드:**
    *   HTML5
    *   CSS3
    *   JavaScript (순수 JS)
*   **데이터베이스:**
    *   MySQL

## 3. 주요 기능

### 3.1. JWT 기반 로그인

*   사용자 이름과 비밀번호를 통한 인증.
*   인증 성공 시 서버에서 JWT(Access Token) 발급 및 클라이언트(`localStorage`)에 저장.
*   발급된 JWT를 `Authorization` 헤더에 담아 보호된 리소스에 접근.
*   `JwtFilter`를 통해 모든 요청에 대한 JWT 유효성 검사 및 인증 처리.
*   Stateless(무상태) 인증 방식으로 서버 부하 감소 및 확장성 확보.

### 3.2. 리프레시 토큰을 이용한 세션 관리 및 안전한 로그아웃

*   **액세스 토큰 만료 대응:** 액세스 토큰(Access Token)의 짧은 유효 기간 만료 시, 사용자가 다시 로그인하는 불편함 없이 세션을 유지합니다.
*   **리프레시 토큰(Refresh Token) 발급:** 로그인 시 액세스 토큰과 함께 긴 유효 기간을 가진 리프레시 토큰을 발급하여 `HttpOnly` 쿠키에 안전하게 저장합니다.
*   **Redis를 통한 토큰 관리:** 발급된 리프레시 토큰은 사용자와 1:1로 매핑하여 Redis에 저장합니다. 이를 통해 서버는 토큰의 유효성을 검증하고, 필요 시 강제로 세션을 종료시킬 수 있습니다.
*   **자동 토큰 재발급:** 액세스 토큰 만료 시, 클라이언트는 자동으로 `/reissue` API를 호출하여 Redis에 저장된 리프레시 토큰과 비교 검증 후 새로운 액세스 토큰을 발급받습니다.
*   **보안 로그아웃:** 로그아웃 시, Redis와 브라우저 쿠키에서 리프레시 토큰을 모두 삭제하여 토큰 탈취 및 재사용 공격을 방지합니다.

### 3.3. 안전한 회원가입

*   사용자 이름, 비밀번호, 나이, 역할을 입력받아 신규 계정 생성.
*   `BCryptPasswordEncoder`를 이용한 비밀번호 안전 암호화.
*   회원가입 페이지에서 **드롭다운을 통한 역할(Role) 선택 기능** 제공.
*   `@Transactional` 및 `Optional`을 활용하여, 존재하지 않는 역할(Role) 선택 시 **명시적인 예외 처리** 및 데이터베이스 롤백을 통한 안정성 확보.

### 3.4. 보안

*   Spring Security 프레임워크를 활용한 강력한 인증 및 인가 처리.
*   비밀번호 평문 저장 방지 및 안전한 해싱 알고리즘(`BCrypt`) 적용.
*   JWT 서명을 통한 토큰 위변조 방지 및 무결성 보장.

## 4. 프로젝트 실행 방법

### 4.1. 사전 준비 (Prerequisites)

*   JDK 21 이상
*   MySQL Database
*   **Redis Server**
*   Gradle (빌드 도구)

### 4.2. 데이터베이스 및 Redis 설정

1.  MySQL과 Redis 서버를 실행합니다.
2.  `src/main/resources/sql/security.sql` 스크립트를 실행하여 필요한 테이블과 기본 데이터를 생성합니다.
3.  `src/main/resources/application.properties` 파일에 MySQL 및 Redis 연결 정보를 설정합니다.
    ```properties
    # MySQL Database
    spring.datasource.url=jdbc:mysql://localhost:3306/security
    spring.datasource.username=security
    spring.datasource.password=1234

    # Redis
    spring.data.redis.host=localhost
    spring.data.redis.port=6379

    # JPA/Hibernate
    spring.jpa.hibernate.ddl-auto=none
    spring.jpa.show-sql=true

    # JWT Secret Key (반드시 강력하고 긴 문자열로 변경하세요!)
    spring.jwt.secret=a-very-long-and-secure-secret-key-that-is-at-least-256-bits-long
    ```

### 4.3. 애플리케이션 빌드 및 실행

1.  프로젝트 루트 디렉토리에서 다음 명령어를 실행하여 프로젝트를 빌드합니다.
    ```bash
    ./gradlew build
    ```
2.  빌드가 성공하면, 다음 명령어로 애플리케이션을 실행합니다.
    ```bash
    java -jar build/libs/loginsecurity-0.0.1-SNAPSHOT.jar
    ```

### 4.4. 웹 브라우저 접속

*   **회원가입 페이지:** `http://localhost:7777/registerform.html`
*   **로그인 페이지:** `http://localhost:7777/loginform.html`

## 5. 프로젝트 구조 (주요 패키지)

*   `com.sinse.loginsecurity.config`: Spring Security, Redis, JWT 등 설정 클래스
*   `com.sinse.loginsecurity.controller`: HTTP 요청 처리 컨트롤러
*   `com.sinse.loginsecurity.domain`: 데이터베이스 엔티티 (User, Role)
*   `com.sinse.loginsecurity.dto`: 데이터 전송 객체 (UserDTO)
*   `com.sinse.loginsecurity.repository`: JPA Repository 인터페이스
*   `com.sinse.loginsecurity.service`: 비즈니스 로직 서비스
*   `com.sinse.loginsecurity.util`: JWT 관련 유틸리티

## 6. 향후 개선 및 학습 목표

*   **`User` 엔티티 PK `Long` 타입으로 리팩토링:** JPA 컨벤션에 따라 `id` 필드를 `Long` 타입으로 변경.
*   **Vue.js SPA 연동:** 현재 순수 JavaScript 기반 프론트엔드를 Vue.js 기반 SPA로 전환.
*   **서비스 로직 리팩토링:** 컨트롤러에 집중된 비즈니스 로직을 서비스 계층으로 분리하여 역할과 책임을 명확히 구분.