# Observability & Alerting Guide

## 1. 목적

이 문서는 AI Ops Platform MVP의 로깅, 메트릭, 트레이싱, 대시보드, 알림 기준을 정의한다.

핵심 목적은 다음과 같다.

- 장애를 빨리 발견한다.
- 비용 급증이나 정책 오작동을 조기에 감지한다.
- 세 팀이 만든 서비스가 통합된 뒤에도 요청 단위로 추적 가능하게 한다.

## 2. 관측성 원칙

1. 모든 서비스 로그는 `request_id` 중심으로 상호 연결 가능해야 한다.
2. 운영 판단은 로그 한 종류에만 의존하지 않고 로그, 메트릭, 이벤트를 함께 본다.
3. 경고는 "많이 울리는 것"보다 "운영자가 실제로 대응할 만한 것"만 남긴다.
4. MVP에서는 과도한 분산 추적보다 구조화 로그와 핵심 메트릭을 먼저 안정화한다.
5. 운영 지표는 사용자 흐름 기준으로 설계한다.

## 3. 공통 텔레메트리 구조

## 3.1 공통 로그 필드

모든 서비스는 JSON 구조 로그를 사용한다.

필수 필드

- `timestamp`
- `level`
- `service`
- `environment`
- `request_id`
- `trace_id`
- `user_id`
- `team_id`
- `operation`
- `status`

권장 필드

- `service_id`
- `workflow_id`
- `execution_id`
- `model`
- `latency_ms`
- `error_code`
- `upstream`

예시

```json
{
  "timestamp": "2026-03-20T13:20:00Z",
  "level": "INFO",
  "service": "gateway-service",
  "environment": "dev",
  "request_id": "req_001",
  "trace_id": "tr_001",
  "user_id": "u_001",
  "team_id": "t_marketing",
  "operation": "execution.create",
  "service_id": "svc_doc_summary",
  "model": "gpt-4o-mini",
  "status": "SUCCESS",
  "latency_ms": 1840
}
```

## 3.2 트레이스 전파 기준

MVP에서 최소 전파 필드

- `request_id`
- `trace_id`
- `span_id` 선택

전파 경로

`Platform UI -> Gateway -> Policy/Rate Limit -> Usage/Billing/Notification/Recommendation -> External AI`

프론트에서도 네트워크 오류 리포팅 시 `request_id`를 사용자에게 보여줄 수 있어야 한다.

## 4. 서비스별 핵심 메트릭

## 4.1 Gateway

- 총 요청 수
- 성공률
- 4xx 비율
- 5xx 비율
- 정책 차단 수
- rate limit 차단 수
- 외부 AI 호출 지연시간
- 모델별 요청 수

## 4.2 Platform UI

- 카탈로그 조회 성공률
- 실행 요청 성공률
- 실행 결과 조회 지연
- 주요 화면 JS 에러 수
- 정책 차단 UX 노출 수

## 4.3 Usage

- 이벤트 소비량
- 적재 성공률
- 처리 지연
- 집계 배치 완료 시간
- 중복 이벤트 탐지 수

## 4.4 Billing

- 비용 계산 성공률
- 비용 계산 지연
- 가격표 누락 건수
- 통화/모델 매핑 오류 수

## 4.5 Recommendation

- 추천 생성 수
- 추천 생성 실패율
- 추천 조회 성공률
- 추천 데이터 신선도

## 5. 권장 SLI/SLO 초안

MVP는 정식 SRE 체계보다는 운영 기준선으로 사용한다.

| 영역 | SLI | 목표 |
|------|-----|------|
| Gateway 가용성 | 2xx/4xx 정상 응답 비율 | 99.5% 이상 |
| 실행 성공률 | 외부 AI 정상 완료 비율 | 95% 이상 |
| 실행 지연 | `POST /api/executions` p95 | 3초 이하 |
| 비용 적재 지연 | 실행 완료 후 비용 반영 시간 p95 | 5분 이하 |
| 추천 신선도 | 최신 집계 기준 추천 생성 | 1일 이내 |

주의사항

- 4xx는 사용자 입력이나 정책 차단일 수 있으므로 전부 장애로 보지 않는다.
- `POLICY_BLOCKED`, `RATE_LIMIT_EXCEEDED`는 운영 지표상 별도 분리한다.

## 6. 대시보드 구성

## 6.1 운영 공통 대시보드

- 분당 요청 수
- 성공/실패 비율
- 정책 차단 수
- rate limit 차단 수
- 외부 모델별 호출량
- 서비스별 평균 지연시간
- 최근 1시간 에러 코드 Top N

## 6.2 비용 운영 대시보드

- 일별 총 비용
- 팀별 비용 Top N
- 모델별 비용 분포
- 이상 급증 팀/사용자
- 비용 계산 실패 건수

## 6.3 데이터 파이프라인 대시보드

- Kafka lag
- Usage 소비 지연
- Billing 계산 지연
- 추천 생성 지연
- DLQ 적재 건수

## 7. 알림 정책

## 7.1 알림 등급

| 등급 | 의미 | 예시 |
|------|------|------|
| P1 | 즉시 대응 필요 | Gateway 5xx 급증, 전체 실행 실패 |
| P2 | 업무 영향 큼 | 비용 계산 적재 중단, Kafka lag 급증 |
| P3 | 추적 필요 | 추천 생성 실패 증가, 일부 화면 오류 |

## 7.2 즉시 알림 대상

- 5분 연속 Gateway 5xx 비율 5% 초과
- 외부 AI 호출 실패율 10% 초과
- Kafka consumer lag 임계치 초과
- Billing 계산 실패 연속 발생
- 인증 실패 급증

## 7.3 일일/정기 리포트 대상

- 팀별 비용 Top 10
- 정책 차단 상위 사용자
- 추천 생성률
- 가격표 누락/매핑 오류
- UI 오류 추이

## 8. 에러 코드와 관측성 연결

| 에러 코드 | 분류 | 알림 대상 여부 |
|-----------|------|---------------|
| `AUTH_TOKEN_INVALID` | 보안/인증 | 급증 시 알림 |
| `POLICY_BLOCKED` | 정상 제어 | 추세 관측 |
| `RATE_LIMIT_EXCEEDED` | 정상 제어 | 급증 시 알림 |
| `UPSTREAM_AI_TIMEOUT` | 외부 의존성 | 즉시 알림 |
| `BILLING_PRICE_NOT_FOUND` | 데이터 품질 | 즉시 알림 |
| `EVENT_CONSUME_FAILED` | 파이프라인 | 즉시 알림 |

## 9. 로그 레벨 기준

| 레벨 | 사용 기준 |
|------|-----------|
| `DEBUG` | 로컬 문제 분석, 운영 기본 비활성 |
| `INFO` | 정상 흐름, 상태 전환 |
| `WARN` | 재시도 가능 오류, 정책 차단, 부분 실패 |
| `ERROR` | 요청 실패, 의존성 장애, 데이터 적재 실패 |

주의사항

- 정책 차단은 비즈니스상 정상일 수 있으므로 남용해서 `ERROR`로 찍지 않는다.
- 사용자가 원인 제공한 4xx는 대부분 `WARN` 이하로 관리한다.

## 10. 샘플 알림 시나리오

## 10.1 Gateway 장애

조건

- 5분 평균 5xx 비율 5% 초과
- 분당 요청 수 100 이상

알림 메시지 예시

`[P1][gateway-service] 5xx ratio exceeded 5% for 5m in prod. Check upstream AI timeout and policy/rate-limit dependencies.`

## 10.2 비용 적재 지연

조건

- `usage.tracked` 이벤트는 유입되는데 `cost.calculated`가 10분 이상 지연

알림 메시지 예시

`[P2][billing-service] Cost calculation delay exceeded 10m. Billing dashboard may be stale.`

## 10.3 인증 실패 급증

조건

- 10분 내 `AUTH_TOKEN_INVALID` 또는 `AUTH_TOKEN_EXPIRED` 비율 급증

운영 해석

- 인증 설정 오류
- 토큰 발급 주체 변경
- 악의적 호출 가능성

## 11. 역할별 가이드

## 11.1 A

- 사용자 오류 화면에 `request_id`를 노출한다.
- 프론트 에러 로깅 시 API 에러 코드와 라우트 정보를 함께 남긴다.
- 화면 성능 지표와 API 실패 지표를 분리한다.

## 11.2 B

- Gateway access log와 application log를 구조화한다.
- Policy, RateLimit, Upstream AI 결과를 같은 `request_id`로 묶는다.
- 4xx와 5xx를 분리한 메트릭을 만든다.

## 11.3 C

- 이벤트 소비 성공/실패를 모두 계측한다.
- idempotency 충돌, 가격표 누락, 데이터 지연을 별도 메트릭으로 둔다.
- 추천 품질보다 먼저 추천 파이프라인 정상성을 계측한다.

## 12. 운영 체크리스트

개발 시작 전

- 공통 로그 필드 확정
- `request_id` 전파 방식 확정
- 대시보드 최소 항목 합의

통합 전

- Gateway, Usage, Billing 로그 상호 추적 가능 확인
- 정책 차단/RateLimit 차단이 에러 통계에 오염되지 않는지 확인
- 비용 지연 알림 테스트
- Kafka lag 시나리오 테스트
