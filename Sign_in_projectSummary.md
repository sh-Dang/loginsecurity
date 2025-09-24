### **SIGN_IN_PROJECT_SUMMARY.md**

#### **1. 프로젝트 목표 및 초기 상태**

*   **목표:** 기존 세션 기반 로그인 프로젝트를 최신 JWT(JSON Web Token) 기반 로그인/인증 시스템으로 전환하고, 회원가입 기능을 추가하여 사용자 인증의 전반적인 흐름을 구축합니다.
*   **초기 상태:** 백엔드에는 JWT 관련 기본 파일(`JwtUtil.java`, `JwtFilter.java`)이 존재했으나, 실제 로그인/회원가입 비즈니스 로직과 프론트엔드(HTML/JavaScript) 연동은 미비한 상태였습니다.

#### **2. JWT 로그인 기능 구현 및 이해 심화**

*   **2.1. 백엔드 로그인 API (`UserController.login`) 구현 및 이해**
    *   **주요 변경점:**
        *   `@PostMapping("/login")` 메소드에 `authenticationManager.authenticate()`를 통한 인증 위임 로직 구현.
        *   인증 성공 시 `JwtUtil.createJwt()`를 호출하여 JWT를 생성하고 `Map<String, String>` 형태로 클라이언트에 반환.
    *   **이해 심화:**
        *   `@RequestBody` 어노테이션을 통해 프론트엔드에서 전송된 JSON 데이터(`UserDTO`)가 백엔드 Java 객체로 자동 매핑되는 원리(`Jackson` 라이브러리 활용)를 이해.
        *   `Map<String, String>`을 반환하는 것이 클라이언트에게 JWT를 명확한 JSON 형태로 전달하는 표준적인 API 응답 방식임을 파악.
        *   `authenticationManager.authenticate()`가 평문 비밀번호를 받아 `PasswordEncoder`를 통해 내부적으로 해싱 및 비교를 수행하여 인증을 처리하는 과정을 이해.
        *   `JwtUtil.createJwt()` 메소드의 각 빌더 메소드(`claim`, `setIssuedAt`, `setExpiration`, `signWith`, `compact`)가 JWT의 헤더, 페이로드, 서명을 어떻게 구성하는지 상세히 이해. 특히 `signWith()`를 통한 서명 과정에서 헤더와 페이로드가 `secretKey`와 함께 해싱되어 토큰의 무결성을 보장하는 원리를 깊이 있게 이해.

*   **2.2. 프론트엔드 로그인 연동 (`loginform.html`) 및 이해**
    *   **주요 변경점:**
        *   `fetch` API를 사용하여 `/login` API로 사용자 이름과 비밀번호를 JSON 데이터로 전송.
        *   로그인 성공 시 백엔드로부터 받은 JWT를 `localStorage`에 저장.
        *   **핵심 문제 해결 및 이해 심화:** 로그인 성공 후 `main.html`로 단순 리디렉션(`window.location.href`) 시 접근 거부 문제 발생. 이는 보호된 `main.html`에 접근하기 위해서는 JWT가 필요하며, 브라우저가 자동으로 토큰을 보내지 않음을 인지. 해결책으로, 로그인 후 받은 JWT를 `Authorization` 헤더에 담아 `fetch`로 `main.html` 내용을 요청하고, 받은 HTML로 현재 페이지를 덮어씌우는 방식으로 구현하여 JWT 기반 시스템에서 보호된 페이지 접근 방식에 대한 깊은 이해를 달성.

*   **2.3. `JwtFilter`의 역할 및 동작 원리 이해**
    *   **주요 변경점:** `JwtFilter` 내부에 `log.debug` 주석 추가를 통해 실행 흐름 추적.
    *   **이해 심화:**
        *   `JwtFilter`의 `doFilterInternal()` 메소드가 서버로 들어오는 **모든 HTTP 요청(HTML, CSS, JS, API 등)에 대해 예외 없이 가동**됨을 로그를 통해 직접 확인.
        *   `Authorization` 헤더가 없거나 유효하지 않은 요청은 `return;` 문을 통해 **빠르게 다음 필터로 전달**되어 효율성을 확보함을 이해.
        *   오직 `Bearer` 토큰이 있는 요청만 실제 JWT 검증 로직을 거쳐 `SecurityContextHolder`에 인증 정보를 설정함을 이해.
        *   `JwtFilter`가 `authorizeHttpRequests`보다 먼저 실행되는 Spring Security 필터 체인의 순서와 중요성을 파악.

#### **3. 회원가입 기능 구현 및 보안 이해 심화**

*   **3.1. 백엔드 회원가입 API (`UserController.register`) 구현 및 이해**
    *   **주요 변경점:**
        *   `@PostMapping("/register")` 메소드 추가.
        *   `UserController`에 `PasswordEncoder`와 `JpaUserRepository` 주입.
        *   사용자가 입력한 비밀번호를 `passwordEncoder.encode()` 메소드를 사용하여 암호화한 후 `User` 객체에 설정.
        *   `jpaUserRepository.save()`를 통해 암호화된 비밀번호와 Role이 설정된 사용자 정보를 DB에 저장.
        *   `ResponseEntity.ok()`를 통해 HTTP 상태 코드 200과 성공 메시지 반환.
    *   **이해 심화:**
        *   **비밀번호 해싱의 중요성:** 평문 비밀번호 저장의 위험성을 인지하고, `BCryptPasswordEncoder`를 통한 안전한 비밀번호 저장 방식의 필요성을 이해.
        *   **`BCryptPasswordEncoder`의 솔트(Salt) 관리:** 개발자가 솔트를 직접 관리할 필요 없이, `BCryptPasswordEncoder`가 해시된 비밀번호 문자열 안에 솔트를 포함시켜 자동으로 관리하고 검증하는 원리를 깊이 있게 이해.
        *   해싱된 비밀번호가 DB에 저장되는 방식 및 로그인 시 검증 과정에서 솔트가 어떻게 활용되는지 상세히 이해.
        *   `ResponseEntity`를 사용하여 HTTP 상태 코드를 명시적으로 제어하는 방법 및 그 중요성(성공/실패, 오류 유형 전달)을 이해.
        *   회원가입 시 사용자에게 기본 `Role`을 부여하는 로직의 필요성을 인지하고 구현.

*   **3.2. 프론트엔드 회원가입 연동 (`registerform.html`) 및 이해**
    *   **주요 변경점:**
        *   `fetch` API를 사용하여 `/register` API로 사용자 이름과 비밀번호를 JSON 데이터로 전송.
        *   `headers: { 'Content-Type': 'application/json' }` 및 `body: JSON.stringify(...)`를 사용하여 올바른 `fetch` 요청 구성 방식 숙지.
        *   `registerResponse.ok`를 통한 응답 상태 코드 확인, `try...catch` 블록을 통한 네트워크 오류 처리 및 사용자 피드백(`alert()`) 제공 로직 추가.
        *   회원가입 성공 시 `loginform.html`로 자동 리디렉션 구현.
    *   **이해 심화:**
        *   `fetch` API의 표준 요청 양식(`method`, `headers`, `body`)에 대한 명확한 이해.
        *   HTTP 상태 코드가 프론트엔드의 `response.ok` 속성에 미치는 영향을 이해하고, 이를 기반으로 사용자 경험을 개선하는 방법 습득.

#### **4. 결론**

이번 프로젝트를 통해 세션 기반 인증에서 벗어나 JWT 기반 인증 시스템의 핵심 원리(Statelessness, 토큰 생성/검증, 필터 체인)를 성공적으로 구축하고 깊이 있게 이해했습니다. 또한, 비밀번호 해싱 및 솔트 관리와 같은 중요한 보안 개념을 실제 코드에 적용하며, 프론트엔드와 백엔드 간의 API 통신 및 역할 분담에 대한 실질적인 경험을 축적했습니다. 이는 현대 웹 애플리케이션 개발에 필수적인 역량을 크게 강화하는 계기가 되었습니다.
