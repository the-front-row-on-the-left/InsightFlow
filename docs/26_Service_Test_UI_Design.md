# Service Test UI Design

**Date:** 2026-03-20

**Goal**

C 역할 서비스와 관련 API를 빠르게 수동 검증할 수 있는 독립 테스트 프론트를 추가한다. 이 프론트는 운영용 제품 UI가 아니라 구현 확인과 로컬 디버깅을 위한 도구이며, 필요 시 폴더 단위로 쉽게 제거할 수 있어야 한다.

**Scope**

- 새 테스트 프론트 앱 `apps/service-test-ui` 추가
- `usage-service`, `billing-service`, `notification-service`, `recommendation-service` 호출 UI 제공
- 환경변수 기반 서비스 URL 설정
- 요청/응답 JSON 확인 기능 제공
- 루트 `.gitignore`를 현재 저장소 구조에 맞게 재정비

**Out of Scope**

- 운영용 인증/권한 처리
- 디자인 시스템 통합
- 복잡한 라우팅, 상태관리, 차트, 저장 기능
- 프론트에서 서비스 계약을 추상화하는 별도 SDK 작성

## Approach Options

### Option 1: 정적 HTML 테스트 페이지

가장 가볍고 제거가 쉽지만, 현재 저장소의 프론트 개발 흐름과 분리된다. 입력 상태, JSON 출력, 환경변수 주입을 직접 다뤄야 해서 오히려 유지보수성이 떨어질 수 있다.

### Option 2: 별도 Vue/Vite 테스트 앱

기존 프론트 스택과 같은 계열을 사용하되, 운영 프론트와는 완전히 분리된 새 앱을 만든다. 빠르게 구현할 수 있고 추후 삭제도 폴더 단위로 단순하다.

### Option 3: Swagger/Redoc만 사용

구현량은 적지만, 여러 서비스를 한 화면에서 빠르게 눌러보는 목적에는 덜 맞는다. 샘플 입력과 응답 비교를 반복하기도 불편하다.

**Recommendation**

Option 2를 사용한다. 저장소의 기존 프론트 경험을 재사용할 수 있고, 테스트 전용 앱이라는 분리 목표도 지킬 수 있다.

## Architecture

`apps/service-test-ui`는 단일 페이지 앱으로 구현한다. 앱은 서비스별 테스트 패널 목록과 공통 API 호출 유틸만 가진다. 각 패널은 하나의 서비스 또는 엔드포인트 그룹에 집중하며, 요청 폼 상태와 최근 응답 상태를 로컬 컴포넌트 상태로 관리한다.

서비스 URL은 `VITE_*` 환경변수로 주입한다. 브라우저가 직접 각 백엔드 서비스를 호출하므로, 실행 환경에 따라 localhost 또는 compose 네트워크에 맞춰 손쉽게 바꿀 수 있어야 한다.

## Components

### 1. App Shell

단일 페이지 레이아웃이다. 앱 목적, 환경변수 기준 URL 정보, 서비스 테스트 패널 목록을 보여준다.

### 2. Service Test Panel

서비스별 카드형 섹션이다. 다음 책임을 가진다.

- 엔드포인트 설명 표시
- 샘플 파라미터 프리셋 제공
- 입력값 수정 UI 제공
- 요청 전송
- 로딩/성공/실패 상태 표시
- 응답 JSON 출력

패널은 범용 컴포넌트로 만들고, 서비스별 설정 객체를 주입받아 재사용한다.

패널 설정 객체는 아래 필드를 가진다.

- `serviceKey`: `usage | billing | notification | recommendation`
- `title`: 카드 제목
- `description`: 테스트 목적 설명
- `basePath`: 프록시 기준 접두사. 예: `/proxy/usage`
- `endpoints`: 엔드포인트 정의 목록

엔드포인트 정의는 아래 필드를 가진다.

- `id`: 화면 내부 식별자
- `label`: 버튼/선택 UI 표시 이름
- `method`: HTTP 메서드. 이번 범위는 모두 `GET`
- `pathTemplate`: path parameter 포함 경로 템플릿. 예: `/api/usage/users/:userId`
- `pathParams`: path parameter 메타데이터 목록
- `queryParams`: query parameter 메타데이터 목록
- `samplePresets`: 샘플 입력 세트 목록
- `notes`: 응답 확인 포인트

즉, 패널은 서비스 단위이고 실제 테스트 가능한 요청은 엔드포인트 정의 단위로 분리한다.

### 3. API Client Utility

브라우저 `fetch`를 감싼 얇은 유틸이다. 성공 응답과 실패 응답을 공통 형식으로 변환하고, 네트워크 오류 메시지를 사용자에게 보여주기 쉬운 형태로 만든다.

### 4. Service Definitions

테스트 가능한 서비스 목록, 기본 엔드포인트, 샘플 파라미터, HTTP 메서드, 설명을 선언형 데이터로 정의한다. UI가 코드 분기보다 설정 위주로 움직이게 해서 삭제와 확장을 단순하게 만든다.

## Data Flow

1. 사용자가 서비스 패널에서 프리셋을 선택하거나 입력값을 수정한다.
2. 패널이 서비스 설정을 기반으로 path parameter를 `pathTemplate`에 채우고, query parameter를 이어서 최종 요청 경로를 만든다.
3. 브라우저는 직접 백엔드 서비스 URL을 호출하지 않고, 같은 오리진의 Vite dev proxy 경로(`/proxy/<serviceKey>`)를 호출한다.
4. API client utility가 해당 프록시 경로로 요청을 전송한다.
5. 응답이 성공이면 JSON 본문과 상태 코드를 패널에 표시한다.
6. 실패면 HTTP 상태, 에러 메시지, 응답 본문을 그대로 노출한다.

이번 범위에서 “request JSON 확인”은 요청 바디 편집기가 아니라, 사용자가 실제로 전송한 최종 URL, path parameter, query parameter를 화면에서 바로 보는 것을 의미한다. 현재 대상 엔드포인트가 모두 `GET`이므로 별도 body editor는 두지 않는다.

## Integration Strategy

브라우저-백엔드 연동은 `Vite dev proxy`를 표준 경로로 사용한다. 이 테스트 프론트의 1차 목적은 로컬 개발 검증이므로, 브라우저가 `localhost`의 다른 포트에 뜬 서비스들을 직접 호출하며 CORS를 맞추는 대신, 프론트 dev server가 프록시 역할을 맡는다.

- 프론트 런타임 요청 경로: `/proxy/usage`, `/proxy/billing`, `/proxy/notification`, `/proxy/recommendation`
- 실제 target URL: 환경변수로 주입된 각 서비스 base URL
- 장점: 서비스 쪽 CORS 설정을 추가로 건드리지 않아도 된다.
- 비범위: 정적 호스팅된 운영 배포

즉, 이 앱은 “로컬 또는 개발 환경에서 dev server로 띄워 쓰는 검증 도구”로 정의한다.

## Environment Contract

환경변수는 모두 선택적이지만, 비어 있는 서비스는 UI에서 “미설정” 상태로 비활성 표시한다.

- `VITE_USAGE_SERVICE_URL`
  - 기본값: `http://localhost:8083`
- `VITE_BILLING_SERVICE_URL`
  - 기본값: `http://localhost:8084`
- `VITE_NOTIFICATION_SERVICE_URL`
  - 기본값: `http://localhost:8085`
- `VITE_RECOMMENDATION_SERVICE_URL`
  - 기본값: `http://localhost:8086`

동작 규칙은 아래와 같다.

- 환경변수가 없으면 기본값을 사용한다.
- 값이 빈 문자열이면 해당 서비스 패널을 비활성화한다.
- 프록시 설정은 비활성 서비스에 대해 target을 만들지 않는다.
- 사용자는 화면 상단에서 현재 연결 대상 URL을 바로 확인할 수 있다.

## Endpoint Matrix

테스트 프론트는 아래 엔드포인트만 지원한다.

### Usage Panel

- `GET /health`
  - 프리셋: 없음
  - 확인 포인트: `service=usage-service`, `status=UP`
- `GET /api/usage/users/:userId?from&to&unit`
  - 샘플 프리셋: `user-001`, `2026-03-01`, `2026-03-20`, `day`
  - 확인 포인트: 사용자 단위 usage summary와 records
- `GET /api/usage/teams/:teamId?from&to&unit`
  - 샘플 프리셋: `team-analytics`, `2026-03-01`, `2026-03-20`, `day`
  - 확인 포인트: 팀 단위 usage summary와 records
- `GET /api/usage/services/:serviceId?from&to&unit`
  - 샘플 프리셋: `recommendation-service`, `2026-03-01`, `2026-03-20`, `day`
  - 확인 포인트: 서비스 단위 usage summary와 records

### Billing Panel

- `GET /health`
  - 프리셋: 없음
  - 확인 포인트: `service=billing-service`, `status=UP`
- `GET /api/billing/users/:userId`
  - 샘플 프리셋: `user-001`
  - 확인 포인트: 사용자 비용 summary와 breakdown
- `GET /api/billing/teams/:teamId`
  - 샘플 프리셋: `team-analytics`
  - 확인 포인트: 팀 비용 summary와 breakdown
- `GET /api/billing/workflows/:workflowId`
  - 샘플 프리셋: `workflow-recommendation-daily`
  - 확인 포인트: 워크플로우 비용 summary와 breakdown
- `GET /api/billing/pricing-tables/:version`
  - 샘플 프리셋: `2026-03`
  - 확인 포인트: 버전별 가격표 항목

### Notification Panel

- `GET /health`
  - 프리셋: 없음
  - 확인 포인트: `service=notification-service`, `status=UP`
- `GET /internal/notifications/subscriptions?user_id&team_id`
  - 샘플 프리셋: `user_id=user-001`
  - 확인 포인트: 구독 채널과 정책 목록
- `GET /internal/notifications?user_id&team_id`
  - 샘플 프리셋: `user_id=user-001`
  - 확인 포인트: 최근 내부 알림 projection 목록

### Recommendation Panel

- `GET /health`
  - 프리셋: 없음
  - 확인 포인트: `service=recommendation-service`, `status=UP`
- `GET /api/recommendations?user_id`
  - 샘플 프리셋: `user_id=user-001`
  - 확인 포인트: 추천 목록, 절감 토큰, 절감 비용, 사유

## Error Handling

- 환경변수 URL이 비어 있으면 패널에서 즉시 경고를 보여준다.
- 네트워크 오류와 HTTP 오류를 구분해서 표시한다.
- JSON 파싱에 실패하면 원문 텍스트를 그대로 보여준다.
- 한 패널의 실패가 다른 패널 사용에 영향을 주지 않도록 상태를 분리한다.
- path parameter가 비어 있으면 요청을 막고 필수 입력 오류를 보여준다.
- query parameter가 비어 있으면 선택값으로 간주하고 URL에 포함하지 않는다.

## Testing Strategy

### Frontend

- 서비스 패널 렌더링 테스트 1개 이상
- 프리셋 적용 및 요청 상태 전환 테스트 1개 이상
- API 유틸의 성공/실패 처리 테스트 1개 이상
- path parameter와 query parameter 조합이 정확히 만들어지는지 테스트 1개 이상
- 비활성 서비스가 호출되지 않는지 테스트 1개 이상

### Build Verification

- `apps/service-test-ui` 단독 빌드 확인
- 기존 `apps/ai-platform-ui`가 영향받지 않았는지 최소 확인

## .gitignore Plan

루트 `.gitignore`는 현재 저장소에서 실제로 생성되는 Node, Gradle, Java, Python, IDE, OS, env 파일을 반영하는 형태로 재작성한다. 이번 범위는 테스트 프론트와 FastAPI recommendation service, 그리고 기존 Gradle 서비스에서 관찰 가능한 산출물 보강에 한정한다.

- `node_modules`, `dist`, `.vite`
- `.gradle`, `build`, `target`, `out`
- `__pycache__`, `.pytest_cache`, `.venv`, `.mypy_cache`
- `.env`, `.env.*`
- IDE/OS 산출물과 로그 파일

단, `.env.example`처럼 공유가 필요한 예시 파일은 무시 대상에 넣지 않는다.

## Success Criteria

- `apps/service-test-ui`를 단독으로 실행할 수 있다.
- 4개 C 역할 서비스에 대해 샘플 요청을 브라우저에서 보낼 수 있다.
- 응답과 에러를 눈으로 바로 확인할 수 있다.
- 새 앱은 기존 운영 프론트와 분리되어 있어 제거가 쉽다.
- `.gitignore`가 현재 저장소의 주요 생성물을 빠짐없이 커버한다.
