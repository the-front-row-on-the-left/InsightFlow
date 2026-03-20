# Environment & Runbook

## 1. 로컬 개발 구성

```text
[Platform UI] ----> [Gateway]
                      |-- [Policy Service]
                      |-- [Rate Limit Service]
                      |-- [Usage]
                      |-- [Billing]
                      |-- [Notification]
                      |-- [Recommendation]
                      |-- [External AI Mock]
```

## 2. 공통 개발환경

| 항목 | 권장 |
|------|------|
| Backend | Spring Boot 3.4.x |
| Java | 21 |
| Frontend | Vue 3 + Vite |
| DB | PostgreSQL |
| Cache | Redis |
| Event Bus | Kafka |
| API Spec | OpenAPI/Swagger |

## 3. 로컬 우선순위

### 최소 실행 세트
- Platform UI
- Gateway
- Policy Service
- Rate Limit Service
- Usage
- Billing
- Notification
- Recommendation
- PostgreSQL
- Redis
- Kafka

### Mock 가능 대상
- Recommendation
- External AI API
- Catalog Admin

## 4. 환경 변수 예시

### Platform
```env
VITE_API_BASE_URL=http://localhost:8080
```

### Gateway
```env
REDIS_HOST=localhost
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
POLICY_SERVICE_BASE_URL=http://localhost:8081
RATE_LIMIT_SERVICE_BASE_URL=http://localhost:8082
```

### Policy
```env
POLICY_PORT=8081
POLICY_CHECKED_TOPIC=policy.checked
```

### Rate Limit
```env
RATE_LIMIT_PORT=8082
REDIS_HOST=localhost
LIMIT_APPLIED_TOPIC=limit.applied
```

### Usage/Billing/Recommendation
```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=insightflow
USAGE_TRACKED_TOPIC=usage.tracked
```

## 5. 기본 실행 순서

1. PostgreSQL
2. Redis
3. Kafka
4. Gateway
5. Policy
6. Rate Limit
7. Usage
8. Billing
9. Notification
10. Recommendation
11. Platform UI

## 6. 점검 체크리스트

- Gateway health check 통과
- request_id 로그 출력
- policy evaluate 응답 정상
- rate limit check 응답 정상
- usage event 수신 정상
- billing cost 계산 정상
- notification 이벤트 소비 준비 정상
- UI에서 최소 1회 실행 성공
