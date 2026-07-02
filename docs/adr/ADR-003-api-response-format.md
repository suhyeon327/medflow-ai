# ADR-003: Standard API Response Format

## Status

Accepted

## Date

2026-07-02

---

## Context

프로젝트의 API가 증가함에 따라 응답 형식이 일관되지 않으면 클라이언트 개발과 예외 처리가 복잡해질 수 있다.

특히 다음 상황에서 문제가 발생한다.

- 성공 응답마다 다른 구조
- 예외 응답마다 다른 형식
- 프론트엔드 예외 처리 복잡도 증가
- API 문서 관리 어려움

---

## Decision

모든 API는 공통 응답 객체(ApiResponse)를 사용한다.

성공 응답

```json
{
  "success": true,
  "data": {
    ...
  },
  "timestamp": "2026-07-02T15:30:21"
}
```

실패 응답

```json
{
  "success": false,
  "error": {
    "code": "RESERVATION_001",
    "message": "이미 예약된 시간입니다."
  },
  "timestamp": "2026-07-02T15:30:21"
}
```

모든 예외는 GlobalExceptionHandler에서 처리한다.

---

## Consequences

### 장점

- 모든 API 응답 형식이 동일하다.
- 프론트엔드 예외 처리가 단순해진다.
- Swagger 문서가 일관된다.
- 유지보수가 쉬워진다.

### 단점

- HTTP Status와 별도로 Error Code를 관리해야 한다.
- 초기 구현이 다소 번거롭다.

---

## Alternatives Considered

### API마다 개별 Response 반환

예시

```
LoginResponse

ReservationResponse

HospitalResponse
```

장점

- 구현이 단순하다.

단점

- 응답 형식이 일관되지 않다.
- 예외 처리 방식이 통일되지 않는다.

---

## Decision Rationale

본 프로젝트는 실무 수준의 API 설계를 목표로 한다.

일관된 응답 형식은 프론트엔드와의 협업을 단순하게 만들고, API 문서 관리와 예외 처리를 쉽게 해준다.

향후 모바일 앱, 웹 클라이언트, AI 서비스(MCP) 등 다양한 클라이언트가 동일한 응답 형식을 사용할 수 있도록 공통 응답 구조를 채택하였다.