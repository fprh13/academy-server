## API 명세서 가이드

서버의 기본 상태를 확인하고, API 문서 사용 방식과 공용 링크 및 에러 정의를 제공합니다.

---

## 📄 API 테스트 사용법

<details>
<summary><strong>상세 보기</strong></summary>


특정 API에 `Try it out`을 클릭 후 Examples: 하단에 시나리오에 맞는 예시 데이터를 선택하거나, 원하는 데이터를 입력한 후 `Execute`합니다.

인증이 필요한 경우 하단 `인증(JWT) 정보`를 확인해주세요.



</details>

---

## 🔐 인증(JWT) 정보

<details>
<summary><strong>상세 보기</strong></summary>

로그인 후 응답된 JWT 토큰을 복사해주세요. 혹은 하단 수강생/강사 계정의 JWT 토큰을 복사해주세요.

1. Swagger 우측 상단의 **Authorize** 버튼을 클릭합니다.
2. `value` 입력란에 복사한 토큰(`Bearer {token}` 형식)을 붙여넣고 저장합니다.
3. 이제 모든 요청은 해당 JWT가 자동 포함되어 전송됩니다.

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


</details>

---

## 📚 더미 데이터 정보

<details>

<summary><strong>상세 보기</strong></summary>

- 강사(`creator`)는 총 10개의 강의를 보유하고 있습니다.
- 수강생(`classmate`)은 아래와 같은 수강 신청 이력을 가지고 있습니다.

| 강의 ID | 상태 |
|---|---|
| 1번 강의 | `PENDING` |
| 2번 강의 | `CONFIRMED` |
| 3번 강의 | `CANCELLED` |
| 4번 강의 | `CONFIRMED` |

위 데이터를 기반으로 수강 신청, 취소, 목록 조회 등의 시나리오를 테스트할 수 있습니다.

</details>

---

## 💬 도메인 언어 (Ubiquitous Language)

<details>
<summary><strong>상세 보기</strong></summary>

프로젝트 전반에서 사용하는 주요 도메인 용어입니다.

| **용어**         | 설명 |
|----------------| --- |
| **Course**     | 강의 |
| **Creator**    | 강의를 개설하는 사용자(강사) |
| **Enrollment** | 수강 신청 |
| **DRAFT**      | 초안 상태 (신청 불가) |
| **OPEN**       | 모집 중 상태 (신청 가능) |
| **CLOSED**     | 모집 마감 상태 (신청 불가) |
| **PENDING**    | 신청 완료, 결제 대기 상태 |
| **WAITING**    | 웨이팅 대기열 상태 |
| **CONFIRMED**  | 결제 완료, 수강 확정 상태 |
| **CANCELLED**  | 수강 취소 상태 |

</details>

---

## ❗ ERROR 정의

<details>
<summary><strong>상세 보기</strong></summary>

| HttpStatus | 설명 | 해결방법 |
|-----------|------|----------|
| **400 Bad Request** | 클라이언트 요청 데이터가 잘못된 경우입니다. | 요청 데이터의 유효 값을 확인해주세요. |
| **401 Unauthorized** | 인증 정보가 없거나 유효하지 않습니다. | 로그인 후 발급된 JWT를 전송해주세요. |
| **403 Forbidden** | 현재 권한으로는 자원에 접근할 수 없습니다.<br>(예: USER 권한이 ADMIN 전용 API 접근 시) | 요청 자원에 맞는 권한으로 요청해주세요. |
| **404 Not Found** | 요청한 데이터를 찾을 수 없습니다. | 요청한 ID나 경로가 올바른지 확인해주세요. |
| **405 Method Not Allowed** | 지원하지 않는 HTTP 메서드로 요청했습니다. | 지원하는 HTTP Method를 사용해주세요. |
| **409 Conflict** | 리소스 충돌(중복 데이터) 발생 | 중복 여부를 확인 후 요청해주세요. |
| **500 Internal Server Error** | 서버 내부에서 예상치 못한 오류 발생 | 담당 개발자에게 문의해주세요. 요청한 API와 데이터를 함께 전달하면 도움이 됩니다. |

</details>

---

## 🔗 링크

<details>
<summary><strong>상세 보기</strong></summary>

- [Github Repository 바로가기](https://github.com/fprh13/academy-server)

</details>
