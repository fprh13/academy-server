# Architecture

## 목적

이 문서는 `academy-server`의 현재 구조와 앞으로 확장할 도메인 경계를 빠르게 이해하기 위한 아키텍처 문서입니다.

이 문서는 다음 질문에 답하기 위해 존재합니다.

- 지금 실제로 구현된 범위는 어디까지인가
- 각 컨텍스트는 어떤 책임을 가져야 하는가
- 새 코드를 어느 계층에 두어야 하는가
- `docs/requirements.md`의 요구사항을 어떤 구조로 풀어야 하는가

참고:

- `AGENTS.md`: 작업 규칙과 응답 규칙
- `docs/code-style.md`: 코드 작성 규칙
- `docs/requirements.md`: 구현 대상 요구사항 문서

이 문서는 코드 스타일이나 세부 구현 규칙보다, 구조와 책임 경계를 설명하는 데 집중합니다.

---

## 현재 상태 한눈에 보기

현재 저장소에서 실제 구현된 주요 컨텍스트는 아래 두 개입니다.

- `common`: 공통 응답, 예외 처리, 웹 설정, 보안 설정
- `identity`: 회원 가입, 로그인, 인증 사용자 조회

아래 컨텍스트는 `docs/requirements.md` 기준으로 앞으로 구현될 대상입니다.

- `course`: 강의 개설, 상태, 정원, 기간
- `enrollment`: 수강 신청, 결제 확정, 취소, 정원 정책

즉, 이 저장소는 현재 `identity`를 중심으로 기본 백엔드 골격을 먼저 갖춘 상태입니다.

`course`, `enrollment`는 문서상 목표 구조를 먼저 정의해 두고, 실제 코드는 이후 그 구조를 따라 확장하는 것이 기준입니다.

---

## 빠른 시작점

처음 진입할 때는 아래 순서로 읽는 것이 가장 효율적입니다.

1. `src/main/java/com/example/academy/common/infrastructure/config/SecurityConfig.java`
2. `src/main/java/com/example/academy/common/infrastructure/config/WebConfig.java`
3. `src/main/java/com/example/academy/identity/presentation/AuthController.java`
4. `src/main/java/com/example/academy/identity/presentation/UserController.java`
5. `src/main/java/com/example/academy/common/presentation/advice/ControllerExceptionAdvice.java`
6. `src/main/java/com/example/academy/identity/application`
7. `src/main/java/com/example/academy/identity/domain`

이 순서로 보면 인증 진입점, 인증 사용자 주입 방식, HTTP 요청 처리 방식, 예외 규약, 애플리케이션 계층 흐름을 짧게 파악할 수 있습니다.

---

## 시스템 개요

기술 스택과 런타임 전제는 아래와 같습니다.

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- JWT 기반 stateless 인증
- H2 데이터베이스
- RestDocs + OpenAPI 3 기반 API 문서 생성

현재 아키텍처의 핵심 방향은 다음과 같습니다.

1. 패키지는 기술이 아니라 도메인 기준으로 나눕니다.
2. 각 도메인은 `presentation -> application -> domain -> infrastructure` 계층을 따릅니다.
3. `domain`은 외부 기술을 직접 알지 않고, `infrastructure`가 그 구현을 담당합니다.
4. 공통 정책은 `common`에 모으되, 도메인 규칙은 각 컨텍스트 안에 둡니다.

---

## 컨텍스트 경계

### 1. `common`

`common`은 여러 도메인에서 공통으로 사용하는 기술적 기반을 담당합니다.

주요 책임:

- `ApiResponse`, `ApiErrorResponse` 같은 응답 규약
- `ControllerExceptionAdvice` 기반 예외 변환
- `SecurityConfig`, `WebConfig` 같은 애플리케이션 전역 설정
- `AggregateRoot`, `DomainEntity` 같은 공통 베이스 타입

`common`은 도메인 정책을 소유하지 않습니다.

비즈니스 의미가 있는 규칙은 `identity`, `course`, `enrollment` 안에 둡니다.

### 2. `identity`

`identity`는 사용자 식별과 인증을 담당합니다.

주요 책임:

- 회원 가입
- 로그인
- 로그인 아이디/이메일 중복 확인
- 공개 프로필 조회
- 인증 사용자 식별
- JWT 발급 및 JWT 기반 인증 복원

핵심 개념:

- `User`: 사용자 어그리게이트
- `Role`: 사용자 권한 값
- `UserRepository`: 도메인 저장소 인터페이스

현재 `identity`는 가장 성숙한 컨텍스트이며, 다른 컨텍스트가 사용자 정보와 권한을 참조하는 기반이 됩니다.

### 3. `course`

`course`는 강의 자체를 소유하는 컨텍스트입니다.

`docs/requirements.md` 기준 목표 책임:

- 강의 등록
- 강의 상태 전이 (`DRAFT -> OPEN -> CLOSED`)
- 가격, 정원, 수강 기간 관리
- 강의 목록/상세 조회
- 현재 신청 인원 집계에 필요한 조회 모델 제공

아키텍처 원칙:

- 강의 상태와 정원 같은 규칙은 `course` 도메인이 소유합니다.
- 수강 신청 자체의 생성/확정/취소는 `enrollment`가 담당하되, 신청 가능 여부 판단에 필요한 강의 상태와 정원 정보는 `course`와 협력합니다.

### 4. `enrollment`

`enrollment`는 수강 신청 라이프사이클을 소유하는 컨텍스트입니다.

`docs/requirements.md` 기준 목표 책임:

- 수강 신청 생성
- 신청 상태 전이 (`PENDING -> CONFIRMED -> CANCELLED`)
- 결제 확정 처리
- 취소 가능 기간 검증
- 내 수강 신청 목록 조회
- 정원 초과 방지 정책 적용

아키텍처 원칙:

- 신청 상태와 취소 규칙은 `enrollment` 도메인이 소유합니다.
- 결제 연동이 단순 상태 변경이라면 외부 PG 연동 계층 없이 application service에서 유스케이스를 조합해도 됩니다.
- 마지막 좌석 동시 경쟁 같은 문제는 `enrollment` 구현 시 명시적으로 다뤄야 합니다.

---

## 패키지와 계층 구조

최상위 패키지는 `com.example.academy.<context>` 형태를 사용합니다.

각 컨텍스트는 아래 계층 구조를 기본으로 따릅니다.

```text
presentation   -> Controller, Request/Response DTO, argument resolver
application    -> 유스케이스 orchestration, 트랜잭션, 권한 검증
domain         -> Entity, Value Object, Repository interface, 정책
infrastructure -> JPA 구현체, JWT, Config, 외부 시스템 연동
```

계층별 책임은 아래처럼 정리합니다.

### `presentation`

- HTTP 요청 해석
- 요청 DTO 검증
- application service 호출
- `ApiResponse` 형태로 응답 변환

### `application`

- 유스케이스 단위 트랜잭션 경계
- 여러 도메인 객체와 저장소 조합
- 권한 검증과 처리 순서 제어
- 도메인 이벤트 발행 이후 후속 흐름 연결

### `domain`

- 상태와 규칙의 중심
- 의미 있는 메서드로만 상태 변경
- 저장소 인터페이스 선언
- 어그리게이트 경계 유지

### `infrastructure`

- JPA 저장소 구현
- JWT 생성과 파싱
- Spring Security 연동
- 실제 외부 기술 세부사항 캡슐화

---

## 현재 요청 처리 구조

### 1. 회원 가입

현재 회원 가입 흐름은 아래와 같습니다.

```text
Client
  -> UserController.register()
  -> UserService.register()
  -> 중복 아이디/이메일 검증
  -> 비밀번호 인코딩
  -> RegisterUserRequest.toEntity()
  -> UserRepository.save()
  -> AFTER_COMMIT UserRegisteredEventHandler
```

의미:

- 요청 DTO는 presentation 계층에서 검증합니다.
- 유스케이스 흐름과 트랜잭션은 `UserService`가 소유합니다.
- `User` 생성 시 도메인 이벤트를 등록합니다.
- 후속 처리 예시는 `UserRegisteredEventHandler`에서 `AFTER_COMMIT` 이벤트로 연결됩니다.

### 2. 로그인

현재 로그인 흐름은 아래와 같습니다.

```text
Client
  -> AuthController.authenticate()
  -> AuthService.authenticate()
  -> UserRepository.findByLoginId()
  -> BCryptPasswordEncoder.matches()
  -> JwtTokenProvider.createAccessToken()
  -> Bearer 토큰 응답
```

의미:

- 인증 자체는 application service에서 처리합니다.
- JWT 생성은 infrastructure의 `JwtTokenProvider`가 담당합니다.
- 현재 액세스 토큰의 subject는 `loginId`, 권한 정보는 claim에 저장됩니다.

### 3. 인증 사용자 주입

인증이 필요한 컨트롤러 흐름은 아래와 같습니다.

```text
Client request
  -> JwtAuthorizationFilter
  -> JwtTokenProvider.parseAccessToken()
  -> SecurityContext 저장
  -> AuthUserResolver
  -> UserRepository.findByLoginId()
  -> Controller 메서드 파라미터 User 주입
```

의미:

- 필터 단계에서는 토큰 파싱과 `Authentication` 복원까지만 수행합니다.
- 실제 `User` 엔티티 조회는 `AuthUserResolver`에서 한 번 더 수행합니다.
- 따라서 "필터에서 DB 조회는 생략"이 현재 구조의 정확한 표현이며, 요청 전체 기준으로는 인증 사용자 조회가 발생할 수 있습니다.

---

## 인증 및 보안 구조

현재 인증 방식은 Spring Security + JWT 기반 stateless 인증입니다.

주요 구성 요소:

- `SecurityConfig`: 공개/보호 엔드포인트, 필터 체인, CORS 정책
- `JwtAuthorizationFilter`: Bearer 토큰 파싱과 `SecurityContext` 복원
- `AuthenticationEntryPointImpl`: 인증 실패 응답
- `AccessDeniedHandlerImpl`: 인가 실패 응답
- `AuthUserResolver`: 컨트롤러 파라미터에 인증 사용자 주입

현재 보안 정책의 특징:

- `/auth/login`, 회원 가입, 일부 프로필/중복 확인 API는 공개
- 프로필 조회 일부는 인증 필요
- 세션은 사용하지 않고 JWT만 사용
- 권한은 토큰 claim의 authority 값으로 복원

주의:

- `.anyRequest().permitAll()` 상태에서는 신규 API 추가 시 보안 정책을 반드시 함께 검토해야 합니다.
- 새 컨텍스트 API를 추가할 때는 컨트롤러 작성보다 먼저 `SecurityConfig`의 공개/보호 범위를 정하는 것이 안전합니다.

---

## 데이터 및 인프라 구조

현재 저장소 기준 인프라 전제는 아래와 같습니다.

- 영속화: Spring Data JPA
- 데이터베이스: H2
- 인증 토큰: JWT
- 비밀번호 인코딩: BCrypt

현재 문서에서는 H2를 사용하지만, 이것이 운영 전용 결정인지 개발 단계 임시 선택인지는 별도 설정 문서로 분리하는 것이 좋습니다.

아키텍처 문서에서는 다음 원칙만 기억하면 됩니다.

- 도메인 저장소 인터페이스는 `domain`에 둡니다.
- JPA 구현체는 `infrastructure/persistence`에 둡니다.
- 토큰, 보안, 설정은 `infrastructure`에 둡니다.

---

## 예외와 응답 규약

현재 HTTP 응답 규약은 공통 포맷으로 수렴합니다.

- 성공 응답: `ApiResponse`
- 실패 응답: `ApiErrorResponse`
- 비즈니스 예외: `CustomException`
- 검증 오류: `MethodArgumentNotValidException`

예외 변환은 `ControllerExceptionAdvice`에서 처리합니다.

새 API를 추가할 때도 controller에서 직접 예외 응답 포맷을 만들지 않고, 공통 예외 규약으로 맞추는 것이 기준입니다.

---

## 테스트와 문서화

현재 테스트는 아래 축으로 구성되어 있습니다.

- controller 테스트
- application service 테스트
- JWT 관련 단위 테스트
- integration 테스트
- RestDocs 지원 테스트

문서 생성 흐름은 테스트에 연결되어 있습니다.

- `test` 실행
- RestDocs 결과와 OpenAPI 3 스펙 생성
- 생성된 `openapi3.yaml`을 Swagger 정적 리소스로 복사
- JaCoCo 리포트 생성 및 커버리지 검증 수행

즉, API 문서는 수동 작성보다 테스트 기반 산출물이 기준입니다.

---

## `docs/requirements.md`를 반영한 목표 구조

`docs/requirements.md`는 아직 코드로 구현되지 않은 요구사항 문서입니다.

이 문서를 실제 아키텍처에 반영할 때는 아래처럼 해석합니다.

### `course`가 소유할 것

- 강의 기본 정보
- 강의 상태 전이
- 정원과 모집 가능 여부
- 수강 기간

### `enrollment`가 소유할 것

- 신청 생성과 상태 전이
- 결제 확정 처리
- 취소 가능 기간 검증
- 내 신청 목록 조회

### 컨텍스트 협력에서 먼저 결정할 것

- 정원을 어디서 최종 차감할지
- 마지막 좌석 동시성 충돌을 어떤 방식으로 막을지
- 취소 시 정원 복구를 어떤 트랜잭션 경계에서 처리할지

실무적으로 가장 중요한 쟁점은 정원 동시성입니다.

수강 신청 생성 시점에 정원 검증만 하고 저장을 분리하면 마지막 좌석 경쟁에서 정원 초과가 발생할 수 있습니다.

따라서 `enrollment` 구현 시에는 아래 중 하나를 명확히 선택해야 합니다.

- DB 락 기반 직렬화
- 원자적 업데이트 쿼리 기반 처리
- 별도 좌석 재고 모델 도입

이 선택은 구현 전에 문서나 설계에서 먼저 확정하는 것이 좋습니다.

---

## 새 코드 배치 기준

새 코드를 추가할 때는 아래 기준으로 판단합니다.

- 인증, 회원, JWT, 사용자 식별 문제인가: `identity`
- 여러 컨텍스트가 함께 쓰는 응답/예외/설정 문제인가: `common`
- 강의 정보와 강의 상태 문제인가: `course`
- 신청, 결제 확정, 취소, 정원 처리 문제인가: `enrollment`

판단이 애매하면 "이 규칙을 가장 강하게 소유하는 어그리게이트가 누구인가"를 먼저 정하고, 그 컨텍스트에 둡니다.

---

## 문서 유지 원칙

이 문서는 실제 코드와 어긋나기 시작하면 빠르게 가치가 떨어집니다.

따라서 아래 원칙으로 유지합니다.

1. 현재 구현된 사실과 앞으로의 목표 구조를 구분해서 적습니다.
2. 구현되지 않은 내용은 "현재 상태"처럼 서술하지 않습니다.
3. 새로운 컨텍스트가 추가되면 패키지 구조보다 먼저 책임 경계를 문서에 반영합니다.
4. 보안 정책이나 트랜잭션 경계가 바뀌면 이 문서도 함께 갱신합니다.
