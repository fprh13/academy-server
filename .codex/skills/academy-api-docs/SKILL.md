---
name: academy-api-docs
description: Use when the user wants scenario-based API documentation tests for this academy repository, matching the existing ControllerTest and Spring RestDocs style
---

# Academy API Docs

이 스킬은 설명 문서를 쓰는 것이 아니라, 기존 스타일에 맞는 시나리오별 API 문서 산출 테스트 코드를 작성할 때 사용합니다.

기준은 `src/test/java/com/example/academy/identity/presentation/UserControllerTest.java`와 `AuthControllerTest.java`입니다.

## 입력 규칙

사용자는 아래 정보를 줍니다.

- 대상 API의 컨트롤러 메서드 또는 관련 파일 경로
- 원하는 시나리오 목록

필요하면 아래도 함께 받습니다.

- 요청/응답 DTO
- 서비스 메서드 시그니처
- 예외 정책

시나리오는 사용자가 준 목록을 우선합니다. 스킬이 임의로 실패 케이스를 과장해서 추가하지 않습니다.

## 먼저 확인할 것

1. 대상 controller, request/response DTO, service를 읽습니다.
2. 기존 테스트 파일이 있으면 먼저 읽습니다.
3. 아래 기준 파일을 같이 확인합니다.
   - `src/test/java/com/example/academy/identity/presentation/UserControllerTest.java`
   - `src/test/java/com/example/academy/identity/presentation/AuthControllerTest.java`
   - `src/test/java/com/example/academy/support/RestDocsSupport.java`
4. 필요 시 `docs/architecture.md`, `docs/code-style.md`, `docs/requirements.md`를 확인합니다.

## 산출물 기준

- 결과는 `ControllerTest`에 바로 넣을 수 있는 테스트 코드여야 합니다.
- API 하나를 `@Nested` 클래스로 묶습니다.
- 시나리오별로 `@Test` 메서드를 작성합니다.
- 메서드명은 기존 스타일처럼 한글 시나리오명 기반으로 작성합니다.
- 각 테스트는 `//given`, `//when`, `//then` 구조를 유지합니다.
- `mockMvc.perform(...)`, 상태 코드 검증, `jsonPath` 검증, `restDocsHandler.document(...)`를 포함합니다.

## 스타일 고정 규칙

- 성공 시나리오와 실패 시나리오를 각각 별도 문서 산출 대상으로 취급합니다.
- 성공 시나리오는 필요할 때만 `summary`, `description`, `requestFields`, `responseFields`를 자세히 씁니다.
- 실패 시나리오는 `tag`, `requestSchema`, `responseSchema` 중심으로 간결하게 유지합니다.
- 성공 응답은 `ApiResponse`, 실패 응답은 `ApiErrorResponse` 기준으로 문서화합니다.
- 검증은 기존 테스트처럼 상태 코드, 예외 타입, 메시지를 우선 확인합니다.
- Mockito 사용 방식도 기존 스타일을 따릅니다.
  - 성공: `Mockito.when(...).thenReturn(...)`
  - 실패: `Mockito.doThrow(...).when(...)`
  - validation 실패: 서비스가 호출되지 않았는지 `Mockito.verify(..., Mockito.never())`를 고려합니다.

## RestDocs 작성 기준

- `ResourceDocumentation.resource(ResourceSnippetParameters.builder()...)` 패턴을 사용합니다.
- `tag`는 기존 컨트롤러 테스트의 네이밍 톤을 따릅니다.
- `summary`는 API 한 줄 목적을 적습니다.
- `description`은 사용법이나 주의사항이 실제로 필요할 때만 추가합니다.
- 요청 바디가 있으면 `requestSchema`, `requestFields`를 작성합니다.
- 응답 필드가 중요하면 `responseFields`를 작성합니다.
- 쿼리 파라미터, 경로 변수, 인증 요구 여부는 실제 API 시그니처에 맞춰 드러냅니다.

## 시나리오 체크 기준

아래는 기본 체크 기준입니다. 실제 작성은 사용자가 준 시나리오 목록을 우선합니다.

- `2XX` 성공
- `4XX` 요청 데이터 유효성 검사 실패
- `4XX` 중복/충돌
- `4XX` 리소스 없음
- `4XX` 인증/인가 실패
- 요구사항상 중요한 비즈니스 실패 시나리오

## 출력 규칙

- 가능하면 완성된 테스트 코드 블록으로 출력합니다.
- 어느 테스트 파일에 넣을지 함께 적습니다.
- 기존 테스트 파일에 추가하는 경우, 어느 `@Nested` 아래에 둘지 제안합니다.
- 정보가 부족하면 임의 구현 대신 `사용자 확인 필요`를 짧게 남깁니다.
- 저장소 스타일과 다르면 새로운 방식으로 일반화하지 말고 기존 패턴을 우선 복제합니다.
