# Gateway + Control Spec (Owner B)

## 1. 목표

모든 실행 요청을 한 곳에서 받아 정책 검사, 제한 처리, 추적, 라우팅을 수행한다. 정식 인증/인가는 MVP 범위에서 제외하고 추후 Gateway의 고도화 항목으로 남긴다.

## 2. 구성 요소

| 모듈 | 역할 |
|------|------|
| Gateway | 단일 진입점, 라우팅 |
| Policy Service Client | 정책 서비스 호출 |
| Rate Limit Service Client | 제한 서비스 호출 |
| Request Logger | request_id 생성 및 기본 로그 |
| Event Publisher | ai.requested 발행, 제어 결과 전파 |

## 3. 핵심 흐름

1. 요청 수신
2. request_id 생성
3. 사용자/팀 식별 값 수신 또는 더미 컨텍스트 적용
4. Policy 검사
5. Rate Limit 검사
6. 외부 AI 또는 하위 실행 엔드포인트 호출
7. 성공/실패 결과 기록
8. `ai.requested` 발행 및 하위 이벤트 체인 시작

## 4. 정책 예시

| 정책명 | 설명 |
|--------|------|
| TEAM_MODEL_DENY | 특정 팀은 특정 모델 금지 |
| USER_DAILY_LIMIT | 사용자 일일 요청 제한 |
| TEAM_MONTHLY_BUDGET | 팀 월 예산 초과 시 차단 |
| SERVICE_ALLOWLIST | 허용 서비스만 사용 가능 |

## 5. 응답 코드 가이드

| 상황 | HTTP | error.code |
|------|------|------------|
| 정책 위반 | 403 | POLICY_BLOCKED |
| 요청 초과 | 429 | RATE_LIMIT_EXCEEDED |
| 외부 AI 실패 | 502 | AI_PROVIDER_ERROR |
| 잘못된 입력 | 400 | INVALID_REQUEST |

## 6. 기술 포인트

- Redis로 rate limit counter 관리
- request_id 헤더 주입
- 비동기 이벤트 발행
- 실패 시에도 로그/이벤트 남기기
- MVP는 인증 미구현을 전제로 하되, 추후 인증 필터가 들어갈 위치는 분리해 둔다

## 7. B의 선행 작업

1. request_id 정책 확정
2. 공통 에러 포맷 확정
3. Policy 평가 인터페이스 확정
4. Event 스키마 확정
5. mock external AI 구성
