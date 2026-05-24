## API 명세서 가이드

서버의 기본 상태를 확인하고, API 문서 사용 방식과 공용 링크 및 에러 정의를 제공합니다.

---

## ✅ 서버 상태 체크

<details>
<summary><strong>상세 보기</strong></summary>

현재 위치한 API에 Try it out -> Execute 버튼을 눌러 확인합니다.

**정상 응답**
```text
{
  "message": "OK",
  "data": null
}
```

</details>

---

## 🔐 JWT 토큰 사용법

<details>
<summary><strong>상세 보기</strong></summary>

로그인 후 응답된 JWT 토큰을 복사해주세요.

1. Swagger 우측 상단의 **Authorize** 버튼을 클릭합니다.
2. `value` 입력란에 복사한 토큰(`Bearer {token}` 형식)을 붙여넣고 저장합니다.
3. 이제 모든 요청은 해당 JWT가 자동 포함되어 전송됩니다.

</details>

---

## 💬 도메인 언어 (Ubiquitous Language)

<details>
<summary><strong>상세 보기</strong></summary>

프로젝트 전반에서 사용하는 주요 도메인 용어입니다.

| **용어**         | 설명 |
|------------| --- |
| **Class**  | 강의 |
| **Creator**    | 강의를 개설하는 사용자(강사) |
| **Enrollment** | 수강 신청 |
| **DRAFT**      | 초안 상태 (신청 불가) |
| **OPEN**       | 모집 중 상태 (신청 가능) |
| **CLOSED**     | 모집 마감 상태 (신청 불가) |
| **PENDING**    | 신청 완료, 결제 대기 상태 |
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
