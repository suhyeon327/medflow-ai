# AGENTS.md

# MedFlow AI Development Guide

## 목적

이 저장소에서 Codex가 코드를 수정하거나 추가할 때 따라야 할 기본 규칙이다.

# 기본 원칙

- 요청 범위를 벗어나는 리팩토링은 하지 않는다.
- 기존 코드의 수정은 최소 범위로 진행한다.
- 기존 프로젝트 구조와 코딩 스타일을 먼저 분석한다.
- 기존 코드 스타일에 따라 주석을 한글로 작성한다.
- 항상 BOM 없이 UTF-8로 파일을 읽고 작성한다.
- 변경 전 관련 파일과 영향 범위를 먼저 파악한다.
- 기존 클래스와 중복되는 클래스를 만들지 않는다.
- 패키지 구조를 임의로 변경하지 않는다.
- 사용자가 요청하지 않은 라이브러리나 의존성을 추가하지 않는다.
- 기존 기능이 동작하지 않도록 만드는 변경은 하지 않는다.
- Lombok과 프로젝트의 네이밍 규칙을 따른다.
- 코드보다 기존 설계를 우선 존중한다.

# 아키텍처 규칙

## Entity

- Entity를 Controller Response로 직접 반환하지 않는다.
- Request DTO와 Response DTO를 사용한다.
- Setter 사용을 최소화한다.
- 상태 변경은 Entity의 도메인 메서드로 구현한다.
- Entity의 비즈니스 로직을 Service에서 직접 구현하지 않는다.

## Service

- 필요한 데이터 변경은 하나의 @Transactional 범위에서 처리한다.
- 조회는 @Transactional(readOnly = true)를 사용한다.
- Service는 비즈니스 로직만 담당한다.

## Repository

- Spring Data JPA를 우선 사용한다.
- 복잡한 조회는 QueryDSL을 사용한다.
- 불필요한 Native Query는 작성하지 않는다.

# Security 규칙

- Spring Security를 사용한다.
- 권한 검증은 @PreAuthorize를 우선 사용한다.
- 로그인 사용자 ID는 AuthenticationPrincipal에서 가져온다.
- Request Body나 Path Variable로 사용자 ID를 전달받지 않는다.
- 본인 데이터인지 반드시 검증한다.

# Exception 규칙

- RuntimeException을 직접 사용하지 않는다.
- 기존 BusinessException을 사용한다.
- 기존 ErrorCode를 우선 재사용한다.
- 필요한 경우에만 새로운 ErrorCode를 추가한다.
- 예외 메시지는 ErrorCode에서 관리한다.

# API 규칙

- ApiResponse를 사용한다.
- RESTful API 규칙을 따른다.
- HTTP Status를 올바르게 사용한다.
- Entity를 직접 반환하지 않는다.
- Controller는 최대한 얇게 유지한다.

# JPA 규칙

- Lazy Loading을 기본으로 사용한다.
- N+1 문제가 예상되면 Fetch Join 또는 EntityGraph를 고려한다.
- 연관관계를 불필요하게 변경하지 않는다.
- Cascade는 필요한 경우에만 사용한다.

# 작업 방식

기능을 구현하기 전에 아래 순서를 따른다.

1. 관련 Entity 확인
2. Repository 확인
3. Service 확인
4. Controller 확인
5. DTO 확인
6. Security 구조 확인
7. 기존 코드 재사용 여부 확인
8. 최소 수정으로 구현

# 금지 사항

- 사용자가 요청하지 않은 대규모 리팩토링
- 사용자가 요청하지 않은 패키지 이동
- 사용자가 요청하지 않은 네이밍 변경
- 미사용 코드 대량 삭제
- 테스트를 제거하여 Build를 통과시키는 행위
- 기존 API를 임의로 변경하는 행위

# 작업 완료 후

최종 응답에는 아래 내용을 포함한다.

## 작업 요약

간단한 작업 내용

## 변경한 파일

파일 목록

## 핵심 수정 내용

변경 사항

## 테스트

실행한 테스트와 결과

## 검증 결과

빌드 성공 여부

## 남은 리스크

추가 확인이 필요한 사항

# 구현 원칙

항상 아래 원칙을 우선한다.

1. 기존 코드 재사용
2. 최소 수정
3. 기존 기능 유지
4. 컴파일 오류 없음
5. 테스트 통과
6. 확장 가능한 구조 유지
7. 유지보수가 쉬운 코드 작성

불확실한 부분은 기존 프로젝트 구조와 구현 방식을 우선 따른다.

## 문서 규칙

프로젝트 구조가 변경되면 PROJECT_GUIDE.md를 업데이트한다.

새로운 API가 추가되면 API.md를 업데이트한다.

ERD가 변경되면 ERD.md를 업데이트한다.