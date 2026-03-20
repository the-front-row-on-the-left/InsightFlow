# Service Test UI Design

**Date:** 2026-03-20

**Goal**

C 역할 서비스와 관련 API를 빠르게 수동 검증할 수 있는 독립 테스트 프론트를 추가한다. 이 프론트는 운영용 제품 UI가 아니라 구현 확인과 로컬 디버깅을 위한 도구이며, 필요 시 폴더 단위로 쉽게 제거할 수 있어야 한다.

**Scope**

- 새 테스트 프론트 앱 `apps/service-test-ui` 추가
- `usage-service`, `billing-service`, `notification-service`, `recommendation-service` 호출 UI 제공
- 환경변수 기반 서비스 URL 설정
- 요청/응답 JSON 확인 기능 제공
- 루트 `.gitignore`를 현재 저장소 구조에 맞게 재정비

**Out of Scope**

- 운영용 인증/권한 처리
- 디자인 시스템 통합
- 복잡한 라우팅, 상태관리, 차트, 저장 기능
- 프론트에서 서비스 계약을 추상화하는 별도 SDK 작성

## Approach Options

### Option 1: 정적 HTML 테스트 페이지

가장 가볍고 제거가 쉽지만, 현재 저장소의 프론트 개발 흐름과 분리된다. 입력 상태, JSON 출력, 환경변수 주입을 직접 다뤄야 해서 오히려 유지보수성이 떨어질 수 있다.

### Option 2: 별도 Vue/Vite 테스트 앱

기존 프론트 스택과 같은 계열을 사용하되, 운영 프론트와는 완전히 분리된 새 앱을 만든다. 빠르게 구현할 수 있고 추후 삭제도 폴더 단위로 단순하다.

### Option 3: Swagger/Redoc만 사용

구현량은 적지만, 여러 서비스를 한 화면에서 빠르게 눌러보는 목적에는 덜 맞는다. 샘플 입력과 응답 비교를 반복하기도 불편하다.

**Recommendation**

Option 2를 사용한다. 저장소의 기존 프론트 경험을 재사용할 수 있고, 테스트 전용 앱이라는 분리 목표도 지킬 수 있다.

## Architecture

`apps/service-test-ui`는 단일 페이지 앱으로 구현한다. 앱은 서비스별 테스트 패널 목록과 공통 API 호출 유틸만 가진다. 각 패널은 하나의 서비스 또는 엔드포인트 그룹에 집중하며, 요청 폼 상태와 최근 응답 상태를 로컬 컴포넌트 상태로 관리한다.

서비스 URL은 `VITE_*` 환경변수로 주입한다. 브라우저가 직접 각 백엔드 서비스를 호출하므로, 실행 환경에 따라 localhost 또는 compose 네트워크에 맞춰 손쉽게 바꿀 수 있어야 한다.

## Components

### 1. App Shell

단일 페이지 레이아웃이다. 앱 목적, 환경변수 기준 URL 정보, 서비스 테스트 패널 목록을 보여준다.

### 2. Service Test Panel

서비스별 카드형 섹션이다. 다음 책임을 가진다.

- 엔드포인트 설명 표시
- 샘플 파라미터 프리셋 제공
- 입력값 수정 UI 제공
- 요청 전송
- 로딩/성공/실패 상태 표시
- 응답 JSON 출력

패널은 범용 컴포넌트로 만들고, 서비스별 설정 객체를 주입받아 재사용한다.

### 3. API Client Utility

브라우저 `fetch`를 감싼 얇은 유틸이다. 성공 응답과 실패 응답을 공통 형식으로 변환하고, 네트워크 오류 메시지를 사용자에게 보여주기 쉬운 형태로 만든다.

### 4. Service Definitions

테스트 가능한 서비스 목록, 기본 엔드포인트, 샘플 파라미터, HTTP 메서드, 설명을 선언형 데이터로 정의한다. UI가 코드 분기보다 설정 위주로 움직이게 해서 삭제와 확장을 단순하게 만든다.

## Data Flow

1. 사용자가 서비스 패널에서 프리셋을 선택하거나 입력값을 수정한다.
2. 패널이 서비스 설정을 기반으로 요청 URL과 쿼리 파라미터를 조합한다.
3. API client utility가 해당 서비스로 요청을 전송한다.
4. 응답이 성공이면 JSON 본문과 상태 코드를 패널에 표시한다.
5. 실패면 HTTP 상태, 에러 메시지, 응답 본문을 그대로 노출한다.

## Error Handling

- 환경변수 URL이 비어 있으면 패널에서 즉시 경고를 보여준다.
- 네트워크 오류와 HTTP 오류를 구분해서 표시한다.
- JSON 파싱에 실패하면 원문 텍스트를 그대로 보여준다.
- 한 패널의 실패가 다른 패널 사용에 영향을 주지 않도록 상태를 분리한다.

## Testing Strategy

### Frontend

- 서비스 패널 렌더링 테스트 1개 이상
- 프리셋 적용 및 요청 상태 전환 테스트 1개 이상
- API 유틸의 성공/실패 처리 테스트 1개 이상

### Build Verification

- `apps/service-test-ui` 단독 빌드 확인
- 기존 `apps/ai-platform-ui`가 영향받지 않았는지 최소 확인

## .gitignore Plan

루트 `.gitignore`는 현재 저장소의 Node, Gradle, Java, Python, IDE, OS, env 파일을 모두 반영하는 형태로 재작성한다. 특히 새 테스트 프론트와 FastAPI recommendation service에서 생기는 다음 항목을 포함한다.

- `node_modules`, `dist`, `.vite`
- `.gradle`, `build`, `target`, `out`
- `__pycache__`, `.pytest_cache`, `.venv`, `.mypy_cache`
- `.env`, `.env.*`
- IDE/OS 산출물과 로그 파일

단, `.env.example`처럼 공유가 필요한 예시 파일은 무시 대상에 넣지 않는다.

## Success Criteria

- `apps/service-test-ui`를 단독으로 실행할 수 있다.
- 4개 C 역할 서비스에 대해 샘플 요청을 브라우저에서 보낼 수 있다.
- 응답과 에러를 눈으로 바로 확인할 수 있다.
- 새 앱은 기존 운영 프론트와 분리되어 있어 제거가 쉽다.
- `.gitignore`가 현재 저장소의 주요 생성물을 빠짐없이 커버한다.
