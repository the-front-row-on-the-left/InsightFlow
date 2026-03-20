# Service Boundary

## 1. 서비스 경계 정의

| 서비스 | 책임 | 소유 데이터 | 비고 |
|--------|------|-------------|------|
| AI Platform | 카탈로그 조회, Workflow 구성, 실행 UX, 결과 표시 | workflows, execution_view | 사용자 접점 |
| Gateway | 인증, 라우팅, 요청 추적, 외부 API 오케스트레이션 | request_log_minimal | 모든 요청 진입점 |
| Policy | 정책 CRUD, 정책 평가 | policies | 조직/팀/역할/사용자 단위 |
| Rate Limit | 요청 제한 검사, quota 판단 | rate_limit_counter_reference | Redis 기반 제어 |
| Usage | 사용량 기록, 집계 | usage_logs, usage_daily_summary | request 기준 |
| Billing | 비용 계산, 비용 집계 | billing_records, billing_summary | usage 기반 |
| Notification | 내부 알림 소비 확장 지점 | notification_delivery_reference | MVP는 소비 구조만 우선 |
| Recommendation | 추천 생성/조회 | recommendations | 비용/패턴 분석 기반 |

## 2. 경계 원칙

- Platform은 외부 AI API를 직접 호출하지 않는다.
- Policy는 비용 계산 로직을 가지지 않는다.
- Rate Limit은 정책 평가 로직을 가지지 않는다.
- Billing은 정책 평가를 하지 않는다.
- Notification은 원천 데이터를 소유하지 않고 이벤트를 소비한다.
- Recommendation은 원천 실행을 변경하지 않고 추천만 제공한다.
- Gateway는 분석 데이터를 최소한만 저장하고 상세 분석은 Usage로 위임한다.

## 3. 소유권이 애매한 항목

| 항목 | 소유 서비스 | 이유 |
|------|-------------|------|
| request_id 생성 | Gateway | 전 요청 공통 |
| workflow_id 보관 | Platform | 사용자 자산 |
| workflow 실행 비용 집계 | Billing | 비용 계산 책임 |
| 정책 위반 로그 | Gateway + Usage | 제어/분석 분리 |
| limit 적용 기록 | Rate Limit + Usage | 제어/분석 분리 |
| 알림 이력 저장 | Notification | 후속 확장 대비 |
| 추천 이력 저장 | Recommendation | 추천 품질 추적 |

## 4. MVP에서 단순화 가능한 세부 서비스

초기에는 다음을 별도 서비스로 두더라도 구현은 단순 스캐폴드 또는 placeholder 수준으로 둘 수 있다.

- External LLM Adapter
- Catalog Admin

## 5. 추후 분리 후보

- Catalog Service
- Execution Orchestrator
- Auth Service
