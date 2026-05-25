---
name: academy-pr
description: Use when the user wants a pull request draft for the current workspace changes, based on this repository's pull request template and the current git diff
---

# Academy PR

현재 워크트리 변경사항을 읽어서 `.github/PULL_REQUEST_TEMPLATE.md` 형식의 PR 초안을 출력할 때 사용합니다.

이 스킬은 코드를 수정하지 않습니다. 목적은 현재 작업을 빠르게 PR 문서로 정리해 사용자에게 보여주는 것입니다.

## 실행 순서

1. `.github/PULL_REQUEST_TEMPLATE.md`를 읽습니다.
2. 현재 변경사항을 확인합니다.
   - `git status --short`
   - `git diff --cached --stat`
   - `git diff --stat`
3. 변경 파일별 핵심 내용을 확인합니다.
   - staged 변경이 있으면 `git diff --cached`
   - unstaged 변경이 있으면 `git diff`
   - untracked 파일은 파일 내용을 직접 읽습니다
4. 확인한 내용만 바탕으로 PR 초안을 작성합니다.

## 작성 규칙

- 출력은 최종 PR 본문만 보여줍니다.
- 템플릿 섹션 순서를 그대로 유지합니다.
- 근거 없는 추측은 하지 않습니다.
- 이슈 번호를 모르면 `Closes #` 또는 `Refs #`는 비워 둡니다.
- 테스트를 실제로 실행하지 않았다면 테스트 체크박스는 체크하지 않습니다.
- self review, 네이밍 확인 같은 항목도 현재 세션에서 직접 확인한 경우에만 체크합니다.
- 변경 이유가 diff에서 명확하지 않으면 `참고 사항`에 "의도 추정이 필요한 부분"으로 짧게 적습니다.

## 출력 형식

아래 형식으로 완성된 PR 본문을 그대로 출력합니다.

```md
## 🔗 관련 이슈
Closes #
<!-- or -->
Refs #

---

## 변경 내용
- ...

---

## 변경 이유
- ...

---

## 테스트
- [ ] 로컬 테스트 완료
- [ ] 주요 시나리오 검증 완료
- [ ] 테스트 코드 추가/수정 완료

---

## 체크리스트
- [ ] self review 완료
- [ ] 불필요한 코드 제거 완료
- [ ] 네이밍 / 컨벤션 확인 완료

---

## 참고 사항
- ...
```

## 품질 기준

- `변경 내용`은 파일 나열이 아니라 사용자 관점의 변경 요약으로 씁니다.
- `변경 이유`는 구현 동기와 문제 해결 포인트를 한두 줄로 압축합니다.
- `참고 사항`에는 리뷰 포인트, 미검증 항목, 후속 작업만 남깁니다.
- 변경사항이 없으면 PR을 꾸미지 말고, 변경사항이 없다고 먼저 명확히 알립니다.
