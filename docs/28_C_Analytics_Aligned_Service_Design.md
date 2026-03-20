# C Analytics Aligned Service Design

**Date:** 2026-03-20

**Goal**

사용자 정의에 맞춰 C 역할 서비스를 다시 정렬한다. 이번 설계의 기준은 `usage`, `billing`, `recommendation`, `notification`이 각각 명확한 책임을 가지고 Kafka 기반으로 연결되며, 최종적으로 `apps/ai-platform-ui`에서 간단한 AIOps 화면으로 확인 가능한 상태다.

**Supersedes**

- 이 문서는 C 역할 구현 범위에서 [`docs/27_C_Analytics_Runtime_Foundation_Design.md`](/Users/skax/Library/Mobile%20Documents/com~apple~CloudDocs/SKALA/agile_and_msa/codex_online_shopping_example/InsightFlow/docs/27_C_Analytics_Runtime_Foundation_Design.md#L1) 를 대체한다.
- 이 문서는 C 역할과 직접 맞닿는 Kafka 계약에 대해 [`docs/06_Event_Contract.md`](/Users/skax/Library/Mobile%20Documents/com~apple~CloudDocs/SKALA/agile_and_msa/codex_online_shopping_example/InsightFlow/docs/06_Event_Contract.md#L1) 의 일부를 구체화한다.

## Service Definitions

### Usage Service

- 실행 요청의 사용량을 저장하고 조회하는 서비스
- `ai.requested`, `ai.completed` 이벤트를 기준으로 request 단위 usage를 만든다
- 사용자/팀 기준 사용량 조회 API를 제공한다

### Billing Service

- usage 데이터를 바탕으로 비용을 계산하고 조회하는 서비스
- `usage.tracked` 이벤트를 받아 비용을 계산한다
- 사용자/팀 기준 비용 조회 API를 제공한다

### Recommendation Service

- 토큰/비용 절감을 위한 추천을 제공하는 서비스
- `usage`와 `billing` 데이터를 읽어 추천을 계산한다
- 사용자 기준 추천 조회 API를 제공한다
- 초기 구현은 FastAPI 기반 규칙 엔진으로 간다
- 이번 단계에서는 Kafka producer 역할을 갖지 않는다

### Notification Service

- 사용량 제한 초과, 비용 급증 같은 운영 알림을 제공하는 서비스
- `limit.exceeded`, `cost.calculated`를 받아 알림을 저장한다
- 사용자/팀 기준 알림 조회 API를 제공한다

## Runtime Flow

1. 실행 계층이 `ai.requested` 이벤트를 발행한다.
2. 실행 완료 시 `ai.completed` 이벤트를 발행한다.
3. `usage-service`가 두 이벤트를 합쳐 `usage.tracked`를 저장/발행한다.
4. `billing-service`가 `usage.tracked`를 받아 비용을 계산하고 `cost.calculated`를 저장/발행한다.
5. `recommendation-service`가 `usage-service`와 `billing-service`를 조회해 추천을 계산하고 API 응답으로 반환한다.
6. `notification-service`가 `limit.exceeded`, `cost.calculated`를 받아 운영 알림을 저장한다.
7. `gateway-service`가 analytics 조회용 BFF 엔드포인트를 제공한다.
8. `apps/ai-platform-ui`가 Gateway만 호출해 AIOps 화면을 구성한다.

## Contract Decisions

- `ai.requested` producer는 `gateway-service`다.
- `ai.completed` producer도 이번 단계에서는 `gateway-service`다.
- `limit.exceeded` producer는 `rate-limit-service`다.
- `usage-service`는 `ai.completed`를 받은 시점에 `request_id` 기준 merge가 완결되었다고 보고 `usage.tracked`를 생성한다.
- `recommendation-service`는 이번 단계에서 `optimization.recommended` 이벤트를 발행하지 않는다. 추천은 조회 시점 계산으로 고정한다.
- `notification-service`의 비용 이상 알림은 `cost.calculated`를 소비한 뒤 서비스 내부 임계치 규칙으로 판정한다.
- 프론트엔드는 analytics 서비스에 직접 붙지 않고, `gateway-service`의 AIOps BFF 엔드포인트만 호출한다.

## Event Contracts

### ai.requested

필수 필드

- `event_id`
- `event_type=ai.requested`
- `request_id`
- `user_id`
- `team_id`
- `service_id`
- `workflow_id`
- `requested_at`
- `payload.model`

### ai.completed

필수 필드

- `event_id`
- `event_type=ai.completed`
- `request_id`
- `model`
- `status`
- `prompt_tokens`
- `completion_tokens`
- `total_tokens`
- `latency_ms`
- `billable`
- `completed_at`

### usage.tracked

필수 필드

- `event_id`
- `event_type=usage.tracked`
- `request_id`
- `user_id`
- `team_id`
- `service_id`
- `workflow_id`
- `model`
- `status`
- `prompt_tokens`
- `completion_tokens`
- `total_tokens`
- `latency_ms`
- `billable`
- `occurred_at`
- `tracked_at`

### cost.calculated

필수 필드

- `event_id`
- `event_type=cost.calculated`
- `request_id`
- `user_id`
- `team_id`
- `service_id`
- `workflow_id`
- `model`
- `currency`
- `cost_amount`
- `price_table_version`
- `billable`
- `status`
- `calculated_at`

### limit.exceeded

필수 필드

- `event_id`
- `event_type=limit.exceeded`
- `request_id`
- `user_id`
- `team_id`
- `service_id`
- `limit_type`
- `threshold`
- `observed_value`
- `occurred_at`

## Event Envelope

모든 Kafka 이벤트는 아래 envelope를 사용한다.

```json
{
  "event_id": "evt_123",
  "event_type": "usage.tracked",
  "occurred_at": "2026-03-20T10:00:00Z",
  "request_id": "req_123",
  "payload": {}
}
```

값 규칙

- `event_id`: dedupe용 전역 유니크 문자열
- `event_type`: 고정 문자열
- `occurred_at`: producer가 이벤트 사실을 확정한 시각
- `request_id`: request 기반 흐름을 잇는 키
- `payload`: 이벤트 타입별 상세 본문

타임스탬프 규칙

- `ai.requested.occurred_at` = `requested_at`
- `ai.completed.occurred_at` = `completed_at`
- `usage.tracked.occurred_at` = `completed_at`
- `cost.calculated.occurred_at` = `calculated_at`
- `limit.exceeded.occurred_at` = limit 판정 시각

상태/enum 규칙

- `status`: `SUCCESS | FAILED | BLOCKED`
- `billable`: `true | false`
- `limit_type`: `REQUEST_RATE | DAILY_TOKEN | MONTHLY_TOKEN | MONTHLY_COST`

## Persistence Boundaries

### usage-service

독립 DB `usage_db`

테이블

- `usage_event_snapshots`
- `usage_records`

### billing-service

독립 DB `billing_db`

테이블

- `pricing_tables`
- `billing_records`

### notification-service

독립 DB `notification_db`

테이블

- `notification_preferences`
- `internal_notifications`

### recommendation-service

- 이번 단계에서는 별도 DB를 두지 않는다
- `usage-service`, `billing-service` API를 조회해 추천을 계산한다

## Database Topology

로컬 Compose에서는 Postgres 컨테이너 하나를 사용하되, 내부에 논리 DB 세 개를 둔다.

- `usage_db`
- `billing_db`
- `notification_db`

각 서비스는 자기 DB만 접속한다.

## Operational Semantics

- Kafka dedupe 키는 `event_id`다
- 비즈니스 upsert 키는 `request_id` 또는 `recommendation_id`다
- 각 consumer는 처리한 `event_id`를 기록해 중복 소비를 무시할 수 있어야 한다
- DB 저장이 성공하고 이벤트 발행이 실패하는 경우를 대비해 producer 단계는 outbox 또는 재발행 가능한 상태 저장을 가져야 한다
- 이번 단계의 완료선에서는 최소 구현으로 DB 저장 후 발행 실패 상태를 기록하고 재시도 가능한 구조를 둔다
- `usage-service`는 `ai.completed`가 없으면 `usage.tracked`를 발행하지 않는다
- `billing-service`는 `status=BLOCKED` 또는 `billable=false` 이면 0 비용 레코드를 저장할 수 있다
- `notification-service`의 비용 이상 알림 규칙은 “최근 7일 평균 대비 2배 초과” 대신 MVP 단순 규칙으로 시작한다:
  - `cost_amount >= 1000 KRW`
  - 또는 `billable=true` 이고 `status=FAILED`

## Gateway Boundary

UI는 Gateway만 호출한다. Gateway는 아래 BFF 엔드포인트를 analytics 서비스로 프록시한다.

- `GET /api/aiops/usage/users/{user_id}`
- `GET /api/aiops/billing/users/{user_id}`
- `GET /api/aiops/recommendations?user_id=...`
- `GET /api/aiops/notifications?user_id=...`

이번 단계에서는 Gateway가 응답 스키마를 재가공하지 않고, 각 analytics 서비스의 응답을 그대로 전달하는 pass-through 형태로 구현한다.

## AIOps UI Integration

기존 `apps/ai-platform-ui`를 AIOps 진입점으로 사용한다.

최소 화면

- usage overview
- billing overview
- recommendation list
- notification list

최소 API 연결

- `GET /api/aiops/usage/users/{user_id}`
- `GET /api/aiops/billing/users/{user_id}`
- `GET /api/aiops/recommendations?user_id=...`
- `GET /api/aiops/notifications?user_id=...`

## Success Criteria

- `usage-service`가 Kafka 이벤트를 받아 실제 usage를 저장/조회한다
- `billing-service`가 `usage.tracked`를 받아 실제 비용을 저장/조회한다
- `recommendation-service`가 usage/billing 기반 추천을 반환한다
- `notification-service`가 제한 초과 또는 비용 관련 알림을 저장/조회한다
- `apps/ai-platform-ui`에서 위 네 종류의 정보를 한 화면군에서 볼 수 있다
