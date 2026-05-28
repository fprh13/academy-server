# README

## 목차

### 필수 포함 항목
- [프로젝트 개요](#프로젝트-개요)
- [기술 스택](#기술-스택)
- [실행 방법](#실행-방법)
- [요구사항 해석 및 가정](#요구사항-해석-및-가정)
- [설계 결정과 이유](#설계-결정과-이유)
- [미구현 / 제약사항](#미구현--제약사항)
- [AI 활용 범위](#ai-활용-범위)

### BE 과제 선택 시 추가 항목

- [API 목록 및 예시](#api-목록-및-예시)
- [데이터 모델 설명](#데이터-모델-설명)
- [테스트 실행 방법](#테스트-실행-방법)

### BE 과제 선택 시 추가 제출물

- [API 명세 또는 샘플 요청/응답](#api-명세-또는-샘플-요청응답)
- [DB 스키마 또는 ERD 설명](#db-스키마-또는-erd-설명)

---

<br>

## 프로젝트 개요
### 과제 BE-A. 수강 신청 시스템
수강 신청 시스템(아카데미)는 크리에이터가 강의를 개설하고, 클래스메이트(수강생)이 강의를 신청, 결제, 취소 할 수 있는 수강 신청 시스템입니다.

현재 구현 범위는 크게 `identity`, `course`, `enrollment` 3개의 컨텍스트로 나뉩니다.

- `identity`: 회원 가입, 로그인, 인증 사용자 조회
- `course`: 강의 등록, 강의 목록/상세 조회, 강의별 수강생 목록 조회
- `enrollment`: 수강 신청, 결제 확정, 수강 취소, 웨이팅(대기열) 취소, 내 신청 목록 조회

비즈니스적으로는 강의 상태(`DRAFT`, `OPEN`, `CLOSED`), 수강 신청 상태(`PENDING`, `WAITING`, `CONFIRMED`, `CANCELLED`), 정원 초과 시 웨이팅(대기열) 전환, 결제 후 7일 이내 취소 정책을 다룹니다.

---

<br>

## 기술 스택
- Java 17
- Spring Boot 3.5
- Spring Data JPA
- Spring Security
- JWT
- H2 Database
- Spring RestDocs
- OpenAPI 3 / Swagger UI
- Gradle
- Github Actions

---

<br>

## 실행 방법
### Windows / Mac(Intel)

```bash
docker pull fprh13/academy-server:latest && docker run -d --name academy-server -p 8080:8080 fprh13/academy-server:latest
```

### Mac (Apple Silicon, M1/M2/M3)

```bash
docker pull --platform linux/amd64 fprh13/academy-server:latest && docker run -d --platform linux/amd64 --name academy-server -p 8080:8080 fprh13/academy-server:latest
```

### 접속 주소

- Swagger UI (API Docs): `http://localhost:8080/swagger-ui/swagger-ui.html`
- H2 Console(DB): `http://localhost:8080/h2-console`

### H2 Console 접속 정보

- Saved Settings: `Generic H2 (Embedded)`
- Driver Class: `org.h2.Driver`
- JDBC URL: `jdbc:h2:mem:testdb`
- User Name: `sa`
- Password: 빈 값

---

<br>

## 요구사항 해석 및 가정
강의 모집 상태와 수강 신청 상태를 어떻게 안전하게 관리할 것인지가 핵심이라고 판단했습니다.

특히 아래 요소들을 중요한 요구사항으로 해석했습니다.

- 강의 상태(`DRAFT`, `OPEN`, `CLOSED`)에 따라 신청 가능 여부가 달라진다.
- 수강 신청 상태(`PENDING`, `CONFIRMED`, `CANCELLED`)에 따라 취소 및 정원 계산 정책이 달라진다.
- 정원 제한이 존재하기 때문에 동시 요청 상황에서 데이터 정합성이 중요하다.

또한 선택 구현 항목이었던 웨이팅(대기열) 기능과 취소 가능 기간 제한을 함께 구현했습니다.

웨이팅 기능은 대기 상태 저장을 넘어, 수강 취소 시 가장 오래 기다린 신청자를 자동으로 승격시키는 흐름까지 포함하여 구현했습니다.

<br>

### 구현 시 가정한 정책

- 결제 기능은 실제 외부 PG 연동 없이, 내부 상태 변경 방식으로 처리한다고 가정했습니다.
- 수강 확정(`CONFIRMED`) 이후 7일이 지난 신청은 환불(수강 확정 취소)할 수 없다고 가정했습니다.
- 정원이 가득 찬 경우 신청을 완전히 거절하지 않고, `WAITING` 상태로 웨이팅(대기열)에 등록된다고 가정했습니다.
- 웨이팅 승격은 가장 오래 기다린 신청자를 우선으로 처리한다고 가정했습니다.
- 강의 정원은 `CONFIRMED`, `PENDING` 상태를 기준으로 관리하며, `WAITING`, `CANCELLED` 상태는 현재 신청 인원에 포함하지 않는다고 가정했습니다.

<br>

### 웨이팅(대기열) 기능 범위

웨이팅 기능은 대규모 트래픽 환경의 실시간 대기열 시스템(예: 대형 콘서트 티켓팅) 수준까지 고려하지 않았습니다.

따라서 현재 구현에서는 별도의 메시지 큐나 분산 시스템 없이, DB 기반 상태 관리와 순차 승격 방식으로 처리했습니다.

---

<br>

## 설계 결정과 이유

### 도메인 기준 패키지 분리

패키지는 `identity`, `course`, `enrollment`, `common`과 같은 도메인 기준으로 분리했습니다.

수강 신청 시스템은 회원, 강의, 수강 신청 기능이 각각 다른 책임과 변경 이유를 가지기 때문에, 도메인 단위로 패키지를 분리하면 관련 코드의 위치를 쉽게 파악할 수 있고 변경 범위를 줄일 수 있다고 판단했습니다.

<br>

### 계층 구조 분리

```text
presentation -> application -> domain -> infrastructure
```

각 계층의 책임은 다음과 같이 분리했습니다.

| 계층 | 역할 |
|---|---|
| `presentation` | API 요청/응답 처리, 인증 사용자 전달 |
| `application` | 유스케이스 흐름 제어, 트랜잭션 관리 |
| `domain` | 핵심 비즈니스 규칙, 상태 전이, 검증 로직 |
| `infrastructure` | JPA Repository 구현, 외부 기술 연동 |

수강 신청 시스템은 강의 모집 상태, 수강 신청 상태, 정원 초과 여부, 결제 확정, 취소 정책, 웨이팅 승격과 같은 다양한 정책과 상태 전이가 존재합니다.

따라서 유스케이스 흐름과 도메인 규칙을 분리하기 위해 계층 구조를 나누었습니다.

<br>

### 수강 신청과 웨이팅 승격 처리

수강 신청과 취소는 `application` 계층의 트랜잭션 내부에서 처리합니다.

수강 신청 시에는 강의 정원을 확인한 뒤, 정원이 남아 있으면 `PENDING` 상태로 등록하고 현재 신청 인원을 증가시킵니다.

정원이 가득 찬 경우에는 `WAITING` 상태로 등록합니다.

수강 취소 시에는 현재 신청 인원을 감소시키고, 가장 오래된 `WAITING` 신청을 `PENDING` 상태로 승격합니다.

```text
수강 신청
-> 정원 확인
-> PENDING 또는 WAITING 결정
-> current 증가 여부 결정

수강 취소
-> current 감소
-> 가장 오래된 WAITING 승격
-> current 증가
```

좌석 수 변경과 수강 신청 상태 변경이 서로 다른 시점에 처리되면 정원 수와 신청 상태가 어긋날 수 있습니다.

이를 줄이기 위해 관련 상태 변경을 하나의 트랜잭션 안에서 함께 처리하도록 설계했습니다.

<br>

### 비관적 락 선택

수강 신청과 취소는 같은 강의의 정원 수(`current`)와 웨이팅 승격 순서에 영향을 줍니다.

해당 부분은 상태를 읽고 판단한 뒤 다시 수정하는 과정입니다.

```text
정원 확인 -> 신청 상태 결정 -> 현재 인원 변경
취소 확인 -> 현재 인원 감소 -> 웨이팅 대상 조회 -> 웨이팅 승격
```

같은 강의에 대해 동시에 수강 신청 또는 취소 요청이 들어오면, 정원 수가 잘못 계산되거나 웨이팅 승격 순서가 꼬일 수 있다고 판단했습니다.

이 경우에는 충돌 발생 후 재시도하는 방식보다, 같은 강의에 대한 정원 변경 작업을 순차적으로 처리하여 데이터 정확성을 보장하는 것이 더 중요하다고 보았습니다.

그래서 강의 조회 시 비관적 락을 사용해 동시성 문제를 제어했습니다.

<br>

### API 문서화 방식

API 문서는 Spring RestDocs와 OpenAPI 3를 함께 사용했습니다.

Spring RestDocs를 통해 테스트 기반으로 API 명세를 자동화화여 관리하고, OpenAPI 3와 Swagger UI를 통해 문서를 웹에서 쉽게 확인할 수 있도록 구성했습니다.

또한 문서 배포 과정을 추가하여 로컬 서버를 직접 실행하지 않아도 최신 API 문서를 확인할 수 있도록 했습니다.

---

<br>

## 미구현 / 제약사항

### 결제 대기(PENDING) 만료 처리 미구현

현재는 수강 신청 시 `PENDING` 상태로 등록되지만, 일정 시간 내 결제가 이루어지지 않았을 때 자동으로 취소되는 기능은 구현하지 않았습니다.

<br>

### 강의 모집 상태 자동 전환 미구현

`Course` 도메인 내부에는 `open`, `close` 상태 전이 메서드가 존재하지만, 이를 직접 호출하는 공개 API는 구현하지 않았습니다.

또한 모집 종료일이 되었을 때 강의를 자동으로 `CLOSED` 상태로 변경하는 기능도 포함하지 않았습니다.

현재는 더미 데이터 기반의 과제 범위에 집중하기 위해 전환 기능은 제외했습니다.

<br>

### 웨이팅 사용자 알림 기능 미구현

현재는 웨이팅(`WAITING`) 상태의 사용자가 수강 신청 가능한 상태로 승격되더라도, 별도의 알림을 보내는 기능은 구현하지 않았습니다.

실제 서비스 환경이라면 이메일, 푸시 알림, SSE/WebSocket 등을 통해 결제를 위해 웨이팅 승격 사실을 사용자에게 안내하는 기능이 필요할 수 있습니다.

<br>

### 동시성 처리 성능

수강 신청과 취소 과정에서 정원 수와 웨이팅 승격 순서를 안전하게 관리하기 위해 비관적 락을 사용했습니다.

다만 실제 운영 환경의 트래픽 규모를 가정한 부하 테스트나 성능 검증은 수행하지 않았습니다.

따라서 동시 요청이 매우 많은 환경에서는 락 경합에 따른 성능 영향 분석과 추가적인 전략 검토가 필요할 수 있습니다.

<br>

### 외부 결제 시스템 미연동

PG 또는 외부 결제 시스템 연동은 구현하지 않았습니다.

현재는 별도의 결제 과정 없이 `수강 확정 API` 호출을 통해 `PENDING -> CONFIRMED` 상태로 변경하는 방식으로 동작합니다.

---

<br>

## AI 활용 범위

### ChatGPT

- 변수명, 메서드명, 정책명, 클래스명 등 네이밍 후보를 검토하는 데 보조적으로 활용했습니다.
- 도메인 정책이나 비즈니스 흐름을 정리할 때 브레인스토밍 용도로 활용했습니다.
- 시나리오를 정리하며, 상태 변화와 예외 케이스를 검토하는 데 활용했습니다.
- README.md 마크다운 정리에 사용했습니다.

<br>

### Codex
`.codex` 에 Skills를 정의하여 반복 작업을 자동화하고 개발 생산성을 높이는 데 활용했습니다.

#### `academy-api-docs skill`
- Spring RestDocs 테스트 및 API 문서 작성 작업을 보조하기 위한 용도로 활용했습니다.
- 반복되는 API 문서 테스트 코드 작성을 빠르게 생성하고 수정하는 데 사용했습니다.

#### `academy-code-review skill`
- CI 검증 게이트 이전 단계에서 코드 안정성과 누락 사항을 점검하기 위한 용도로 활용했습니다.

#### `academy-pr skill`
- 개인 개발 환경에서도 작업 이력을 빠르게 정리하기 위해 활용했습니다.
- Git 커밋 이력을 기반으로 PR 초안 및 작업 요약 내용을 생성하는 데 사용했습니다.

<br>

### 테스트 코드 작성

- 테스트 코드를 직접 작성하거나, 초안을 작성한 뒤 Codex를 통해 작성, 수정,보완하는 방식으로 병행했습니다.
- 반복적인 테스트 코드 작성 시간을 줄이고, 다양한 케이스를 빠르고 다양하게 검증하고자 했습니다.

---

<br>

# BE 과제 선택 시 추가 항목:

## API 목록 및 예시

시나리오별 요청/응답 예시는 아래 문서에서 확인할 수 있습니다.
- API Docs: https://academy-api-docs.vercel.app

<br>

### 인증 / 회원

| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/auth/login` | 로그인 |
| POST | `/users` | 회원가입 |
| GET | `/users/login-id/exists` | 로그인 아이디 중복 체크 |
| GET | `/users/email/exists` | 이메일 중복 체크 |
| GET | `/users/profile` | 내 프로필 조회 |
| GET | `/users/{userId}` | 공개 프로필 조회 |

<br>

### 강의

| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/courses` | 강의 등록 (강사) |
| GET | `/courses` | 강의 목록 조회 |
| GET | `/courses/{courseId}` | 강의 상세 조회 |
| GET | `/courses/{courseId}/classmates` | 강의 수강생 목록 조회 (강사 전용) |

<br>

### 수강 신청

| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/enrollments` | 수강 신청 |
| POST | `/enrollments/{enrollmentId}/confirm` | 수강 확정 |
| DELETE | `/enrollments/{enrollmentId}/cancel` | 수강 신청 취소 |
| DELETE | `/enrollments/{enrollmentId}/wait-cancel` | 웨이팅(대기열) 취소 |
| POST | `/enrollments/{enrollmentId}/refund` | 수강 확정 취소 |
| GET | `/enrollments` | 내 수강 신청 목록 조회 |

---

<br>

## 데이터 모델 설명

핵심 데이터 모델은 `User`, `Course`, `Enrollment` 세 개의 주요 도메인으로 구성됩니다.

<br>

### User

회원 계정 정보를 관리합니다.

| 필드 | 설명 |
|---|---|
| `loginId` | 로그인 아이디 |
| `password` | 비밀번호 |
| `email` | 이메일 |
| `name` | 사용자 이름 |
| `role` | 사용자 역할 |

사용자 역할(`role`)은 아래와 같이 구분됩니다.

- `ADMIN` : 관리자
- `CREATOR` : 강사
- `USER` : 일반 수강생

<br>

### Course

강의 정보를 관리합니다.

| 필드 | 설명 |
|---|---|
| `title` | 강의 제목 |
| `description` | 강의 설명 |
| `price` | 수강 가격 |
| `capacity` | 최대 정원 및 현재 신청 인원 |
| `startDate` | 수강 시작일 |
| `endDate` | 수강 종료일 |
| `state` | 강의 모집 상태 |
| `creator` | 강사 정보 |

강의 상태(`state`)는 아래와 같이 관리됩니다.

- `DRAFT` : 초안 상태
- `OPEN` : 모집 중
- `CLOSED` : 모집 마감

하나의 강사(`User`)는 여러 개의 강의(`Course`)를 개설할 수 있습니다.

<br>

### Enrollment

수강 신청 및 결제 상태를 관리합니다.

| 필드 | 설명 |
|---|---|
| `state` | 수강 신청 상태 |
| `confirmedAt` | 수강 확정 시각 |
| `cancelledAt` | 취소 시각 |
| `course` | 신청한 강의 |
| `user` | 신청 사용자 |

수강 신청 상태(`state`)는 아래와 같이 관리됩니다.

- `PENDING` : 결제 대기
- `WAITING` : 웨이팅(대기열)
- `CONFIRMED` : 수강 확정
- `CANCELLED` : 취소됨

수강생(`User`) 1명은 여러 개의 수강 신청 이력(`Enrollment`)을 가질 수 있습니다.

<br>

### 전체 관계 구조

- `User(강사) 1:N Course`
- `User(수강생) 1:N Enrollment`
- `Course 1:N Enrollment`

---

<br>

## 테스트 실행 방법

Docker로 스프링 서버를 실행한 뒤, 로컬 Swagger UI에서 API를 테스트할 수 있습니다.

### 1. 서버 실행

```bash
docker pull fprh13/academy-server:latest && docker run -d --name academy-server -p 8080:8080 fprh13/academy-server:latest
```

### 2. Swagger UI 접속

```text
http://localhost:8080/swagger-ui/swagger-ui.html
```

### 3. 테스트 계정 정보

#### 수강생 계정

```text
loginId: classmate
password: classmate1234@
```

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjbGFzc21hdGUiLCJyb2xlIjoiUk9MRV9VU0VSIiwiaWF0IjoxNzc5OTAzMjgzLCJleHAiOjEwNDE5ODE2ODgzfQ.jMGNpenWDlr-RlUlbgMNL05TuAhPhaGpm3_cptgZ9Ng
```

#### 강사 계정

```text
loginId: creator
password: creator1234@
```

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjcmVhdG9yIiwicm9sZSI6IlJPTEVfQ1JFQVRPUiIsImlhdCI6MTc3OTkwNzIzOCwiZXhwIjoxMDQxOTgyMDgzOH0.CKSm1f3N8NfeJCrjgb2kLwb1AXltZACikLiEOJdiY80
```

<br>

### 4. 더미 데이터 정보

강사 계정(`creator`)은 총 10개의 강의를 보유하고 있습니다.

수강생 계정(`classmate`)은 아래와 같은 수강 신청 이력을 가지고 있습니다.

| 강의 ID | 상태 |
|---|---|
| 1번 강의 | `PENDING` |
| 2번 강의 | `CONFIRMED` |
| 3번 강의 | `CANCELLED` |
| 4번 강의 | `CONFIRMED` |

<br>

### 5. 테스트 가능한 시나리오

위 더미 데이터를 기반으로 아래 시나리오를 테스트할 수 있습니다.

- 강의 목록 조회
- 강의 상세 조회
- 수강 신청
- 수강 신청 취소
- 수강 확정
- 수강 확정 취소
- 웨이팅 취소
- 내 수강 신청 목록 조회
- 강의별 수강생 목록 조회

자세한 요청/응답 예시는 Swagger 문서의 `[필독] API 명세서 가이드`를 참고하면 됩니다.

---

<br>

# BE 과제 선택 시 추가 제출물

## API 명세 또는 샘플 요청/응답

상세 API 명세와 시나리오별 샘플 요청/응답은 아래 문서에서 확인할 수 있습니다.

- API Docs: https://academy-api-docs.vercel.app

해당 문서에는 인증/회원, 강의, 수강 신청 기능에 대한 API 명세와 예시 요청/응답이 포함되어 있습니다.

---

<br>

## DB 스키마 또는 ERD 설명

![ERD](docs/ERD.png)

### 주요 테이블

| 테이블 | 설명 |
|---|---|
| `users` | 회원 기본 정보와 사용자 역할을 저장합니다. |
| `courses` | 강의 정보, 정원, 수강 기간, 모집 상태, 강사 정보를 저장합니다. |
| `enrollments` | 사용자와 강의를 연결하는 수강 신청 이력 정보를 저장합니다. |

<br>

### 테이블 관계

| 관계 | 설명 |
|---|---|
| `users` → `courses` | 한 명의 강사는 여러 개의 강의를 개설할 수 있습니다. |
| `users` → `enrollments` | 한 명의 수강생은 여러 개의 수강 신청 이력을 가질 수 있습니다. |
| `courses` → `enrollments` | 하나의 강의는 여러 개의 수강 신청 이력을 가질 수 있습니다. |

<br>

### 테이블별 역할

#### `users`

회원 계정 정보를 저장하는 테이블입니다.

- 로그인 아이디
- 비밀번호
- 이메일
- 이름
- 사용자 역할

사용자 역할은 `ADMIN`, `CREATOR`, `USER`로 구분됩니다.

#### `courses`

강의 정보를 저장하는 테이블입니다.

- 강의 제목
- 강의 설명
- 가격
- 최대 정원
- 현재 신청 인원
- 수강 시작일
- 수강 종료일
- 모집 상태
- 강사 ID

강의 상태는 `DRAFT`, `OPEN`, `CLOSED`로 관리됩니다.

#### `enrollments`

수강 신청 이력을 저장하는 테이블입니다.

- 수강 신청 상태
- 수강 확정 시각
- 취소 시각
- 신청 사용자 ID
- 강의 ID

수강 신청 상태는 `PENDING`, `WAITING`, `CONFIRMED`, `CANCELLED`로 관리됩니다.

수강 신청, 결제 확정, 수강 취소, 웨이팅 취소와 같은 주요 비즈니스 흐름은 해당 테이블의 상태 변경을 중심으로 처리됩니다.
