# Development Guide For B Gateway

## 1. 역할 정의

B의 역할은 전체 시스템의 제어 평면을 만드는 것이다.

핵심 책임

- 단일 진입점 제공
- 정책 검사
- rate limit
- request_id 생성과 전파
- 공통 에러 포맷
- 이벤트 발행
- 외부 AI 연동 또는 mock 대체

세부 기준은 [14_Gateway_Control_Spec.md](/Users/skax/Downloads/ai_ops_docs_bundle/14_Gateway_Control_Spec.md)를 우선 따른다.

## 2. B의 목표

B의 목표는 "기능을 많이 넣는 것"이 아니라 "모든 흐름이 통과할 수 있는 좁고 안정적인 관문"을 만드는 것이다.

MVP에서 가장 중요한 것은 아래 5가지다.

1. `POST /api/executions` 진입점 안정화
2. `request_id` 생성
3. 정책 차단
4. rate limit 차단
5. `ai.requested` 발행과 Policy/Rate Limit 서비스 연동

## 3. B가 가장 먼저 해야 할 일

1. 공통 에러 포맷 고정
2. `request_id` 생성 정책 고정
3. 실행 API 입출력 고정
4. Policy service 인터페이스 고정
5. Event publisher 인터페이스 고정

이 다섯 가지가 흔들리면 A와 C 모두 막힌다.

## 4. 권장 구현 순서

### 1단계

- health check
- 기본 라우팅
- 공통 응답/에러 구조
- `request_id` 생성

### 2단계

- `POST /api/executions`
- mock external AI 연결
- 성공/실패 결과 변환

### 3단계

- Policy 검사
- Rate limit 검사
- 표준 에러 코드 반환

### 4단계

- `ai.requested`
- `policy.checked`
- `limit.applied`
- 실패 시 이벤트/로그 처리

### 5단계

- 내부 조회/운영 보조 엔드포인트
- 추후 인증 필터 삽입 지점 정리

## 5. request_id 가이드

`request_id`는 B가 가장 강하게 소유해야 하는 계약이다.

원칙

- 모든 외부 요청마다 생성
- 이미 들어온 값이 있으면 형식 검증 후 수용 여부 결정
- 응답 `meta.request_id`와 헤더에 같이 반영
- 하위 서비스 호출 헤더에도 포함
- 이벤트 payload에도 포함

검증 포인트

- UI 응답
- Usage 로그
- Billing 레코드
- Recommendation 입력

## 6. 더미 사용자/팀 컨텍스트 가이드

현재는 정식 인증이 없으므로 B가 아래 방식을 제공해야 한다.

- 개발용 헤더 수용 또는
- Gateway 기본값 주입

예시

- `X-User-Id`
- `X-Team-Id`

주의사항

- 헤더가 없을 때의 기본 동작을 문서화한다.
- Policy와 RateLimit이 같은 컨텍스트를 보도록 통일한다.
- 추후 인증 도입을 위해 컨텍스트 생성 레이어를 분리한다.

## 7. Policy 구현 가이드

초기에는 복잡한 룰 엔진보다 단순 규칙 평가가 낫다.

우선 구현할 룰

- 특정 팀의 특정 모델 차단
- 사용자 일일 호출 수 제한
- 팀 월 예산 초과 차단
- 서비스 allowlist

권장 구조

- 입력 context
- 규칙 목록
- 평가 결과
- 차단 사유

반환 예시 필드

- `allowed`
- `reason_code`
- `matched_rule`

## 8. Rate Limit 구현 가이드

MVP는 Redis 기반 단순 카운터로 충분하다.

우선순위

- user 기준 제한
- team 기준 제한

주의사항

- rate limit 실패는 정책 실패와 다른 에러 코드로 구분한다.
- 카운터 키 규칙을 문서화한다.
- 하루 단위, 분 단위, 월 단위를 혼용하면 빠르게 혼란이 생긴다.

## 9. 외부 AI 연동 가이드

초기에는 mock provider를 먼저 만든다.

이유

- A가 바로 실행 흐름을 붙일 수 있다.
- C가 이벤트와 비용 계산을 먼저 검증할 수 있다.
- 실제 외부 AI 장애에 종속되지 않는다.

이후 실제 provider로 바꿀 때 필요한 것

- timeout
- 재시도 기준
- 모델 매핑
- 실패 응답 변환

## 10. 이벤트 발행 가이드

최소 이벤트

- `ai.requested`
- `policy.checked`
- `limit.applied`

반드시 포함할 필드

- `request_id`
- `user_id`
- `team_id`
- `service_id`
- `workflow_id`
- `model`
- `status`

실수하기 쉬운 지점

- 실패 요청에서 이벤트를 생략하는 것
- Gateway가 책임 없는 usage 정규화까지 끌어안는 것
- `request_id`만 있고 `workflow_id`를 빼먹는 것

## 11. 에러 포맷 가이드

B는 에러 코드 표준 소유자에 가깝다.

최소 에러 코드

- `INVALID_REQUEST`
- `POLICY_BLOCKED`
- `RATE_LIMIT_EXCEEDED`
- `AI_PROVIDER_ERROR`

원칙

- 에러 메시지는 사용자 친화적이어야 하지만, 코드 자체는 안정적이어야 한다.
- 같은 상황에서 다른 코드를 반환하지 않는다.
- A가 화면 분기를 할 수 있을 정도로 일관돼야 한다.

## 12. B가 A와 맞춰야 하는 것

- 실행 API 요청/응답
- 실행 상태 값
- 에러 코드
- `request_id` 위치
- 더미 헤더 사용 방식

A가 UI를 빨리 만들 수 있게, 문서보다 한 발 앞서 바뀌는 인터페이스를 줄이는 것이 중요하다.

## 13. B가 C와 맞춰야 하는 것

- `usage.tracked`를 만들기 위한 원본 실행 메타데이터
- 실패 요청의 status 의미
- 가격표 계산에 필요한 model/token 정보
- 정책/제한 결과 이벤트 발행 시점

특히 C가 usage를 정규화하려면 Gateway가 `request_id`, `service_id`, `workflow_id`, `model`, 실행 status를 안정적으로 내려줘야 한다.

## 14. 로그와 메트릭

B는 아래를 반드시 남겨야 한다.

- `request_id`
- `service_id`
- `workflow_id`
- 정책 평가 결과
- rate limit 결과
- upstream 호출 결과
- 최종 상태 코드

메트릭 우선순위

- 요청 수
- 4xx/5xx 비율
- 정책 차단 수
- rate limit 차단 수
- upstream timeout 수

## 15. 테스트 가이드

최소 테스트

- 정상 실행
- 잘못된 입력
- 정책 차단
- rate limit 초과
- external AI 실패
- 이벤트 발행 성공

권장 통합 테스트

1. 실행 요청 수신
2. request_id 생성
3. policy 통과
4. rate limit 통과
5. mock provider 호출
6. `ai.requested`, `policy.checked`, `limit.applied` 발행 확인

## 16. PR 기준

- "Gateway 전체"가 아니라 기능 단위로 나눈다.
- 이벤트 추가와 API 추가를 한 PR에 섞을 때는 섹션으로 설명한다.
- 테스트 케이스와 샘플 payload를 꼭 붙인다.

좋은 PR 예시

- `feat(gateway): add request id middleware and error envelope`
- `feat(gateway): orchestrate policy and rate-limit checks before execution`

## 17. B가 자주 막히는 지점과 해결법

문제

- A 요구사항 때문에 응답 필드가 계속 늘어남

해결

- 응답 필수/선택 필드를 구분하고 문서 먼저 고정

문제

- C가 원하는 이벤트 필드가 많음

해결

- 정말 계산과 집계에 필요한 최소 필드부터 확정

문제

- 외부 AI 연동이 불안정함

해결

- mock provider를 기본 경로로 유지하고 실제 provider는 adapter로 분리

## 18. 완료 기준

- 실행 API가 안정적으로 동작한다.
- 정책/제한 차단이 구분된다.
- `request_id`가 끝까지 전파된다.
- `ai.requested`, `policy.checked`, `limit.applied`가 발행된다.
- A와 C가 동시에 연동 가능한 수준의 계약 안정성을 가진다.
