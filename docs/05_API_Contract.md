# API Contract (MVP v0)

## 1. 공통 규칙

### Base Path
- Front → Gateway만 직접 호출
- Gateway 내부/하위 서비스 API는 `/internal/**` 사용 가능

### 공통 응답 예시
```json
{
  "success": true,
  "data": {},
  "meta": {
    "request_id": "req_123"
  }
}
```

### 공통 에러 예시
```json
{
  "success": false,
  "error": {
    "code": "POLICY_BLOCKED",
    "message": "This model is not allowed for your team."
  },
  "meta": {
    "request_id": "req_123"
  }
}
```

## 2. Platform 관련 API

### 서비스 카탈로그 조회
- `GET /api/catalog/services`
- query: `category`, `tag`, `keyword`

### 서비스 상세 조회
- `GET /api/catalog/services/{service_id}`

### Workflow 생성
- `POST /api/workflows`

요청 예시
```json
{
  "name": "문서 요약 후 보고서 생성",
  "steps": [
    {"service_id": "doc-summary"},
    {"service_id": "report-generator"}
  ]
}
```

### Workflow 조회
- `GET /api/workflows/{workflow_id}`

### AI 실행
- `POST /api/executions`

요청 예시
```json
{
  "service_id": "doc-summary",
  "workflow_id": "wf_001",
  "input": {
    "text": "..."
  },
  "model": "gpt-4o-mini"
}
```

### 실행 결과 조회
- `GET /api/executions/{execution_id}`

## 3. Policy / Control 관련 API

### 정책 목록 조회
- `GET /internal/policies`

### 정책 평가
- `POST /internal/policies/evaluate`

### Rate Limit 체크
- `POST /internal/rate-limit/check`

## 4. Analytics 관련 API

### 사용자 사용량 조회
- `GET /api/usage/users/{user_id}`

### 팀 비용 조회
- `GET /api/billing/teams/{team_id}`

### 추천 조회
- `GET /api/recommendations?user_id={user_id}`

## 5. 초기 합의 필요 항목

- `service_id` 네이밍 규칙
- `workflow_id` 생성 방식
- `input` 최대 크기
- 스트리밍 응답 지원 여부
- 폴리시 위반 시 차단 vs 대체 추천 동시 반환 여부
