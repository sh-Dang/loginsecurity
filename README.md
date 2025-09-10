# 로그인 보안 프로젝트 (JWT 기반)

## 1. 프로젝트 개요

이 프로젝트는 기존의 세션 기반 로그인 시스템을 현대적인 JWT(JSON Web Token) 기반의 인증 시스템으로 전환하고, 안전한 회원가입 기능을 추가하여 사용자 인증의 전반적인 흐름을 구축합니다.

## 2. 주요 기술 스택

*   **백엔드:**
    *   Java 21
    *   Spring Boot
    *   Spring Security (6.x)
    *   JPA / Hibernate
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
*   발급된 JWT를 `Authorization` 헤더에 담아 보호된 리소스(예: `main.html` 내용)에 접근.
*   `JwtFilter`를 통해 모든 요청에 대한 JWT 유효성 검사 및 인증 처리.
*   Stateless(무상태) 인증 방식으로 서버 부하 감소 및 확장성 확보.

### 3.2. 안전한 회원가입

*   사용자 이름과 비밀번호를 통한 신규 계정 생성.
*   `BCryptPasswordEncoder`를 이용한 비밀번호 안전 암호화 및 솔트(Salt) 자동 관리.
*   회원가입 시 모든 사용자에게 기본 `ROLE_USER` 자동 부여.

### 3.3. 보안

*   Spring Security 프레임워크를 활용한 강력한 인증 및 인가 처리.
*   비밀번호 평문 저장 방지 및 안전한 해싱 알고리즘(`BCrypt`) 적용.
*   JWT 서명을 통한 토큰 위변조 방지 및 무결성 보장.

## 4. 프로젝트 실행 방법

### 4.1. 사전 준비 (Prerequisites)

*   JDK 21 이상
*   MySQL Database
*   Gradle (빌드 도구)

### 4.2. 데이터베이스 설정

1.  MySQL 서버를 실행합니다.
2.  `src/main/resources/sql/security.sql` 스크립트를 실행하여 필요한 `user` 및 `role` 테이블을 생성하고, 기본 `ROLE_USER` 데이터를 삽입합니다.
3.  `src/main/resources/application.properties` 파일에 MySQL 연결 정보를 설정합니다.
    ```properties
    # MySQL Database Configuration
    spring.datasource.url=jdbc:mysql://localhost:3306/your_database_name?useSSL=false&serverTimezone=UTC
    spring.datasource.username=your_username
    spring.datasource.password=your_password
    spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

    # JPA/Hibernate Configuration
    spring.jpa.hibernate.ddl-auto=update # or none if you manage schema manually
    spring.jpa.show-sql=true
    spring.jpa.properties.hibernate.format_sql=true

    # JWT Secret Key (반드시 강력하고 긴 문자열로 변경하세요!)
    spring.jwt.secret=your_very_long_and_secure_jwt_secret_key_here_at_least_256_bits
    ```
    *   `your_database_name`, `your_username`, `your_password`를 실제 환경에 맞게 변경하세요.
    *   `spring.jwt.secret` 값은 반드시 강력하고 긴 문자열로 변경해야 합니다.

### 4.3. 애플리케이션 빌드 및 실행

1.  프로젝트 루트 디렉토리에서 다음 명령어를 실행하여 프로젝트를 빌드합니다.
    ```bash
    ./gradlew build
    ```
2.  빌드가 성공하면, 다음 명령어로 애플리케이션을 실행합니다.
    ```bash
    java -jar build/libs/loginsecurity-0.0.1-SNAPSHOT.jar
    ```
    *   또는 IntelliJ IDEA와 같은 IDE에서 직접 실행할 수도 있습니다.

### 4.4. 웹 브라우저 접속

애플리케이션이 성공적으로 실행되면, 웹 브라우저를 통해 다음 URL에 접속합니다.

*   **회원가입 페이지:** `http://localhost:7777/registerform.html`
*   **로그인 페이지:** `http://localhost:7777/loginform.html`

## 5. 프로젝트 구조 (주요 패키지)

*   `com.sinse.loginsecurity.config`: Spring Security 및 애플리케이션 전반의 설정 (JWT 필터, SecurityConfig, AppConfig 등)
*   `com.sinse.loginsecurity.controller`: HTTP 요청을 처리하고 응답을 반환하는 REST 컨트롤러 (UserController)
*   `com.sinse.loginsecurity.domain`: 데이터베이스 엔티티 (User, Role)
*   `com.sinse.loginsecurity.dto`: 데이터 전송 객체 (UserDTO)
*   `com.sinse.loginsecurity.repository`: JPA를 이용한 데이터베이스 접근 인터페이스 (JpaUserRepository, JpaRoleRepository)
*   `com.sinse.loginsecurity.service`: 비즈니스 로직을 담당하는 서비스 (JpaUserDetailsService)
*   `com.sinse.loginsecurity.util`: JWT 관련 유틸리티 클래스 (JwtUtil)

## 6. 향후 개선 사항

*   **리프레시 토큰(Refresh Token) 로직 구현:** 액세스 토큰 만료 시 사용자 재로그인 없이 새로운 액세스 토큰 발급.
*   **사용자 Role 선택 기능 추가:** 회원가입 시 사용자가 직접 Role을 선택할 수 있도록 확장.
*   **`User` 엔티티 PK `Long` 타입으로 리팩토링:** JPA 컨벤션에 따라 `id` 필드를 `Long` 타입으로 변경.
*   **리포지토리 메소드 `Optional` 사용:** `findByUsername` 등 조회 메소드 반환 타입을 `Optional`로 변경하여 Null 안정성 강화.
*   **Vue.js SPA 연동:** 현재 순수 JavaScript 기반 프론트엔드를 Vue.js 기반 SPA로 전환.
