# Deployment & Release Management

## 1. 목적

이 문서는 로컬, 개발, 스테이징, 운영 환경으로 이어지는 배포 전략과 설정 관리 원칙을 정의한다.

목표는 다음과 같다.

- 세 명이 병렬 개발해도 환경 차이로 통합이 막히지 않게 한다.
- 계약 변경과 배포 변경을 구분해서 관리한다.
- MVP 단계에서도 최소한의 릴리즈 검증 절차를 유지한다.

## 2. 환경 구성

| 환경 | 목적 | 특징 |
|------|------|------|
| local | 개인 개발 | mock/더미 데이터 허용 |
| dev | 팀 통합 개발 | 기능 통합, 빠른 검증 |
| staging | 배포 전 검증 | 운영과 유사 설정 |
| prod | 실제 운영 | 변경 통제, 모니터링 필수 |

## 3. 환경별 원칙

## 3.1 local

- mock 서비스 사용 가능
- 추천 서비스 생략 가능
- 외부 AI는 mock 또는 sandbox key 사용
- 개발 편의를 위한 seed 데이터 허용

## 3.2 dev

- 최소한 실제 서비스 간 HTTP/Kafka 연결 검증
- API/Event 계약 변경 검증 환경
- 실패해도 빠른 복구 우선

## 3.3 staging

- 운영과 동일한 배포 방식 사용
- 실제 Secret 체계 사용
- 가격표, 정책 룰, 에러 포맷 최종 검증

## 3.4 prod

- 수동 승인 또는 보호된 CI/CD 파이프라인 필수
- 롤백 절차 문서화 필수
- 알림과 대시보드 준비 없이 배포 금지

## 4. 배포 단위

MVP 기준 배포 단위는 아래와 같다.

- `platform-ui`
- `gateway-service`
- `policy-service`
- `rate-limit-service`
- `usage-service`
- `billing-service`
- `notification-service`
- `recommendation-service`

초기에는 일부 서비스가 스캐폴드 수준이어도, 배포 관점의 논리적 단위는 최신 기획서 기준으로 유지한다.

## 5. 브랜치와 릴리즈 흐름

## 5.1 권장 브랜치 전략

- `main`: 배포 가능한 기준선
- `feature/*`: 개인 작업 브랜치
- `release/*`: 스테이징 검증용 브랜치 선택 가능
- `hotfix/*`: 운영 긴급 수정

## 5.2 병합 기준

- API/Event/Data 계약 문서와 구현이 일치해야 한다.
- 최소 단위 테스트 통과
- 담당자 외 1인 이상 리뷰 권장
- 변경이 계약에 영향이 있으면 관련 문서 동시 수정

## 6. 설정 관리

## 6.1 설정 계층

1. 애플리케이션 기본값
2. 환경별 설정 파일
3. Secret/환경 변수
4. 배포 시점 override

우선순위가 높을수록 하위 값을 덮는다.

## 6.2 환경 변수 권장 목록

공통

- `APP_ENV`
- `LOG_LEVEL`
- `SERVICE_NAME`
- `REQUEST_TIMEOUT_MS`

Gateway

- `REDIS_HOST`
- `KAFKA_BOOTSTRAP_SERVERS`
- `UPSTREAM_AI_BASE_URL`
- `DEFAULT_USER_ID`
- `DEFAULT_TEAM_ID`

Usage/Billing/Recommendation

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `KAFKA_BOOTSTRAP_SERVERS`
- `PRICE_TABLE_VERSION`

Platform UI

- `VITE_API_BASE_URL`
- `VITE_APP_ENV`

## 6.3 설정 관리 규칙

- 운영 설정은 문서가 아니라 배포 시스템에서 관리한다.
- 설정 이름은 서비스 간 최대한 일관되게 맞춘다.
- 환경마다 의미가 달라지는 변수명은 피한다.
- mock 전용 설정은 `local` 또는 `dev`에서만 허용한다.

## 7. 데이터베이스 마이그레이션

## 7.1 원칙

- 스키마 변경은 코드 배포와 분리 가능한 방향을 우선 고려한다.
- 파괴적 변경은 두 단계 이상으로 진행한다.
- 운영 배포 직전 수동 SQL 실행을 피하고 migration 도구를 사용한다.

## 7.2 권장 순서

1. nullable 컬럼 추가
2. 새 코드 배포
3. 데이터 backfill
4. 읽기 경로 전환
5. 구 컬럼 제거

## 8. 배포 전략

## 8.1 MVP 권장 전략

- UI: 정적 배포, 빠른 롤백 가능
- Gateway: rolling update
- Usage/Billing/Recommendation: rolling update + consumer lag 확인

## 8.2 배포 순서

변경이 없는 경우에는 모두 독립 배포 가능하지만, 일반적인 통합 배포는 아래 순서를 권장한다.

1. 공통 인프라/Secret 반영
2. Policy/Rate Limit
3. Usage/Billing/Notification/Recommendation
4. Gateway
5. Platform UI

이유

- 하위 의존성이 준비된 뒤 Gateway가 새 계약을 노출해야 한다.
- UI는 마지막에 배포해 사용자 노출 시점과 서버 준비 상태를 맞춘다.

## 9. 릴리즈 체크리스트

배포 전

- 관련 문서 업데이트 완료
- 테스트 전략 기준 주요 시나리오 통과
- 대시보드 준비
- 알림 규칙 확인
- 롤백 방법 확인

배포 중

- health check 정상
- 에러율 모니터링
- Kafka lag 모니터링
- 비용 계산 지연 확인

배포 후

- 카탈로그 조회 확인
- 단일 실행 확인
- 정책 차단 시나리오 확인
- 비용 반영 확인
- 추천 조회 확인

## 10. 롤백 기준

즉시 롤백 조건

- Gateway 5xx 급증
- 인증 미구현 상태에서의 공개 범위 과다 노출
- 비용 계산 전면 중단
- 이벤트 소비 중단
- UI에서 핵심 실행 흐름 불가

롤백 방식

- UI: 직전 버전 재배포
- 서비스: 직전 안정 이미지 배포
- 스키마: 가능하면 롤포워드 우선, 불가 시 사전 준비된 다운 마이그레이션 적용

## 11. 계약 변경 관리

다음 항목은 계약 변경으로 간주한다.

- API 경로/필드 변경
- 이벤트 이름/필수 필드 변경
- ID 규칙 변경
- 공통 에러 코드 변경

변경 절차

1. 문서 업데이트
2. 영향 담당자 확인
3. 하위 호환 여부 판단
4. dev 환경 통합 테스트
5. staging 검증
6. prod 반영

## 12. 역할별 배포 책임

## 12.1 A

- UI 빌드와 환경별 API base URL 검증
- 에러 메시지 및 상태 화면의 운영 환경 동작 확인
- 배포 후 핵심 사용자 플로우 스모크 테스트

## 12.2 B

- Gateway, Policy, RateLimit의 호환성 보장
- 추후 인증 도입 시 필터/헤더 구조가 깨지지 않도록 인터페이스 분리
- request_id/에러 포맷/헬스체크 검증

## 12.3 C

- 스키마 변경과 소비기 배포 순서 관리
- 가격표 버전과 비용 계산 로직 동기화
- Notification 소비기와 Recommendation 소비기 의존 이벤트 확인
- consumer lag와 재처리 영향 확인

## 13. 배포 금지 조건

- 문서 계약과 구현이 어긋난 상태
- 운영 Secret 미검증
- 롤백 경로 미정
- 알림 미구성
- 가격표 누락 상태에서 Billing 배포

## 14. MVP 이후 확장

- canary 배포
- feature flag
- blue/green 배포
- 테넌트 단위 점진 배포
- 자동 승인/자동 롤백
