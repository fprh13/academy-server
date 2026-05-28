# Code Style

## 목적

이 문서는 `academy-server`에서 새로 작성하거나 수정하는 코드의 기준을 정의합니다.

문법 취향보다 설계 일관성을 우선합니다.

참고:

- `AGENTS.md`: 작업 규칙과 응답 규칙
- `docs/architecture.md`: 패키지 구조와 계층 책임

---

## 적용 범위

이 문서는 기본적으로 `src/main/java/com/example/academy` 이하의 모든 신규 코드에 적용합니다.


---

## 핵심 원칙

다음 원칙을 우선합니다:

1. 패키지는 기술이 아니라 도메인 기준으로 분리합니다
2. 계층별 책임을 섞지 않습니다
3. setter 대신 의미 있는 메서드로 상태를 변경합니다
4. 새 코드는 레거시 패턴을 복제하지 않습니다
5. 최소 변경으로 문제를 해결하되, 수정한 범위 안에서는 코드를 더 명확하게 만듭니다

---

## 패키지와 계층 구조

최상위 패키지는 `com.example.academy.<context>` 형태를 사용합니다.

주요 컨텍스트는 아래 계층 구조를 따릅니다:

```text
presentation
application
domain
infrastructure
```

이 구조는 코드 탐색성과 변경 범위를 명확하게 유지하기 위한 기준입니다.

You MUST follow these rules:

1. `presentation`은 HTTP 요청/응답 처리에 집중합니다
2. `application`은 유스케이스 orchestration, 트랜잭션, 권한 검증을 담당합니다
3. `domain`은 비즈니스 규칙과 상태 변경을 담당합니다
4. `infrastructure`는 DB, Redis, JWT 같은 외부 기술 구현을 담당합니다

You MUST NOT:

- controller에 비즈니스 로직을 작성한다
- domain에서 Spring, JPA 구현체, 외부 API 세부사항에 직접 의존한다
- infrastructure 객체를 controller가 직접 호출하도록 연결한다

---

## Controller 규칙

controller는 진입점이며, 요청 해석과 응답 변환만 담당해야 합니다.

You MUST:

- `@RestController`와 명확한 `@RequestMapping`을 사용한다
- 요청 DTO에는 `@Valid` 기반 검증을 적용한다
- 응답은 프로젝트 표준인 `ApiResponse`로 감싼다
- 인증 사용자는 현재 프로젝트 패턴에 맞게 주입받아 사용한다

You SHOULD:

- controller 메서드 하나에서 한 유스케이스만 호출한다
- URL, 메서드명, 응답 타입이 같은 의도를 가리키도록 맞춘다

You MUST NOT:

- controller에서 repository를 직접 호출한다
- controller에서 엔티티 상태를 직접 변경한다
- controller에서 예외 포맷을 개별적으로 만들지 않는다

---

## Application Service 규칙

application service는 유스케이스의 흐름을 조합하는 계층입니다.

You MUST:

- `@Service`를 사용한다
- 트랜잭션 경계는 application service에 둔다
- 읽기 전용 조회는 `@Transactional(readOnly = true)`를 기본으로 둔다
- 권한 검증, 조회 조합, 이벤트 발행 순서를 여기서 제어한다

You SHOULD:

- 한 메서드가 하나의 유스케이스를 설명하도록 유지한다
- 외부 시스템 연동은 가능한 한 인터페이스 또는 이벤트 뒤로 숨긴다

You MUST NOT:

- application service가 HTTP 세부사항을 알게 만든다
- application service가 SQL/JPA 구현 디테일을 직접 다룬다
- 여러 유스케이스를 하나의 큰 메서드에 섞는다

---

## Domain 규칙

domain은 이 프로젝트의 핵심 규칙을 담는 계층입니다.

You MUST:

- 엔티티 상태 변경은 의미 있는 메서드로만 노출한다
- 값 객체로 표현할 수 있는 값은 VO를 우선 고려한다
- 도메인 규칙은 entity, value object, policy 안에 둔다
- JPA 기본 생성자는 `protected` 수준으로 제한한다

You SHOULD:

- 생성 시점에 유효한 상태를 보장하는 생성자를 사용한다
- `String`, `boolean`, `Long`만으로 의미를 표현하지 말고, 필요하면 타입으로 드러낸다

You MUST NOT:

- public setter를 추가한다
- entity를 단순 데이터 컨테이너처럼 사용한다
- presentation DTO를 domain에서 직접 사용한다

예시:

- `changeStatus`, `withdraw`처럼 의도가 드러나는 메서드명을 사용합니다
- `PhoneNumber`처럼 의미가 있는 값은 VO로 유지합니다

---

## DTO 규칙

DTO는 계층 간 데이터 전달 형식을 고정하는 역할만 수행해야 합니다.

You MUST:

- 요청 DTO는 `SomethingRequest`로 명명한다
- 응답 DTO는 `SomethingResponse`로 명명한다
- 단순 불변 데이터는 `record`를 우선 고려한다
- 요청 검증은 request DTO에서 처리한다

You SHOULD:

- DTO 이름에 화면이나 API 의미를 직접 드러낸다
- 필요한 경우 DTO 내부에 단순 변환 메서드를 둘 수 있지만, 복잡한 조합 로직은 application/domain으로 올린다

You MUST NOT:

- 신규 코드에서 `ReqDto`, `ResDto` 네이밍을 사용한다
- DTO에 비즈니스 정책을 넣는다
- entity를 API 응답으로 직접 노출한다

---

## 네이밍 규칙

이 프로젝트의 네이밍은 짧음보다 명확함을 우선합니다.

You MUST:

- 클래스명은 역할이 드러나게 작성한다
- 메서드명은 동사로 시작한다
- boolean 이름은 `is`, `has`, `can` 패턴을 우선 사용한다
- 조회 메서드는 `find`, `get`, `exists` 중 의미에 맞는 동사를 사용한다

You SHOULD:

- 축약어 사용을 최소화한다
- 같은 계층에서 비슷한 역할의 타입은 같은 접미사를 사용한다

You MUST NOT:

- 의미 없는 축약어를 새로 도입한다
- 같은 개념에 여러 이름을 혼용한다

---

## 의존성 주입과 객체 생성

객체 생성 방식은 테스트 가능성과 변경 용이성에 직접 영향을 줍니다.

You MUST:

- 생성자 주입을 사용한다
- Lombok을 사용할 경우 `@RequiredArgsConstructor`를 우선 사용한다
- 필드는 가능한 `final`로 선언한다

You MUST NOT:

- 필드 주입을 사용한다
- setter 주입을 사용한다
- 테스트 편의를 이유로 production code에 불필요한 setter를 추가한다

---

## 예외와 응답 규칙

예외 응답은 공통 규약으로 수렴해야 클라이언트와 테스트가 안정됩니다.

You MUST:

- 비즈니스 오류는 공통 예외 타입을 사용한다
- 예외 응답은 `ControllerExceptionAdvice`를 통해 일관되게 변환한다
- 성공 응답은 `ApiResponse` 규약을 사용한다

You MUST NOT:

- controller마다 예외 포맷을 다르게 만든다
- 예외를 숨기기 위해 `null`이나 빈 객체를 반환한다

---

## JPA / Persistence 규칙

영속성 코드는 도메인 모델을 저장하기 위한 구현 세부사항으로 다룹니다.

You MUST:

- repository interface는 domain에 둔다
- repository implementation은 infrastructure에 둔다
- 복잡한 조회 조건은 persistence 구현으로 격리한다
- 연관관계와 fetch 전략은 의도를 가지고 선언한다

You SHOULD:

- 조회 전용 로직과 상태 변경 로직을 구분한다
- Querydsl 같은 조회 최적화 코드는 infrastructure에서만 다룬다

You MUST NOT:

- controller나 service에서 엔티티 매핑 세부사항을 퍼뜨린다
- API 응답 편의를 위해 엔티티 구조를 먼저 왜곡한다

---

## 테스트와 변경 원칙

코드 스타일은 테스트 전략과 분리되지 않습니다.

You MUST:

- 동작을 바꾸는 변경에는 관련 테스트를 함께 수정하거나 추가한다
- 버그 수정 시 가능하면 회귀 테스트를 추가한다

You SHOULD:

- 단위 테스트, 통합 테스트, controller 테스트 중 가장 가까운 계층에서 먼저 검증한다
- fixture를 사용하되 테스트 의도가 흐려지지 않게 유지한다

You MUST NOT:

- 테스트 없이 동작 변경을 종료한다
- 테스트를 통과시키기 위해 실제 규칙을 약화한다

---

## 포맷팅 규칙

포맷팅은 가독성을 위한 최소 기준만 정의하고, 설계 규칙보다 우선하지 않습니다.

You MUST:

- 들여쓰기는 일관되게 유지한다
- wildcard import를 사용하지 않는다
- import는 정리된 상태를 유지한다
- annotation, blank line, 메서드 순서는 읽기 흐름이 드러나게 맞춘다

You SHOULD:

- 같은 파일 안에서는 같은 포맷 스타일을 유지한다
- 포맷 수정만 하는 커밋과 로직 수정 커밋을 가능하면 분리한다

---

## 완료 기준

코드 스타일 문서의 목적은 코드 리뷰에서 같은 지적을 반복하지 않게 만드는 것입니다.

작업 완료 시 기대 상태:

- 새 코드는 이 문서의 계층 기준을 따른 상태
- DTO, 예외, 응답 규약이 일관된 상태
- 도메인 규칙이 controller나 DTO에 새지 않은 상태
- 변경한 범위의 테스트와 검증이 통과한 상태
