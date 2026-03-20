# Common Development Start Guide

## 1. 목적

이 문서는 A, B, C가 병렬 개발을 시작하기 전에 반드시 공유해야 할 공통 개발 기준을 정리한다.

목표는 다음과 같다.

- 각자 독립적으로 개발하더라도 통합 시 계약 충돌을 줄인다.
- "무엇부터 만들지"를 공통 언어로 맞춘다.
- MVP 범위와 후순위 항목을 혼동하지 않게 한다.

## 2. 현재 MVP 전제

현재 문서 기준으로 확정된 MVP 전제는 아래와 같다.

- 모든 외부 요청은 Gateway를 통과한다.
- 정식 인증/인가는 구현하지 않는다.
- 대신 `request_id`, 정책, rate limit, 공통 에러 포맷, 이벤트 계약은 반드시 지킨다.
- 사용자/팀 컨텍스트는 더미 값 또는 테스트 헤더를 사용할 수 있다.
- 비용 계산의 단일 기준은 Billing이다.
- Recommendation은 규칙 기반 PoC로 시작한다.

## 3. 개발 시작 전 필독 문서

모두 공통으로 먼저 읽어야 하는 문서

- [01_Project_Charter.md](/Users/skax/Downloads/ai_ops_docs_bundle/01_Project_Charter.md)
- [02_Role_Split_and_RnR.md](/Users/skax/Downloads/ai_ops_docs_bundle/02_Role_Split_and_RnR.md)
- [03_System_Context_and_Architecture.md](/Users/skax/Downloads/ai_ops_docs_bundle/03_System_Context_and_Architecture.md)
- [05_API_Contract.md](/Users/skax/Downloads/ai_ops_docs_bundle/05_API_Contract.md)
- [06_Event_Contract.md](/Users/skax/Downloads/ai_ops_docs_bundle/06_Event_Contract.md)
- [07_Data_Model.md](/Users/skax/Downloads/ai_ops_docs_bundle/07_Data_Model.md)
- [08_Development_Convention.md](/Users/skax/Downloads/ai_ops_docs_bundle/08_Development_Convention.md)
- [10_Test_Strategy.md](/Users/skax/Downloads/ai_ops_docs_bundle/10_Test_Strategy.md)

운영 기준으로 함께 참고할 문서

- [16_Access_and_Security_Roadmap.md](/Users/skax/Downloads/ai_ops_docs_bundle/16_Access_and_Security_Roadmap.md)
- [17_Observability_and_Alerting_Guide.md](/Users/skax/Downloads/ai_ops_docs_bundle/17_Observability_and_Alerting_Guide.md)
- [18_Deployment_and_Release_Management.md](/Users/skax/Downloads/ai_ops_docs_bundle/18_Deployment_and_Release_Management.md)
- [20_Pricing_and_Cost_Policy.md](/Users/skax/Downloads/ai_ops_docs_bundle/20_Pricing_and_Cost_Policy.md)

## 4. 공통 개발 원칙

1. 문서 계약이 코드보다 먼저다.
2. API/Event/Data 모델 변경은 문서 수정 없이 진행하지 않는다.
3. 개인 작업은 mock으로 빨리 진행하되, 통합 기준은 계약 문서로 맞춘다.
4. 모든 요청/이벤트/로그에서 `request_id`를 잃지 않는다.
5. 팀별 최적화보다 전체 통합 성공을 우선한다.

## 5. 병렬 개발의 최소 고정 계약

세 명이 동시에 개발하려면 아래 계약은 반드시 먼저 고정해야 한다.

### API 계약

- 공통 응답 형식
- 공통 에러 형식
- 실행 API 요청/응답
- 비용/사용량 조회 응답 핵심 필드

### 이벤트 계약

- `ai.requested`
- `policy.checked`
- `limit.applied`
- `usage.tracked`
- `cost.calculated`
- `optimization.recommended`

### 식별자 규칙

- `service_id`
- `workflow_id`
- `request_id`
- `execution_id`
- `user_id`
- `team_id`

이 3가지는 작업 초반에 임의 변경하지 않는 것이 중요하다.

## 6. 추천 개발 순서

공통적으로는 아래 순서를 권장한다.

1. 계약 문서 읽기
2. 각자 최소 mock 입력/출력 만들기
3. 각자 로컬 단위 기능 완성
4. 계약 기반 통합 테스트
5. 데모 시나리오 기준으로 보완

좀 더 구체적으로는 다음 흐름이 좋다.

1. B가 `request_id`, 에러 포맷, 실행 API 골격, 이벤트 발행 골격을 먼저 고정
2. A는 mock API로 화면 흐름을 먼저 완성
3. C는 이벤트 소비와 비용 계산 골격을 먼저 완성
4. 이후 A-B 실행 API 연동
5. 이후 B-C 이벤트 연동
6. 마지막으로 A-C 조회 API 연동

## 7. 브랜치와 작업 단위

브랜치 규칙은 [08_Development_Convention.md](/Users/skax/Downloads/ai_ops_docs_bundle/08_Development_Convention.md)를 따른다.

추가 권장사항

- 한 브랜치에서 한 기능만 다룬다.
- 계약 변경과 구현 변경을 한 PR에 섞을 수는 있지만, 설명을 분리한다.
- 공통 문서 수정이 있는 작업은 제목에서 바로 드러나게 한다.

예시

- `feature/gateway-execution-entry`
- `feature/platform-execution-result-view`
- `feature/analytics-billing-calculator`

## 8. 환경과 더미 컨텍스트 기준

MVP는 무인증 접근을 허용하므로, 아래 기준을 공통으로 맞춘다.

- 로컬/개발 환경에서 `X-User-Id`, `X-Team-Id` 헤더를 사용할 수 있다.
- 헤더가 없으면 Gateway가 기본 더미 값을 채울 수 있다.
- 모든 서비스는 `user_id`, `team_id`가 비어 있을 때의 기본 처리 방식을 문서화한다.
- A는 화면에서 더미 사용자 전제를 숨기지 말고 명시적으로 개발한다.

권장 기본값 예시

- `user_id = u_demo_001`
- `team_id = t_demo`

## 9. 공통 디버깅 기준

장애나 오류가 생기면 아래 순서로 본다.

1. `request_id`가 생성되었는가
2. API 응답의 `error.code`가 문서와 맞는가
3. 이벤트가 발행되었는가
4. 로그에서 같은 `request_id`가 이어지는가
5. UI/Usage/Billing의 데이터 표시가 계약 필드와 일치하는가

## 10. 공통 테스트 기준

최소한 아래는 각자 구현 중 반드시 확인한다.

- 정상 요청 1건
- 정책 차단 1건
- rate limit 초과 1건
- `usage.tracked` 기반 비용 계산 1건
- 추천 1건 생성 또는 빈 결과 처리 1건

## 11. 공통 PR 체크리스트

- 문서 계약과 구현이 일치하는가
- 샘플 요청/응답이 설명 가능한가
- 로그에 `request_id`가 남는가
- 에러 코드가 규격과 맞는가
- mock에서 real로 바꿀 때 경계가 분리되어 있는가
- 최소 테스트가 포함되어 있는가

## 12. 통합 주간 루틴

주 2회 통합 점검 규칙을 더 실무적으로 운영하면 아래와 같다.

화요일

- API/Event 계약 변경 여부 확인
- 식별자와 필수 필드 변경 여부 확인
- 각자 막힌 인터페이스 공유

금요일

- 실제 데모 흐름 점검
- 현재 연결된 기능 기준으로 smoke test
- 다음 주에 먼저 고정해야 할 계약 확인

## 13. 자주 틀리는 지점

- A가 임시 프론트 모델명을 그대로 UI/Backend 계약으로 굳혀버리는 경우
- B가 에러 코드를 임의로 바꾸는 경우
- C가 이벤트 필드가 오기 전까지 구현을 멈추는 경우
- `request_id`는 있는데 `execution_id` 연결이 끊기는 경우
- 비용 표시를 A가 자체 계산해버리는 경우

## 14. 통합 전 최종 체크리스트

- `POST /api/executions` 흐름이 끝까지 이어지는가
- `request_id`가 UI, Gateway, Usage, Billing에서 동일하게 추적되는가
- `usage.tracked` 이벤트 필수 필드 누락이 없는가
- Billing이 가격표를 찾지 못하는 경우가 처리되는가
- Recommendation이 빈 결과일 때도 UI가 깨지지 않는가
- 문서와 실제 payload 예시가 어긋나지 않는가

## 15. 개발 시작 선언 기준

아래가 되면 각자 구현을 본격 시작해도 된다.

- 실행 API 계약 확인 완료
- 이벤트 계약 확인 완료
- 더미 사용자/팀 컨텍스트 방식 합의
- 가격표 구조 합의
- 공통 에러 포맷 합의

이 기준이 흔들리면 코드를 더 쓰기보다 문서를 먼저 정리하는 것이 낫다.
