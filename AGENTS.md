# AGENTS.md

## 목적

이 문서는 `academy-server`에서 작업할 때 항상 적용되는 저장소 공통 지침입니다.

---

## 우선 참고 문서

작업 전에 아래 문서를 우선 확인합니다.

1. `docs/architecture.md`
2. `docs/code-style.md`
3. `docs/requirements.md`
4. `docs/test-guide.md`

---

## 프로젝트 요약

이 프로젝트는 수강 신청 시스템의 Spring Boot 백엔드입니다.

- 현재 도메인 경계: `identity`, `course`, `enrollment`
- 현재 주요 어그리게이트: `User`, `Course`, `Enrollment`

---

## 작업 규칙

반드시 지킬 기준:

1. 먼저 관련 코드를 이해한 뒤 수정합니다.
2. 변경 범위는 요청된 목적에 맞게 최소화합니다.
3. 구조와 계층 책임은 `docs/architecture.md`, `docs/code-style.md` 기준을 따릅니다.
4. 가능한 경우 테스트와 검증까지 함께 맞춥니다.

---

## 주요 경로

- 애플리케이션 루트: `src/main/java/com/example/academy`
- 테스트: `src/test/java/com/example/academy`
- 설정: `src/main/resources`
- 구조와 책임: `docs/architecture.md`
- 코드 작성 기준: `docs/code-style.md`
- 구현 범위와 요구사항: `docs/requirements.md`

빠른 진입점과 도메인별 구조 설명은 `docs/architecture.md`를 우선 봅니다.

---

## 완료 기준

작업을 마치기 전에 아래를 확인합니다.

1. 변경 목적에 맞는 코드와 문서만 수정했는가
2. 관련 계층 책임이 유지되는가
3. 필요한 테스트 또는 검증을 실행했는가
4. 검증 결과를 설명할 수 있는가
5. 문서 변경이 필요한 경우 함께 반영했는가

---

## 문서 운영 원칙

루트 `AGENTS.md`는 짧고 공통적인 기준만 유지합니다.

상세 규칙은 `docs/` 문서나 필요한 하위 디렉터리의 추가 지침 파일로 분리합니다.
