# Development Guide For C Analytics

## 1. 역할 정의

C의 역할은 실행 데이터를 운영 가능한 정보로 바꾸는 것이다.

핵심 책임

- Usage 적재와 집계
- Billing 계산과 조회
- Notification 이벤트 소비 구조
- Recommendation 생성과 조회

세부 기준은 [15_Analytics_Spec.md](/Users/skax/Downloads/ai_ops_docs_bundle/15_Analytics_Spec.md)와 [20_Pricing_and_Cost_Policy.md](/Users/skax/Downloads/ai_ops_docs_bundle/20_Pricing_and_Cost_Policy.md)를 우선 따른다.

## 2. C의 목표

C의 목표는 "분석 시스템을 크게 만드는 것"이 아니라 "실행 1건이 비용과 추천으로 이어지는 최소 데이터 흐름"을 만드는 것이다.

MVP 핵심은 아래와 같다.

1. 이벤트 수신
2. request 단위 usage 저장
3. 비용 계산
4. 집계 조회
5. 추천 1종 생성

## 3. C가 가장 먼저 해야 할 일

1. 가격표 구조 확정
2. `usage.tracked` 필수 필드 확인
3. Usage/Billing 테이블 골격 생성
4. 이벤트 consumer 골격 생성
5. 추천 규칙 1개 고정

초반에는 조회 API보다 적재와 계산의 정확한 골격이 더 중요하다.

## 4. 권장 구현 순서

### 1단계

- Usage 로그 적재
- `ai.requested`, `policy.checked`, `limit.applied` 이벤트 수신
- `usage.tracked` 이벤트 발행
- `request_id` 기준 상태 결합

### 2단계

- Billing 가격표 로딩
- request 단위 비용 계산
- 비용 레코드 저장

### 3단계

- 사용자/팀 기준 집계 API
- 서비스/워크플로우 기준 집계 API

### 4단계

- Recommendation 규칙 1개 구현
- 추천 저장/조회

### 5단계

- 지연, 중복, 가격표 누락 대응 강화

## 5. Usage 구현 가이드

Usage는 전체 데이터 흐름의 기초다.

반드시 저장할 핵심 필드

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

주의사항

- `ai.requested`만 받고 끝내지 않는다.
- Policy/Rate Limit 결과와 합쳐 `usage.tracked`를 만들 수 있어야 한다.
- request 단위 상태가 여러 이벤트로 나뉘므로 merge 전략이 필요하다.

## 6. Billing 구현 가이드

Billing은 C의 가장 중요한 신뢰 지점이다.

원칙

- 계산 기준은 가격표 단일 소스
- Gateway 추정값이 있어도 최종 비용은 Billing이 계산
- 반올림 규칙과 버전 정보를 함께 남김

반드시 저장할 추가 필드

- `price_table_version`
- `billable`
- `cost_before_rounding`
- `currency`

실수하기 쉬운 지점

- prompt/output 단가를 뒤바꾸는 것
- request 실패를 무조건 0원 처리하는 것
- 집계 전에 소수점 반올림을 해버리는 것

## 7. Recommendation 구현 가이드

Recommendation은 처음부터 똑똑할 필요가 없다.

추천 1순위 규칙 예시

- 동일 서비스의 최근 7일 평균 비용이 높다.
- 더 저렴한 대체 모델이 존재한다.
- 정책상 허용 가능하다.

이 규칙 하나만 있어도 PoC는 충분하다.

중요한 점

- 추천 품질보다 추천 생성 가능성과 설명 가능성이 더 중요하다.
- 빈 추천 결과도 정상 케이스다.

## 8. C가 B와 맞춰야 하는 것

- 이벤트 필드 목록
- 이벤트 발행 시점
- 실패 요청의 status 의미
- 더미 사용자/팀 컨텍스트 값
- 토큰/지연 필드 제공 여부

특히 비용 계산은 `model`, `prompt_tokens`, `completion_tokens` 없이는 정확해지기 어렵다.

## 9. C가 A와 맞춰야 하는 것

- 비용 조회 응답 구조
- 추천 조회 응답 구조
- 화면에 필요한 집계 단위
- 비용 계산 지연 상태 표현

주의사항

- A가 바로 쓰기 쉬운 응답을 주되, 집계 정의를 애매하게 만들지 않는다.
- `일별`, `팀별`, `서비스별`이 섞인 응답은 피한다.

## 10. 이벤트 소비 설계 가이드

초기에는 단순하고 안전한 쪽이 낫다.

권장 원칙

- at-least-once 전제
- idempotency 고려
- `request_id` 기준 upsert 또는 중복 방지
- 실패 시 재처리 가능 구조

체크 포인트

- 같은 이벤트를 두 번 받아도 비용이 두 번 계산되지 않는가
- `ai.requested`는 왔지만 `policy.checked` 또는 `limit.applied`가 늦게 와도 괜찮은가
- 가격표가 없을 때 실패 기록이 남는가

## 11. 데이터 모델 가이드

초기에는 아래 정도만 확실히 해도 충분하다.

Usage

- 원본 이벤트 기반 로그 테이블
- 일별 요약 테이블

Billing

- request 단위 레코드
- 팀/사용자 단위 요약

Recommendation

- 추천 결과 테이블
- 추천 생성 근거 metadata

## 12. 조회 API 가이드

우선 필요한 조회 API

- `GET /api/usage/users/{user_id}`
- `GET /api/usage/teams/{team_id}`
- `GET /api/billing/teams/{team_id}`
- `GET /api/recommendations?user_id=...`

응답 설계 원칙

- 합계와 기간 범위를 같이 준다.
- 통화 정보를 같이 준다.
- 빈 데이터는 200 + 빈 배열/0값으로 처리한다.

## 13. 가격표 운영 가이드

가격표는 코드에 하드코딩하지 않는 것이 좋다.

초기 선택지

- DB 테이블
- 설정 파일

추천

- MVP는 설정 파일 또는 seed 테이블
- 단, 버전 필드는 반드시 유지

## 14. 로그와 메트릭

C가 우선 모니터링해야 할 것

- 이벤트 수신 수
- 소비 실패 수
- 중복 이벤트 수
- 가격표 누락 수
- 비용 계산 실패 수
- 집계 지연

로그에는 아래가 중요하다.

- `request_id`
- `event_type`
- `billing_status`
- `price_table_version`
- `recommendation_type`

## 15. 테스트 가이드

최소 테스트

- `usage.tracked` 발행 후 billing 계산
- 가격표 기반 비용 계산
- 가격표 누락 처리
- 동일 이벤트 중복 수신 처리
- 추천 1건 생성

권장 시나리오

1. 실행 완료 이벤트 수신
2. usage 적재
3. billing 계산
4. 집계 조회
5. 추천 생성

## 16. PR 기준

- 적재, 계산, 조회를 한 PR에 모두 넣지 않는다.
- 이벤트 스키마 가정이 있다면 PR 설명에 적는다.
- 샘플 이벤트 payload와 기대 저장 결과를 같이 제시한다.

좋은 PR 예시

- `feat(analytics): normalize request events into usage tracked payload`
- `feat(billing): calculate per-token cost using versioned price table`

## 17. C가 자주 막히는 지점과 해결법

문제

- B 이벤트 필드가 아직 고정되지 않음

해결

- 임시 mock event를 문서 기준으로 만들고 adapter로 받는다.

문제

- 가격표 기준이 계속 바뀜

해결

- 버전 관리와 seed 데이터를 먼저 둔다.

문제

- Recommendation이 과하게 커짐

해결

- 규칙 1개만 먼저 고정하고 설명 가능한 추천부터 만든다.

## 18. 완료 기준

- request 단위 usage 저장이 된다.
- request 단위 비용 계산이 된다.
- 팀/사용자 기준 조회가 가능하다.
- 추천 1종 이상이 조회된다.
- A가 비용/추천 화면을 붙일 수 있는 응답을 제공한다.
