### **JWT 로그인 프로젝트 이해도 심화 요약 (로그 및 주석 기반)**

이번 세션에서 추가된 `log.debug` 문과 주석들은 백엔드와 프론트엔드의 상호작용, 그리고 Spring Security의 내부 동작을 시각적으로 확인하고 이해하는 데 결정적인 역할을 했습니다.

#### **1. `JwtFilter`의 동작 원리 및 효율성 확인**

*   **확인된 로그:**
    *   `8. /main 요청이 들어온 이후 가동된 필터 입니다 `
    *   `9. authorization은 무엇인가요????null`
    *   `10. 'Bearer '로 시작하는 토큰이 존재하는 것을 확인 했습니다. 다음 단계로 넘어갑니다.`
    *   `11. 'Bearer '를 제거하고 순수하게 추출한 토큰 값은=====`
*   **명확해진 점:**
    *   `JwtFilter`의 `doFilterInternal()` 메소드는 **서버로 들어오는 모든 HTTP 요청(HTML, CSS, JS, API 등)에 대해 예외 없이 실행**됨을 로그를 통해 직접 확인했습니다.
    *   `authorization == null` 또는 `!authorization.startsWith("Bearer ")` 조건문과 `return;` 문 덕분에, 토큰이 없는 요청(예: CSS 파일 요청)은 **복잡한 검증 로직을 거치지 않고 매우 빠르게 다음 필터로 넘어감**을 로그의 흐름으로 파악했습니다. 이는 필터의 효율성을 보여줍니다.
    *   오직 `Bearer` 토큰이 존재하는 요청만이 토큰 추출 및 유효성 검증 단계로 넘어감을 확인했습니다.

#### **2. 로그인(`UserController`) 과정의 세부 흐름 파악**

*   **확인된 로그:**
    *   `1. 들어와서 userDTO에 저장된 정보의 정체는 ...`
    *   `2. 들어와서 userDTO에 저장된 유저ID는 ...`
    *   `3. 들어와서 userDTO에 저장된 유저의 비밀번호는 ...`
    *   `4. authenticationManager로 검증해서 가져온 객체 ...`
    *   `5. getPrincipal()로 가져와 담아낸 userDetails 정보는 ...`
    *   `6. 로그인 정보가 담긴 UserDetails객체에서 추출해낸 role은 ...`
    *   `7. 로그인 정보를 사용(JwtUtil객체를 통해)해 만든 token 문자열은 == ...`
*   **명확해진 점:**
    *   `@RequestBody`를 통해 프론트엔드에서 전송된 `UserDTO`가 백엔드 `UserController`에 정확히 매핑되어 들어옴을 확인했습니다.
    *   `authenticationManager.authenticate()` 메소드가 **사용자 인증(비밀번호 비교 포함)의 핵심 역할**을 수행하며, 이 메소드가 성공적으로 실행되면 이미 인증이 완료된 상태임을 명확히 이해했습니다.
    *   `CustomUserDetails` 객체에서 사용자 이름과 권한을 추출하여 JWT 생성에 활용하는 흐름을 확인했습니다.

#### **3. `JpaUserDetailsService`와 `PasswordEncoder`의 연동 확인**

*   **확인된 로그:**
    *   `14. 유저이름으로 꺼내온 유저 객체에 담긴 password는 === $2a$10$Is7b0OxMNnLkFBGkzm8p5elKc6OXqdvaijIhE8ArC8p1dsJQQc.0O`
*   **명확해진 점:**
    *   데이터베이스에 비밀번호가 이미 BCrypt로 암호화되어 저장되어 있음을 로그를 통해 확인했습니다.
    *   `JpaUserDetailsService`에 `PasswordEncoder`를 주입했지만 직접 비교 로직을 넣지 않아도, Spring Security의 `AuthenticationManager`가 등록된 `BCryptPasswordEncoder` 빈을 자동으로 사용하여 **암호화된 비밀번호를 올바르게 검증**하고 있었음을 확인했습니다.
    *   `BCryptPasswordEncoder`가 솔트(Salt)를 별도로 관리할 필요 없이 **해시된 비밀번호 문자열 안에 솔트를 포함**시켜 저장하고 검증함을 이해했습니다.

#### **4. 프론트엔드-백엔드 간 JWT 기반 상호작용 심화**

*   **확인된 코드 변경:** `loginform.html`의 자바스크립트 코드 수정
*   **명확해진 점:**
    *   로그인 성공 후 `window.location.href`를 통한 단순 페이지 이동으로는 보호된 `main.html`에 접근할 수 없음을 경험했습니다.
    *   JWT 기반 시스템에서는 로그인 후 **새로운 `fetch` 요청에 JWT를 `Authorization` 헤더에 담아 보내야만** 보호된 리소스(예: `main.html`의 내용)에 접근할 수 있음을 코드를 통해 구현하고 확인했습니다. 이는 클라이언트와 서버 간의 두 번째 상호작용임을 명확히 했습니다.

