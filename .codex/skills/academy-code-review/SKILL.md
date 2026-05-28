---
name: academy-code-review
description: Use when the user wants a review of specific files, directories, packages, or changes in this academy repository, especially to find critical problems, architectural risks, or better alternatives
---

# Academy Code Review

지정한 파일, 디렉터리, 패키지, 변경사항을 리뷰할 때 사용합니다.

목표는 칭찬이 아니라 치명적인 문제, 요구사항 불일치, 구조적 리스크, 더 나은 대안을 빠르게 찾는 것입니다.

## 리뷰 원칙

- 사용자가 지정한 위치를 먼저 읽습니다.
- 치명적 문제 판단에 필요하면 관련 계층과 테스트까지 범위를 넓힙니다.
- 이 저장소에서는 `docs/architecture.md`, `docs/code-style.md`, `docs/requirements.md`를 리뷰 기준으로 사용합니다.
- 추측성 지적은 하지 않습니다. 근거가 약하면 `Open Questions`로 분리합니다.

## 확인 순서

1. 사용자가 지정한 파일이나 경로를 읽습니다.
2. 필요 시 아래 주변 맥락을 추가로 봅니다.
   - controller를 보면 application service, request/response DTO
   - application을 보면 domain, repository interface, transaction 경계
   - domain을 보면 상태 변경 메서드, 불변식, aggregate 경계
   - 기능 코드가 있으면 관련 테스트 유무
3. 아래 우선순위로 문제를 찾습니다.
   - 치명적 버그나 런타임 실패 가능성
   - 요구사항 불일치
   - 계층 책임 위반과 아키텍처 침범
   - 동시성, 트랜잭션, 정합성 문제
   - 테스트 부재 또는 검증 공백
   - 더 단순하거나 안전한 대안

## 저장소 특화 체크포인트

- `presentation -> application -> domain -> infrastructure` 계층이 섞이지 않았는지 봅니다.
- controller에 비즈니스 로직이나 repository 직접 호출이 없는지 봅니다.
- domain이 setter 중심 데이터 컨테이너로 무너졌는지 봅니다.
- 생성자 기반 설계와 명확한 네이밍을 지키는지 봅니다.
- `identity`, `course`, `enrollment` 경계를 침범하지 않는지 봅니다.
- 정원, 상태 전이, 취소 가능 기간, 동시성 같은 요구사항 핵심 규칙이 빠지지 않았는지 봅니다.

## 출력 형식

문제부터 바로 출력합니다. 요약이 필요해도 findings 뒤에 둡니다.

```md
Findings

1. [CRITICAL] path/to/file:line
문제: ...
영향: ...
대안: ...

2. [HIGH] path/to/file:line
문제: ...
영향: ...
대안: ...

Open Questions
- ...

Residual Risks
- ...
```

## 출력 규칙

- 치명적 문제부터 심각도 순으로 정렬합니다.
- 각 항목에는 `문제`, `영향`, `대안`을 반드시 적습니다.
- 치명적 문제가 없으면 중요한 리스크와 대안을 대신 제시합니다.
- 라인 번호를 알 수 있으면 포함하고, 없으면 파일 경로라도 명시합니다.
- 수정 제안은 이 저장소의 구조와 규칙 안에서 제시합니다.
- “문제 없음”으로 끝내지 말고, 남은 리스크나 테스트 공백이 있으면 같이 적습니다.
