# System Context & Architecture

## 1. 시스템 컨텍스트

```text
[기업 사용자]
   ↓
[AI 플랫폼 UI / Workflow]
   ↓
[API Gateway]
   ↓
[Policy Service / Rate Limit Service / Routing]
   ↓
[외부 AI API / 내부 서비스]
   ↓
[Usage / Billing / Notification / Recommendation]
```

## 2. 상위 아키텍처

### Front 영역
- 서비스 카탈로그
- 서비스 상세
- Workflow 생성/저장
- 실행 결과 확인
- 비용/사용량 대시보드 조회

### Core Control 영역
- API Gateway
- 인증/인가(후순위, MVP 제외)
- Policy Service
- Rate Limit Service
- Request Routing
- 공통 로깅

### Analytics 영역
- Usage Tracking
- Billing
- Notification
- Recommendation

## 3. 설계 원칙

1. 모든 실행 요청은 Gateway를 통과한다.
2. 모든 요청은 추적 가능한 request_id를 가진다.
3. Policy Service와 Rate Limit Service는 Gateway 앞단의 독립 제어 서비스로 동작한다.
4. Usage, Billing, Notification, Recommendation은 이벤트 기반 비동기 처리 가능 구조를 우선한다.
5. Recommendation과 Notification은 1차적으로 배치/지연 허용 구조로 시작한다.

## 4. 서비스 목록

| 서비스 | 목적 |
|--------|------|
| ai-platform-ui | 카탈로그/워크플로우/실행 UX |
| gateway-service | 단일 진입점, 오케스트레이션, 공통 제어, 추후 인증 관문 |
| policy-service | 정책 저장 및 평가 |
| rate-limit-service | 요청 제한 검사 |
| usage-service | 요청 로그 저장 / 집계 |
| billing-service | 비용 계산 / 비용 조회 |
| notification-service | 내부 이벤트 소비 기반 알림 확장 지점 |
| recommendation-service | 추천 생성 |
| external-llm-adapter | 외부 AI API 호출 어댑터(초기엔 gateway 내부 가능) |

## 5. 병렬 개발에 맞는 구현 순서

### 1단계: 계약 고정
- API Contract
- Event Contract
- Data Model

### 2단계: 목업 연동
- A는 mock API로 화면 먼저 개발
- B는 gateway + policy-service + rate-limit-service 먼저 개발
- C는 dummy event 기반 usage/billing/recommendation/notification 골격을 먼저 개발

### 3단계: 통합
- Gateway ↔ Policy/Rate Limit HTTP 연동
- Gateway ↔ Analytics 이벤트 연결
- Platform ↔ Gateway 실제 연동
- Dashboard ↔ Usage/Billing 조회 연결
