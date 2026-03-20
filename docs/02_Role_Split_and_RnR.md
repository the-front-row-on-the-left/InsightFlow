# Role Split & RnR

## 1. 병렬 개발 전제

세 명이 병렬 개발하되, **계약(API/Event/Data) 먼저 합의**하고 이후 각 트랙을 독립적으로 개발한다.

## 2. 역할 분담

| 담당 | 트랙 | 핵심 책임 | 주 산출물 |
|------|------|-----------|-----------|
| A | AI 플랫폼 (Front + Workflow) | 사용자 경험, 카탈로그, 실행 UI/UX, Workflow 저장/실행 | Front 화면, Workflow API 소비, 실행 결과 저장 |
| B | API Gateway + Policy + RateLimit | 단일 진입점, 요청 통제, 정책 평가, 제한 처리, 추후 인증 관문 준비 | Gateway Service, Policy Service, RateLimit Service |
| C | Usage + Billing + Recommendation + Notification | 로그 수집, 비용 계산, 통계/분석, 추천 로직, 내부 알림 소비 구조 | Usage Service, Billing Service, Recommendation Service, Notification Service |

## 3. 공통 책임

| 항목 | 공동 책임 |
|------|-----------|
| API 계약 | 모두 리뷰, B가 최종 게이트 |
| 이벤트 계약 | B, C 주도 / A 리뷰 |
| 데이터 모델 | C 주도 / B 검토 / A 소비 관점 검토 |
| 에러 규격 | B 주도 / 전체 합의 |
| 테스트 시나리오 | 전체 공동 작성 |

## 4. 인터페이스 의존성

### A가 필요한 것
- 서비스 카탈로그 조회 API
- Workflow 실행 API
- 실행 결과 조회 API
- 비용/사용량 조회 API
- 정책 위반 시 에러 포맷

### B가 필요한 것
- 서비스 식별자 규칙
- Workflow 실행 요청 규격
- Usage/Billing에 넘길 이벤트 스키마

### C가 필요한 것
- Gateway에서 내려주는 request_id, service_id, workflow_id
- 사용자/팀 식별자
- 모델/토큰/상태 코드 등 실행 메타데이터

## 5. 협업 규칙

- 계약 변경은 문서 먼저 수정 후 코드 반영
- 이벤트 이름은 임의 변경 금지
- 식별자 규칙(user_id, team_id, workflow_id, service_id)은 전역 고정
- 공통 응답 포맷 / 에러 포맷 통일
- 주 2회 통합 점검
  - 화: 계약 리뷰
  - 금: 통합 데모

## 6. Definition of Done

### 공통 DoD
- 문서 반영 완료
- 단위 테스트 통과
- Swagger/OpenAPI 최신화
- 샘플 요청/응답 존재
- 로컬 환경 통합 실행 가능

### 담당별 DoD
- A: 주요 화면 3개 이상 + 실행 흐름 동작
- B: 정책/제한 적용 및 로깅 확인
- C: 비용 집계/추천 API 응답 확인
