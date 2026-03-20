# InsightFlow

문서 검색 기반 AI 플랫폼과 운영용 AIOps 대시보드를 함께 실험하는 MSA 예제입니다.

## 구성

- `apps/ai-platform-ui`: 사용자용 Vue 3 + Vite 프론트
- `apps/ai-ops-dashboard-ui`: 운영용 Vue 3 + Vite 대시보드
- `shared/common-web`: 공통 응답 포맷, `request_id`, CORS, Kafka 이벤트 공통 모듈
- `services/gateway-service`: 사용자 요청 진입점
- `services/ai-ops-core-service`: 정책 검사, rate limit 검사, AI 실행 오케스트레이션
- `services/policy-service`: 정책 평가
- `services/rate-limit-service`: Redis 기반 요청 제한 검사
- `services/usage-service`: 사용량 집계/조회
- `services/billing-service`: 가격표 기반 비용 계산/조회
- `services/notification-service`: 운영 알림 조회
- `services/recommendation-service`: FastAPI 기반 절감 추천 조회

## 현재 동작 범위

- 사용자 요청은 `ai-platform-ui -> gateway-service -> ai-ops-core-service` 경로로 실행됩니다.
- `svc_doc_search` 는 실제 OpenAI 호출을 사용할 수 있습니다.
  - `OPENAI_API_KEY` 가 설정되면 OpenAI `/responses` 호출
  - 키가 없으면 로컬 문서 코퍼스 기반 fallback
- 문서 검색 기본 모델은 `gpt-4.1-mini` 입니다.
- `usage-service` 와 `billing-service` 는 `ai.requested`, `ai.completed`, `usage.tracked` 이벤트를 기준으로 동작합니다.
- 사용량은 OpenAI 응답의 실제 `usage` 필드를 우선 사용합니다.
  - `input_tokens`, `output_tokens`, `total_tokens`
  - usage 정보가 없을 때만 내부 추정값 fallback
- 비용은 실제 OpenAI 청구 금액이 아니라 내부 가격표 기준으로 계산합니다.
- `ai-ops-dashboard-ui` 는 이제 mock 데이터 없이 실서비스 API만 조회합니다.

## 주요 포트

- `5173`: 사용자용 UI
- `5174`: AIOps 대시보드
- `8080`: Gateway
- `8081`: Policy
- `8082`: Rate Limit
- `8083`: Usage
- `8084`: Billing
- `8085`: Notification
- `8086`: Recommendation
- `8087`: AI Ops Core
- `5432`: Postgres
- `6379`: Redis
- `9092`: Kafka

## 빠른 시작

1. `.env.example` 를 참고해서 `.env` 를 준비합니다.
2. 실제 OpenAI 호출을 쓰려면 `.env` 에 `OPENAI_API_KEY` 를 넣습니다.
3. 전체 스택 실행:

```bash
docker compose up -d --build
```

4. 접속:
   - 사용자 UI: `http://localhost:5173`
   - AIOps 대시보드: `http://localhost:5174`

중지:

```bash
docker compose down
```

## 환경 변수 핵심

- `OPENAI_ENABLED=true`
- `OPENAI_API_KEY=...`
- `OPENAI_MODEL=gpt-4.1-mini`
- `USER_DAILY_LIMIT=100`
- `TEAM_DAILY_LIMIT=300`

현재 rate limit 기본값은 개발용으로 완화되어 있습니다.

## 기본 확인

헬스 체크:

```bash
curl http://localhost:8080/health
curl http://localhost:8087/health
curl http://localhost:8083/health
curl http://localhost:8084/health
curl http://localhost:8085/health
```

문서 검색 실행:

```bash
curl -X POST http://localhost:8080/api/executions \
  -H 'Content-Type: application/json' \
  -d '{
    "service_id": "svc_doc_search",
    "model": "gpt-4.1-mini",
    "input": {
      "query": "billing settlement policy",
      "document_scope": "billing"
    }
  }'
```

실행 이력:

```bash
curl http://localhost:8080/api/executions
```

사용량/비용 조회:

```bash
curl http://localhost:8083/api/usage/users/u_demo_001
curl http://localhost:8084/api/billing/users/u_demo_001
curl "http://localhost:8086/api/recommendations?user_id=u_demo_001"
curl "http://localhost:8085/internal/notifications?user_id=u_demo_001&team_id=t_demo"
```

## 데이터 저장 방식

- `usage-service`, `billing-service`, `notification-service` 는 Postgres 사용
- `rate-limit-service` 는 Redis 사용
- `recommendation-service` 는 별도 DB 없이 `usage/billing` 조회 기반
- 실행 이력은 현재 `gateway-service` 메모리에 저장되므로 컨테이너 재시작 시 사라집니다.

## 역할별 서비스

- A: `apps/ai-platform-ui`, `apps/ai-ops-dashboard-ui`
- B: `services/gateway-service`, `services/ai-ops-core-service`, `services/policy-service`, `services/rate-limit-service`
- C: `services/usage-service`, `services/billing-service`, `services/notification-service`, `services/recommendation-service`

## 참고 문서

- `docs/05_API_Contract.md`
- `docs/06_Event_Contract.md`
- `docs/07_Data_Model.md`
- `docs/21_Common_Development_Start_Guide.md`
