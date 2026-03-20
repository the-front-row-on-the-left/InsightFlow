# Usage + Billing + Recommendation Spec (Owner C)

## 1. 목표

실행 로그를 분석 가능한 데이터로 만들고, 비용을 계산하며, 최적화/추천 기능을 제공한다.

## 2. 구성 요소

| 서비스 | 역할 |
|--------|------|
| Usage Service | 요청 로그 저장, 사용자/팀/서비스별 집계 |
| Billing Service | 가격표 기반 비용 계산 |
| Notification Service | 내부 이벤트 소비 기반 알림 확장 |
| Recommendation Service | 비용 최적화 / 서비스 추천 |

## 3. Usage 기능

- ai.requested / policy.checked / limit.applied 이벤트 수신
- usage.tracked 이벤트 발행
- request_id 기준 상태 결합
- 사용자/팀/서비스/워크플로우 기준 집계
- 일별/주별/월별 summary 생성

### 조회 API 예시
- `GET /api/usage/users/{user_id}`
- `GET /api/usage/teams/{team_id}`
- `GET /api/usage/services/{service_id}`

## 4. Billing 기능

- 모델별 가격표 관리
- 토큰 사용량 기반 비용 계산
- 요청 단위 비용 저장
- 팀/사용자/서비스/워크플로우 단위 합산

### 조회 API 예시
- `GET /api/billing/users/{user_id}`
- `GET /api/billing/teams/{team_id}`
- `GET /api/billing/workflows/{workflow_id}`

## 5. Recommendation 기능

### 1차 PoC 추천
- 같은 목적의 더 저렴한 모델 추천
- 사용 빈도가 높은 서비스 추천
- 자주 쓰는 조합을 workflow로 저장 추천

### 초기 규칙 예시
- 최근 7일 동일 서비스 고비용 요청이 N회 이상
- 저비용 대체 모델 존재
- 정책상 허용 모델인 경우 추천 생성

## 6. C의 선행 작업

1. 가격표 구조 합의
2. usage / billing 스키마 생성
3. usage.tracked 필수 필드 확정
4. 추천 규칙 1개 고정
5. 샘플 대시보드 응답 정의

## 7. 완료 조건

- request 단위 비용 계산 가능
- 사용자/팀 기준 집계 API 동작
- 추천 1종 이상 응답 가능
- 샘플 대시보드용 데이터 제공 가능
