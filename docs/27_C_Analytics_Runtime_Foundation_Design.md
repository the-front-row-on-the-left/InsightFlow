# C Analytics Runtime Foundation Design

**Date:** 2026-03-20

**Goal**

C 역할 서비스가 실제 이벤트 흐름 위에서 동작하도록 `usage-service`, `billing-service`, `notification-service`를 순차적으로 완성한다. 이번 단계의 기준은 서비스별 독립 DB, Kafka 기반 이벤트 소비/발행, 조회 API가 같은 런타임 데이터에 연결되는 상태다.

## Scope

- `usage-service`에 독립 Postgres DB 연결
- `billing-service`에 독립 Postgres DB 연결
- `notification-service`에 독립 Postgres DB 연결
- `usage-service`가 `ai.requested`, `policy.checked`, `limit.applied`를 소비하고 `usage.tracked`를 발행
- `billing-service`가 `usage.tracked`를 소비하고 비용 계산 후 `cost.calculated`를 발행
- `notification-service`가 `cost.calculated`와 `optimization.recommended`를 소비해 내부 알림을 저장
- 각 서비스 조회 API가 메모리 seed가 아니라 실제 저장 데이터 기준으로 동작
- Docker Compose에 서비스별 DB를 반영

## Out of Scope

- `recommendation-service` 전용 DB 추가
- 운영 수준 재시도 정책, DLQ, 정확한 Kafka 운영 튜닝
- 고급 관측성 스택 추가
- 멀티 리전, 샤딩, 스키마 레지스트리

## Chosen Approach

이번 단계는 `usage -> billing -> notification` 순차 완성형으로 간다.

이 방식의 이유는 아래와 같다.

- `usage-service`가 전체 데이터 흐름의 시작점이다.
- `billing-service`는 `usage.tracked`의 형식이 정해져야 안전하게 계산할 수 있다.
- `notification-service`는 `cost.calculated`와 추천 이벤트가 있어야 의미 있는 저장/조회가 가능하다.

따라서 핵심 경로는 순차적으로 만들고, 테스트와 문서는 각 단계에서 함께 고정한다.

## Runtime Model

### Usage Service

- Kafka consumer로 `ai.requested`, `policy.checked`, `limit.applied`를 수신한다.
- `request_id` 기준으로 request 상태를 merge/upsert 한다.
- merge 결과에서 usage 계산에 필요한 필드가 충분히 모이면 `usage_records`에 저장한다.
- 저장 완료 후 `usage.tracked` 이벤트를 발행한다.
- 조회 API는 `usage_records` 기반으로 사용자/팀/서비스별 집계를 응답한다.

### Billing Service

- Kafka consumer로 `usage.tracked`를 수신한다.
- 가격표를 DB seed 또는 애플리케이션 초기화 데이터로 로딩한다.
- request 단위 비용을 계산해 `billing_records`에 저장한다.
- 저장 완료 후 `cost.calculated` 이벤트를 발행한다.
- 조회 API는 `billing_records`와 `pricing_tables` 기준으로 응답한다.

### Notification Service

- Kafka consumer로 `cost.calculated`, `optimization.recommended`를 수신한다.
- 사용자/팀 기준 구독 정책을 조회한다.
- 알림 projection을 생성해 `internal_notifications`에 저장한다.
- 조회 API는 저장된 subscription과 notification을 기준으로 응답한다.

### Recommendation Service

- FastAPI 계산 서비스로 유지한다.
- 별도 DB는 두지 않는다.
- `usage-service`, `billing-service` 조회 API를 읽어 추천을 계산한다.
- 이후 단계에서만 snapshot 저장 또는 ML 파이프라인을 고려한다.

## Database Topology

서비스별 DB를 분리한다.

- `usage-service` -> `usage_db`
- `billing-service` -> `billing_db`
- `notification-service` -> `notification_db`

각 서비스는 자기 DB만 소유한다. 다른 서비스 테이블을 직접 조회하지 않는다. 서비스 간 데이터 전달은 Kafka 이벤트와 공개 API로만 한다.

## Persistence Boundaries

### Usage Persistence

최소 저장 단위는 두 가지다.

- `usage_event_snapshots`
  - 원본 이벤트를 request 기준으로 병합하기 위한 상태 저장
- `usage_records`
  - 집계와 조회의 기준이 되는 정규화 usage 레코드

핵심 필드

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

### Billing Persistence

최소 저장 단위는 두 가지다.

- `pricing_tables`
- `billing_records`

핵심 필드

- `request_id`
- `user_id`
- `team_id`
- `service_id`
- `workflow_id`
- `model`
- `price_table_version`
- `currency`
- `input_unit_price`
- `output_unit_price`
- `cost_before_rounding`
- `cost_amount`
- `billable`
- `status`
- `calculated_at`

### Notification Persistence

최소 저장 단위는 두 가지다.

- `notification_preferences`
- `internal_notifications`

핵심 필드

- `notification_id`
- `request_id`
- `event_type`
- `channel`
- `recipient_type`
- `recipient_id`
- `title`
- `message`
- `status`
- `occurred_at`
- `metadata_json`

## Event Contracts

### Incoming to Usage

- `ai.requested`
- `policy.checked`
- `limit.applied`

이 세 이벤트는 최소한 아래 공통 필드를 가져야 한다.

- `request_id`
- `user_id`
- `team_id`
- `service_id`
- `workflow_id`
- `occurred_at`

추가로 `usage-service`가 최종 `usage.tracked`를 만들기 위해 아래 필드를 병합한다.

- `model`
- `status`
- `prompt_tokens`
- `completion_tokens`
- `total_tokens`
- `latency_ms`
- `billable`

### Outgoing from Usage

`usage.tracked`는 `billing-service`가 직접 계산 가능한 형태여야 한다.

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

### Outgoing from Billing

`cost.calculated`는 `notification-service`와 `recommendation-service`가 읽을 수 있는 형태여야 한다.

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

## Error Handling

- 같은 이벤트를 두 번 받아도 중복 저장/중복 발행이 발생하지 않아야 한다.
- 선행 이벤트보다 후행 이벤트가 먼저 와도 `request_id` 기준 병합이 가능해야 한다.
- 가격표를 찾지 못하면 `billing_records`에 실패 상태를 남기고 재처리 가능하게 한다.
- notification 생성 실패는 메시지 소비 전체를 깨뜨리기보다 개별 이벤트 실패로 남긴다.
- 조회 API는 데이터가 없어도 200과 빈 결과를 반환한다.

## Testing Strategy

### Usage

- 이벤트 병합 테스트
- 중복 이벤트 idempotency 테스트
- `usage.tracked` 발행 테스트
- DB 저장 기반 조회 API 테스트

### Billing

- `usage.tracked` 소비 테스트
- 가격표 기준 비용 계산 테스트
- 반올림 규칙 테스트
- `cost.calculated` 발행 테스트

### Notification

- `cost.calculated` 소비 테스트
- 구독 조건 매칭 테스트
- 알림 저장/조회 테스트
- 중복 알림 방지 테스트

### Integration

- Compose 기반으로 Kafka + 3개 DB + 3개 서비스가 뜨는지 확인
- 샘플 이벤트 투입 후 `usage -> billing -> notification` 데이터 흐름이 실제 저장으로 이어지는지 확인

## Delivery Order

1. `usage-service` DB 스키마, repository, consumer, producer, 조회 연동
2. `billing-service` DB 스키마, pricing seed, consumer, producer, 조회 연동
3. `notification-service` DB 스키마, consumer, projection 저장, 조회 연동
4. `recommendation-service`를 새 runtime 흐름에 맞게 조회 통합
5. Compose, runbook, 샘플 테스트 시나리오 정리

## Success Criteria

- `usage-service`가 Kafka 이벤트를 받아 DB에 usage 데이터를 저장하고 `usage.tracked`를 발행한다.
- `billing-service`가 `usage.tracked`를 받아 DB에 비용을 저장하고 `cost.calculated`를 발행한다.
- `notification-service`가 `cost.calculated`를 받아 DB에 내부 알림을 저장한다.
- 각 서비스 조회 API가 실제 저장 데이터 기준으로 동작한다.
- 세 서비스가 각자 독립 DB를 가진 상태로 Docker Compose에서 함께 실행된다.
