# ADR-001: Feature-based Package Structure

## Status

Accepted

## Date

2026-07-02

---

## Context

프로젝트는 AI 기반 의료 예약·문진 플랫폼으로 시작하지만, 향후 다음 기능들이 지속적으로 추가될 예정이다.

- 의료진 관리
- 예약 관리
- AI 문진
- 진료 기록
- 알림
- 관리자 기능
- MCP Server
- 외부 병원 API 연동

초기에는 규모가 작지만 프로젝트가 성장할 가능성을 고려해야 한다.

일반적인 Layered Package 구조(controller, service, repository)는 프로젝트 규모가 커질수록 관련 코드가 여러 패키지로 흩어져 유지보수가 어려워질 수 있다.

---

## Decision

기능(Feature) 단위로 패키지를 구성한다.

예시

```
com.medflow

├── auth
├── patient
├── doctor
├── hospital
├── reservation
├── questionnaire
├── notification
└── common
```

각 Feature 내부에는 해당 기능에 필요한 Controller, Service, Repository, Entity, DTO 등을 함께 배치한다.

예시

```
reservation

├── controller
├── service
├── repository
├── domain
├── dto
└── exception
```

---

## Consequences

### 장점

- 기능 응집도가 높아진다.
- 관련 코드의 탐색이 쉬워진다.
- 기능 단위 개발 및 유지보수가 편리하다.
- 향후 MSA로 분리하기 쉬운 구조이다.
- 새로운 개발자가 프로젝트를 이해하기 쉽다.

### 단점

- 초기 프로젝트에서는 다소 복잡하게 느껴질 수 있다.
- 기능별 패키지 설계 경험이 부족하면 구조가 일관되지 않을 수 있다.

---

## Alternatives Considered

### Layered Package

```
controller
service
repository
entity
dto
```

장점

- 초기에 이해하기 쉽다.
- 예제가 많다.

단점

- 프로젝트 규모가 커질수록 동일 기능의 코드가 여러 패키지에 흩어진다.
- 기능 변경 시 여러 디렉터리를 동시에 수정해야 한다.

---

## Decision Rationale

본 프로젝트는 단순 CRUD 프로젝트가 아니라 의료 플랫폼을 목표로 한다.

프로젝트의 확장성과 유지보수성을 고려하여 Feature-based Package Structure를 선택하였다.