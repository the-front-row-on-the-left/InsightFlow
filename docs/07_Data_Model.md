# Data Model (Draft)

## 1. 핵심 엔티티

### users
| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | string | 사용자 식별자 |
| team_id | string | 소속 팀 |
| role | string | 역할 |
| status | string | 활성/비활성 |

### service_catalog
| 컬럼 | 타입 | 설명 |
|------|------|------|
| service_id | string | 서비스 ID |
| name | string | 서비스명 |
| category | string | chatbot/report/analysis/rag |
| owner_type | string | internal/partner |
| pricing_model | string | per_token/per_request/fixed |
| status | string | published/draft |

### workflows
| 컬럼 | 타입 | 설명 |
|------|------|------|
| workflow_id | string | 워크플로우 ID |
| name | string | 이름 |
| created_by | string | 생성자 |
| team_id | string | 팀 ID |
| steps_json | json | 단계 정의 |

### executions
| 컬럼 | 타입 | 설명 |
|------|------|------|
| execution_id | string | 실행 ID |
| request_id | string | 요청 ID |
| service_id | string | 서비스 ID |
| workflow_id | string | 워크플로우 ID |
| status | string | PENDING/SUCCESS/FAILED |
| started_at | timestamp | 시작 시각 |
| finished_at | timestamp | 완료 시각 |

### usage_logs
| 컬럼 | 타입 | 설명 |
|------|------|------|
| usage_id | string | 사용량 로그 ID |
| request_id | string | 요청 ID |
| user_id | string | 사용자 ID |
| team_id | string | 팀 ID |
| service_id | string | 서비스 ID |
| workflow_id | string | 워크플로우 ID |
| model | string | 모델명 |
| prompt_tokens | int | 입력 토큰 |
| completion_tokens | int | 출력 토큰 |
| total_tokens | int | 총 토큰 |
| latency_ms | int | 지연 |
| status | string | SUCCESS/FAILED |
| created_at | timestamp | 생성 시각 |

### billing_records
| 컬럼 | 타입 | 설명 |
|------|------|------|
| billing_id | string | 비용 레코드 ID |
| request_id | string | 요청 ID |
| user_id | string | 사용자 ID |
| team_id | string | 팀 ID |
| service_id | string | 서비스 ID |
| workflow_id | string | 워크플로우 ID |
| model | string | 모델명 |
| currency | string | KRW/USD |
| cost | decimal | 계산 비용 |
| created_at | timestamp | 생성 시각 |

### policies
| 컬럼 | 타입 | 설명 |
|------|------|------|
| policy_id | string | 정책 ID |
| scope_type | string | org/team/role/user |
| scope_id | string | 범위 식별자 |
| allowed_models_json | json | 허용 모델 |
| allowed_services_json | json | 허용 서비스 |
| daily_request_limit | int | 일간 요청 수 제한 |
| monthly_budget_limit | decimal | 월 예산 제한 |
| status | string | 활성 여부 |

### recommendations
| 컬럼 | 타입 | 설명 |
|------|------|------|
| recommendation_id | string | 추천 ID |
| target_type | string | user/team/service/workflow |
| target_id | string | 대상 식별자 |
| recommendation_type | string | cost_optimization/service_suggestion |
| message | string | 추천 내용 |
| metadata_json | json | 세부 정보 |
| created_at | timestamp | 생성 시각 |

## 2. 저장소 제안

| 데이터 | 저장소 |
|--------|--------|
| users/service_catalog/workflows/policies | PostgreSQL |
| usage_logs/billing_records 대량 로그 | PostgreSQL 시작 → 추후 ClickHouse |
| rate limit counter | Redis |

## 3. ID 규칙

- user: `u_{uuid}`
- team: `t_{slug}`
- service: `svc_{slug}`
- workflow: `wf_{uuid}`
- request: `req_{uuid}`
- execution: `exe_{uuid}`
