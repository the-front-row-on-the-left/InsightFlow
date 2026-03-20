# Event Contract (Kafka)

## 1. 이벤트 설계 원칙

- 모든 이벤트는 `event_id`, `event_type`, `occurred_at`를 가진다.
- `request_id`는 필수다.
- `user_id`, `team_id`, `service_id`는 가능하면 항상 포함한다.
- 금액/토큰 등 계산 값은 원본과 계산 결과를 구분한다.

## 2. 공통 Envelope

```json
{
  "event_id": "evt_001",
  "event_type": "ai.requested",
  "occurred_at": "2026-03-20T10:00:00Z",
  "request_id": "req_001",
  "payload": {}
}
```

## 3. 이벤트 목록

| 이벤트 | Producer | Consumer | 목적 |
|--------|----------|----------|------|
| ai.requested | Gateway | Usage | 실행 진입 기록 |
| policy.checked | Policy | Usage | 정책 결과 분석 |
| limit.applied | Rate Limit | Usage | 제한 적용 분석 |
| usage.tracked | Usage | Billing, Recommendation, Notification | 정규화된 사용량 이벤트 |
| cost.calculated | Billing | Recommendation, Platform | 비용 결과 공유 |
| optimization.recommended | Recommendation | Platform | 추천 제공 |

## 4. 주요 이벤트 스키마

### ai.requested
```json
{
  "request_id": "req_001",
  "user_id": "u_001",
  "team_id": "t_marketing",
  "service_id": "svc_doc_summary",
  "workflow_id": "wf_001",
  "model": "gpt-4o-mini",
  "input_size": 1200
}
```

### usage.tracked
```json
{
  "request_id": "req_001",
  "user_id": "u_001",
  "team_id": "t_marketing",
  "service_id": "svc_doc_summary",
  "workflow_id": "wf_001",
  "model": "gpt-4o-mini",
  "status": "SUCCESS",
  "total_tokens": 1220,
  "latency_ms": 1840
}
```

### policy.checked
```json
{
  "request_id": "req_001",
  "scope": "team",
  "scope_id": "t_marketing",
  "result": "ALLOWED",
  "rules_applied": ["TEAM_MODEL_RULE", "MONTHLY_BUDGET_RULE"]
}
```

### limit.applied
```json
{
  "request_id": "req_001",
  "scope": "user",
  "scope_id": "u_001",
  "result": "PASSED",
  "remaining_quota": 97
}
```

### cost.calculated
```json
{
  "request_id": "req_001",
  "user_id": "u_001",
  "team_id": "t_marketing",
  "service_id": "svc_doc_summary",
  "workflow_id": "wf_001",
  "model": "gpt-4o-mini",
  "currency": "KRW",
  "cost": 184.23
}
```

### optimization.recommended
```json
{
  "request_id": "req_001",
  "user_id": "u_001",
  "service_id": "svc_doc_summary",
  "current_model": "gpt-4o-mini",
  "recommended_model": "gpt-4.1-mini",
  "reason": "lower_cost_similar_task"
}
```

## 5. 결정 필요 항목

- 이벤트 보관 기간
- exactly-once 필요 여부
- 재처리 전략
- DLQ 운영 여부
