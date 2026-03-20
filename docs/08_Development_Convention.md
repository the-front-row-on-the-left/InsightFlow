# Development Convention

## 1. 브랜치 전략

- main: 안정 버전
- develop: 통합 브랜치
- feature/{domain}-{short-name}
- hotfix/{issue}

예시
- `feature/platform-workflow-create`
- `feature/gateway-policy-evaluator`
- `feature/analytics-cost-summary`

## 2. 커밋 규칙

- feat:
- fix:
- refactor:
- docs:
- test:
- chore:

예시
- `feat(gateway): add policy check before ai execution`

## 3. 패키지 / 모듈 명명

- 서비스명은 kebab-case
- API path는 plural 사용
- 이벤트명은 lower-dot-case
- DB 테이블은 snake_case

## 4. API 규약

- 공통 base path: `/api`
- 내부 호출: `/internal`
- 에러 코드는 대문자 스네이크 사용
- `request_id`를 헤더와 응답 meta에 포함

권장 헤더
- `X-Request-Id`
- `X-User-Id`
- `X-Team-Id`

## 5. 코드 리뷰 기준

- 계약 문서와 불일치 없는가
- 에러 처리 일관성 있는가
- 로그에 request_id가 남는가
- 하드코딩된 정책/가격표 없는가
- 테스트가 최소 1개 이상 포함되는가

## 6. 문서 관리 규칙

- 계약 변경 시 문서 먼저 수정
- 샘플 요청/응답 항상 최신 유지
- 임시 결정도 Decision Log에 기록
