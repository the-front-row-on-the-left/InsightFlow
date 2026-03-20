# Access & Security Roadmap

## 1. 목적

이 문서는 AI Ops Platform의 접근 통제와 보안 관련 기준을 정리하되, MVP에서 실제 구현할 것과 후속 고도화로 미룰 것을 구분한다.

목표는 다음과 같다.

- MVP 단계에서 구현하지 않을 인증/인가 범위를 명확히 남긴다.
- 비밀정보 관리, 로그 마스킹, 내부 접근 규칙 같은 기본 보안 원칙은 유지한다.
- A, B, C가 추후 인증/인가를 붙일 수 있도록 확장 지점을 문서화한다.

## 2. 기본 원칙

1. 외부 사용자 요청은 반드시 Gateway를 통해 인증된다.
2. 인증과 인가의 최종 진입 게이트는 Gateway가 가진다.
3. 하위 서비스 간 호출은 외부 사용자 토큰을 직접 신뢰하지 않고, Gateway가 전달한 내부 신뢰 헤더 또는 서비스 계정으로 검증한다.
4. 모든 요청은 `request_id`, `user_id`, `team_id`, `role`을 추적 가능해야 한다.
5. 비밀정보는 코드 저장소나 프론트엔드 번들에 포함하지 않는다.
6. 프롬프트 원문, 결과 원문, 민감한 사용자 입력은 기본적으로 로그에 남기지 않는다.

위 원칙 중 1, 2, 4는 최종 목표 기준이다. MVP에서는 아래처럼 축소 적용한다.

- 외부 요청은 Gateway를 통해서만 받는다.
- 인증/인가는 구현하지 않는다.
- `request_id`는 필수로 유지한다.
- `user_id`, `team_id`, `role`은 실제 로그인 기반이 아니라 테스트/더미 값 또는 명시적 입력값으로 처리할 수 있다.

## 3. 인증 구조

## 3.1 MVP 인증 방식

MVP는 정식 인증을 구현하지 않는다.

즉, 초기 버전은 다음 전제를 사용한다.

- 엔드포인트는 별도 로그인 없이 호출 가능
- 프론트는 Access Token 없이 Gateway 호출 가능
- 사용자/팀 컨텍스트가 필요하면 요청 헤더, 테스트 데이터, 고정 더미 값 중 하나를 사용
- 운영 환경으로 가기 전에는 반드시 정식 인증/인가 설계를 다시 열어야 함

권장 더미 헤더 예시

```text
X-Debug-User-Id: u_demo_001
X-Debug-Team-Id: t_demo
X-Debug-User-Role: platform_user
```

주의사항

- 이 헤더는 로컬/개발 환경에서만 허용하는 것이 좋다.
- staging/prod에 가까운 환경으로 갈수록 제거 또는 차단해야 한다.

## 3.2 후속 인증 고도화 방향

정식 인증을 붙일 때는 아래 구조를 권장한다.

```json
{
  "sub": "u_123e4567",
  "email": "user@company.com",
  "team_id": "t_marketing",
  "role": "team_admin",
  "scopes": ["catalog.read", "execution.write", "usage.read"],
  "iss": "https://auth.company.internal",
  "aud": "ai-ops-platform",
  "iat": 1773972000,
  "exp": 1773975600
}
```

후속 구현 시 필수 claim

- `sub`
- `team_id`
- `role`
- `iss`
- `aud`
- `exp`

후속 구현 시 권장 claim

- `email`
- `scopes`
- `name`

## 3.3 Gateway 인증 처리 흐름

MVP 흐름

1. 클라이언트가 요청 전송
2. Gateway가 `request_id` 생성 또는 전달값 검증
3. Gateway가 더미 사용자/팀 컨텍스트를 구성
4. Policy/RateLimit 평가 수행
5. 하위 서비스로 공통 컨텍스트 전달

후속 고도화 흐름

1. 클라이언트가 Bearer Token과 함께 요청 전송
2. Gateway가 토큰 존재 여부 확인
3. Gateway가 서명, 만료, issuer, audience 검증
4. Gateway가 사용자 컨텍스트 추출
5. Gateway가 내부 공통 컨텍스트 생성
6. Gateway가 Policy/RateLimit 평가 전에 인증 실패 여부를 확정
7. 인증 성공 시 내부 서비스로 최소 사용자 컨텍스트 전달

## 4. 인가 모델

## 4.1 후속 권한 모델

정식 인가를 붙일 때는 RBAC 기반으로 시작한다.

| 역할 | 설명 | 대표 권한 |
|------|------|-----------|
| `platform_user` | 일반 사용자 | 카탈로그 조회, 실행, 본인 실행 이력 조회 |
| `team_admin` | 팀 운영자 | 팀 정책 조회, 팀 비용 조회, 팀 추천 조회 |
| `ops_admin` | 운영 관리자 | 전역 정책 관리, 장애 대응용 조회, 운영 로그 조회 |

후속 구현 시 권한은 URL 단위보다는 기능 단위로 정의한다.

예시 권한

- `catalog.read`
- `workflow.read`
- `workflow.write`
- `execution.write`
- `execution.read.self`
- `usage.read.self`
- `usage.read.team`
- `billing.read.team`
- `policy.read.team`
- `policy.write.team`
- `ops.read.audit`

## 4.2 인가 책임 분리

| 책임 | 주체 |
|------|------|
| 토큰 유효성 검증 | Gateway |
| 엔드포인트 접근 제어 | Gateway |
| 팀/사용자 범위 검증 | Gateway + 각 서비스 |
| 모델/서비스 사용 허용 여부 | Policy Service |
| 요청 빈도 제한 | Gateway/RateLimit |

MVP에서는 인증/인가 차단 대신 Policy와 RateLimit이 1차 제어를 담당한다. 정식 인가 도입 후에는 Gateway가 1차 차단을 맡고, 각 서비스는 자신이 소유한 데이터에 대해 최소한의 범위 재검증을 수행한다.

예시

- MVP에서는 `GET /api/executions/{execution_id}` 같은 조회도 보호되지 않을 수 있으므로, 데모/내부 개발 범위를 전제로만 운영한다.
- 후속 인가 도입 시 Usage/Billing 서비스는 `execution_id`가 요청 사용자 또는 동일 팀 자산인지 다시 확인한다.

## 4.3 후속 권한 체크 예시

| API | 최소 권한 | 추가 조건 |
|-----|-----------|-----------|
| `GET /api/catalog/services` | `catalog.read` | 없음 |
| `POST /api/executions` | `execution.write` | 해당 팀의 정책 허용 |
| `GET /api/executions/{id}` | `execution.read.self` | 본인 실행 또는 팀 관리자 |
| `GET /api/billing/teams/{team_id}` | `billing.read.team` | 요청자 `team_id`와 일치 또는 ops_admin |
| `GET /internal/policies` | 내부 서비스 전용 | 외부 호출 차단 |

## 5. 내부 서비스 간 보안

## 5.1 내부 호출 원칙

- `/internal/**` 엔드포인트는 외부 네트워크에서 직접 접근 불가해야 한다.
- MVP에서는 네트워크 레벨 분리 또는 환경 분리로 최소 보호를 하고, 서비스 계정 토큰은 후속 도입 항목으로 둔다.
- 하위 서비스는 `X-Internal-Caller` 같은 내부 식별 헤더를 사용할 수 있으나, 외부에서 위조 불가한 환경이어야 한다.

## 5.2 내부 전달 헤더 표준

Gateway가 하위 서비스로 전달하는 권장 헤더

- `X-Request-Id`
- `X-User-Id`
- `X-Team-Id`
- `X-User-Role`
- `X-Actor-Type: user`
- `X-Authenticated-By: gateway`

주의사항

- 이 헤더들은 외부 클라이언트가 직접 신뢰하면 안 된다.
- 하위 서비스는 Gateway에서만 오는 trusted proxy 환경을 전제로 검증한다.

## 6. 비밀정보 관리

## 6.1 비밀정보 분류

| 항목 | 예시 | 저장 위치 |
|------|------|-----------|
| 외부 AI API Key | OpenAI 호환 키 | Secret Manager 또는 배포 환경 Secret |
| DB 계정 | PostgreSQL 계정 | Secret Manager |
| JWT 공개키/검증 설정 | JWK URL, issuer | 후속 도입 시 Config + 일부 Secret |
| Kafka 인증 정보 | SASL 사용자/비밀번호 | Secret Manager |
| Redis 인증 정보 | 비밀번호 | Secret Manager |

## 6.2 금지 사항

- `.env`를 저장소에 커밋 금지
- 프론트엔드 환경 변수에 서버용 API Key 저장 금지
- 테스트 스크린샷에 토큰/키 노출 금지
- 예제 문서에 실제 Key 사용 금지

## 6.3 권장 방식

- 로컬: `.env.local` 또는 IDE secret 설정
- 개발/스테이징/운영: Secret Manager 또는 CI/CD secret store
- 키 교체 주기: 운영 키는 분기 1회 이상 또는 사고 발생 즉시

## 7. 데이터 보호 기준

## 7.1 민감도 분류

| 데이터 | 민감도 | 기본 정책 |
|--------|--------|-----------|
| 사용자 이메일 | 중간 | 로그 마스킹 권장 |
| 프롬프트 원문 | 높음 | 저장 최소화, 로그 금지 |
| AI 응답 원문 | 중간~높음 | 저장 정책 별도 정의 |
| 비용/사용량 집계 | 중간 | 팀/관리자 범위만 조회 |
| 정책 설정 | 중간 | 관리자만 변경 가능 |

## 7.2 로그 마스킹 규칙

로그에 남기지 말아야 할 항목

- `Authorization` 헤더
- 외부 AI API Key
- 사용자가 입력한 원문 텍스트 전체
- 주민번호, 카드번호, 전화번호 등 패턴 매칭 가능한 개인정보

허용되는 로그 항목

- `request_id`
- `user_id`
- `team_id`
- `service_id`
- `workflow_id`
- `model`
- `latency_ms`
- `status_code`
- `error_code`

## 7.3 저장 기준

- Usage/Billing은 원문 대신 요약된 메타데이터 중심 저장
- 실행 이력 저장 시 `input_size`, `token_count`, `content_hash`를 우선 사용
- 원문 저장이 꼭 필요한 서비스는 명시적으로 별도 정책을 둔다

## 8. 감사 추적과 보안 로깅

## 8.1 감사 대상 이벤트

- 로그인 성공/실패(후속 인증 도입 시)
- 정책 변경
- 관리자 권한 사용
- 비용 조회
- 팀 범위 데이터 조회
- 운영자 수동 재처리

## 8.2 감사 로그 필드

```json
{
  "audit_id": "aud_001",
  "occurred_at": "2026-03-20T12:30:00Z",
  "request_id": "req_001",
  "actor_id": "u_001",
  "actor_role": "ops_admin",
  "action": "policy.update",
  "resource_type": "policy",
  "resource_id": "pol_001",
  "result": "SUCCESS"
}
```

## 9. 보안 에러 응답 기준

MVP에서는 아래 인증/인가 에러를 실제로 구현하지 않아도 된다. 대신 문서상 예약 코드로만 남겨둔다.

| 상황 | HTTP 상태 | 에러 코드 |
|------|-----------|-----------|
| 토큰 누락 | 401 | `AUTH_TOKEN_MISSING` |
| 토큰 만료 | 401 | `AUTH_TOKEN_EXPIRED` |
| 토큰 위조/검증 실패 | 401 | `AUTH_TOKEN_INVALID` |
| 권한 부족 | 403 | `AUTH_FORBIDDEN` |
| 팀 범위 불일치 | 403 | `AUTH_SCOPE_MISMATCH` |
| 내부 API 외부 접근 | 403 | `INTERNAL_API_FORBIDDEN` |

에러 예시

```json
{
  "success": false,
  "error": {
    "code": "AUTH_FORBIDDEN",
    "message": "You do not have permission to access this resource."
  },
  "meta": {
    "request_id": "req_123"
  }
}
```

## 10. 역할별 구현 가이드

## 10.1 A가 반드시 지킬 것

- MVP에서는 로그인 화면 없이 엔드포인트를 호출하는 전제를 허용한다.
- UI는 추후 인증 도입을 위해 API client 레이어에 인증 헤더 삽입 지점을 분리해 둔다.
- 현재는 정책 차단 메시지와 일반 오류 메시지를 구분해 보여주면 충분하다.

## 10.2 B가 반드시 지킬 것

- MVP는 인증 실패 코드를 구현하지 않아도 된다.
- 내부 서비스 전달 헤더 표준을 고정한다.
- `request_id`와 더미 또는 테스트 주체를 함께 로그에 남길 수 있게 한다.
- `/internal/**` 보호를 네트워크 레벨과 애플리케이션 레벨 모두에서 고려한다.
- 추후 인증 필터가 들어갈 위치와 인터페이스를 분리해 둔다.

## 10.3 C가 반드시 지킬 것

- 팀/사용자 범위 조회 API에서 재검증을 수행한다.
- MVP에서는 비용/사용량 조회 보호가 약할 수 있으므로 내부 데모/개발 범위 전제로 사용한다.
- 운영용 재처리 API가 생기면 반드시 감사 로그를 남긴다.

## 11. MVP 이후 확장 항목

- OAuth2/OIDC 표준 통합
- 서비스 간 mTLS
- 세분화된 ABAC 정책
- 테넌트별 암호화 키 분리
- 데이터 보존 기간과 삭제 자동화

## 12. 체크리스트

개발 시작 전

- MVP에서 인증 미구현 합의
- 더미 사용자/팀 컨텍스트 방식 합의
- 내부 헤더 표준 합의
- staging/prod 전 인증 재설계 필요 여부 체크

통합 전

- 내부 API 외부 차단 확인
- 민감정보 로그 노출 점검
- 비용/사용량 조회 노출 범위 점검
