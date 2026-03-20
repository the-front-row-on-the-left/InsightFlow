# C Analytics Aligned Service Design

**Date:** 2026-03-20

**Goal**

사용자 정의에 맞춰 C 역할 서비스를 다시 정렬한다. 이번 설계의 기준은 `usage`, `billing`, `recommendation`, `notification`이 각각 명확한 책임을 가지고 Kafka 기반으로 연결되며, 최종적으로 `apps/ai-platform-ui`에서 간단한 AIOps 화면으로 확인 가능한 상태다.

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

### Notification Service

- 사용량 제한 초과, 비용 급증 같은 운영 알림을 제공하는 서비스
- `limit.exceeded`, `cost.calculated`, `optimization.recommended`를 받아 알림을 저장한다
- 사용자/팀 기준 알림 조회 API를 제공한다

## Runtime Flow

1. 실행 계층이 `ai.requested` 이벤트를 발행한다.
2. 실행 완료 시 `ai.completed` 이벤트를 발행한다.
3. `usage-service`가 두 이벤트를 합쳐 `usage.tracked`를 저장/발행한다.
4. `billing-service`가 `usage.tracked`를 받아 비용을 계산하고 `cost.calculated`를 저장/발행한다.
5. `recommendation-service`가 `usage-service`와 `billing-service`를 조회해 추천을 계산하고 필요 시 `optimization.recommended`를 발행한다.
6. `notification-service`가 `limit.exceeded`, `cost.calculated`, `optimization.recommended`를 받아 운영 알림을 저장한다.
7. `apps/ai-platform-ui`가 usage, billing, recommendation, notification을 조회해 AIOps 화면을 구성한다.

## Event Contracts

### ai.requested

필수 필드

- `event_id`
- `request_id`
- `user_id`
- `team_id`
- `service_id`
- `workflow_id`
- `requested_at`

### ai.completed

필수 필드

- `event_id`
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
- `request_id`
- `user_id`
- `team_id`
- `service_id`
- `limit_type`
- `threshold`
- `observed_value`
- `occurred_at`

### optimization.recommended

필수 필드

- `event_id`
- `recommendation_id`
- `user_id`
- `team_id`
- `service_id`
- `current_model`
- `recommended_model`
- `estimated_token_savings`
- `estimated_cost_savings`
- `reason`
- `recommended_at`

## Persistence Boundaries

### usage-service

독립 DB `usage_db`

테이블

- `request_event_snapshots`
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
- `notifications`

### recommendation-service

- 이번 단계에서는 별도 DB를 두지 않는다
- `usage-service`, `billing-service` API를 조회해 추천을 계산한다

## Operational Semantics

- Kafka dedupe 키는 `event_id`다
- 비즈니스 upsert 키는 `request_id` 또는 `recommendation_id`다
- 각 consumer는 처리한 `event_id`를 기록해 중복 소비를 무시할 수 있어야 한다
- DB 저장이 성공하고 이벤트 발행이 실패하는 경우를 대비해 producer 단계는 outbox 또는 재발행 가능한 상태 저장을 가져야 한다
- 이번 단계의 완료선에서는 최소 구현으로 DB 저장 후 발행 실패 상태를 기록하고 재시도 가능한 구조를 둔다

## AIOps UI Integration

기존 `apps/ai-platform-ui`를 AIOps 진입점으로 사용한다.

최소 화면

- usage overview
- billing overview
- recommendation list
- notification list

최소 API 연결

- `GET /api/usage/users/{user_id}`
- `GET /api/billing/users/{user_id}`
- `GET /api/recommendations?user_id=...`
- `GET /internal/notifications?user_id=...`

## Success Criteria

- `usage-service`가 Kafka 이벤트를 받아 실제 usage를 저장/조회한다
- `billing-service`가 `usage.tracked`를 받아 실제 비용을 저장/조회한다
- `recommendation-service`가 usage/billing 기반 추천을 반환한다
- `notification-service`가 제한 초과 또는 비용 관련 알림을 저장/조회한다
- `apps/ai-platform-ui`에서 위 네 종류의 정보를 한 화면군에서 볼 수 있다
