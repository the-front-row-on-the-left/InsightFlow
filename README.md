# InsightFlow

문서 패키지를 기반으로 만든 최소 개발 스캐폴드입니다.

구성

- `apps/ai-platform-ui`: Vue 3 + Vite 기본 UI
- `shared/common-web`: 백엔드 공통 응답 포맷, `request_id` 처리, 이벤트 envelope
- `services/gateway-service`: 실행 진입점용 Gateway 골격
- `services/ai-ops-core-service`: 실행 중재/오케스트레이션 전용 서비스
- `services/policy-service`: 정책 평가 전용 서비스 골격
- `services/rate-limit-service`: 요청 제한 검사 전용 서비스 골격
- `services/usage-service`: Usage 조회 골격
- `services/billing-service`: Billing 조회 골격
- `services/notification-service`: 내부 이벤트 소비용 Notification 골격
- `services/recommendation-service`: Recommendation 조회 골격

현재 범위

- Gateway, AI Ops Core, Policy, Rate Limit은 문서 기준 MVP 흐름이 동작합니다.
- `POST /api/executions`는 Gateway를 통과한 뒤 AI Ops Core에서 Policy 검사, Rate Limit 검사, mock provider 실행을 거칩니다.
- Rate Limit은 기본적으로 Redis 카운터를 사용합니다.
- `ai.requested`, `policy.checked`, `limit.applied`는 Kafka 토픽 발행을 기준으로 구성돼 있습니다.
- UI는 문서 기준 서비스 목록과 다음 작업 안내만 보여줍니다.

실행 기준

- Backend: Java 21, Gradle
- Frontend: Node.js 20+

공통으로 먼저 맞춰둔 것

- 루트 코드 스타일 규칙: `.editorconfig`
- 로컬 인프라 템플릿: `compose.yaml`
- 공통 환경 변수 예시: `.env.example`
- 백엔드 공통 모듈: `shared/common-web`
- 프론트 공통 env/API 진입점: `apps/ai-platform-ui/.env.example`, `apps/ai-platform-ui/src/lib/api.ts`

로컬 시작 순서

1. `.env.example`를 참고해 환경값 준비
2. `docker compose up -d`
3. `docker compose up -d --build`
4. 필요하면 헬스체크와 API 호출로 확인
5. 프론트에서 `npm install && npm run dev`

서비스를 컨테이너로 함께 실행하려면

- 인프라 + B 서비스까지 한 번에: `docker compose up -d --build`
- 헬스 확인
  - `curl http://localhost:8080/health`
  - `curl http://localhost:8087/health`
  - `curl http://localhost:8081/health`
  - `curl http://localhost:8082/health`

팀 역할 기준

- A: `apps/ai-platform-ui`
- B: `services/gateway-service`, `services/ai-ops-core-service`, `services/policy-service`, `services/rate-limit-service`
- C: `services/usage-service`, `services/billing-service`, `services/notification-service`, `services/recommendation-service`

공통 계약 출처

- API: `docs/05_API_Contract.md`
- Event: `docs/06_Event_Contract.md`
- Data Model: `docs/07_Data_Model.md`
- Start Guide: `docs/21_Common_Development_Start_Guide.md`

예상 다음 작업

1. AI Ops Core와 Analytics 간 이벤트 체인 보강
2. Usage/Billing/Notification/Recommendation 이벤트 소비 스텁 확장
3. UI를 Gateway 실 API와 연결
