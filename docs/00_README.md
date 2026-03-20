# AI Ops Platform 문서 패키지

이 문서 패키지는 PRD v1.0을 기준으로, 아래 3개 병렬 개발 트랙이 바로 작업을 시작할 수 있도록 정리한 시작 문서 모음이다.

1. AI 플랫폼 (Front + Workflow)
2. API Gateway + Policy + RateLimit
3. Usage + Billing + Recommendation + Notification

## 문서 구성

- `01_Project_Charter.md` : 프로젝트 개요 / 목표 / 범위
- `02_Role_Split_and_RnR.md` : 3인 역할 분담 및 협업 규칙
- `03_System_Context_and_Architecture.md` : 전체 구조 / 컨텍스트 / 서비스 관계
- `04_Service_Boundary.md` : 서비스 경계 / 책임 / 소유 데이터
- `05_API_Contract.md` : 초기 REST API 계약
- `06_Event_Contract.md` : Kafka 이벤트 계약
- `07_Data_Model.md` : 핵심 데이터 모델 초안
- `08_Development_Convention.md` : 코딩/브랜치/리뷰/에러 응답 규칙
- `09_Environment_and_Runbook.md` : 개발환경 / 로컬 실행 / 공통 의존성
- `10_Test_Strategy.md` : 테스트 전략 및 인수 기준
- `11_Backlog_and_Milestones.md` : MVP 백로그 / 마일스톤
- `12_Risk_and_Decision_Log.md` : 리스크 / 의사결정 로그
- `13_AI_Platform_Spec.md` : 담당자 1용 세부 설계
- `14_Gateway_Control_Spec.md` : 담당자 2용 세부 설계
- `15_Analytics_Spec.md` : 담당자 3용 세부 설계
- `16_Access_and_Security_Roadmap.md` : MVP 무인증 접근 기준, 비밀정보, 감사 추적, 추후 인증/인가 로드맵
- `17_Observability_and_Alerting_Guide.md` : 로깅, 메트릭, 대시보드, 알림 기준
- `18_Deployment_and_Release_Management.md` : 환경 분리, 배포 전략, 설정/릴리즈 관리
- `19_Incident_Response_Runbook.md` : 장애 탐지, 대응 절차, 역할별 런북
- `20_Pricing_and_Cost_Policy.md` : 가격표 구조, 비용 계산, 반올림, 비용 정책
- `21_Common_Development_Start_Guide.md` : 3인 공통 개발 착수 기준, 계약/통합 체크리스트
- `22_Development_Guide_For_A_Platform.md` : A용 UI/Workflow 개발 가이드
- `23_Development_Guide_For_B_Gateway.md` : B용 Gateway/Policy/RateLimit 개발 가이드
- `24_Development_Guide_For_C_Analytics.md` : C용 Usage/Billing/Recommendation 개발 가이드

## 권장 사용 순서

1. `01_Project_Charter.md`
2. `02_Role_Split_and_RnR.md`
3. `03_System_Context_and_Architecture.md`
4. 계약 문서 (`05~07`)
5. 운영 기준 문서 (`16~20`)
6. 공통 개발 가이드 (`21`)
7. 각자 자신의 세부 설계 문서와 역할별 개발 가이드 (`13~15`, `22~24`)
8. 개발 규칙 / 테스트 / 마일스톤 (`08~12`)

## 개발 시작 전 필수 추천 문서

- 공통: `21_Common_Development_Start_Guide.md`
- A: `13_AI_Platform_Spec.md`, `16_Access_and_Security_Roadmap.md`, `17_Observability_and_Alerting_Guide.md`, `20_Pricing_and_Cost_Policy.md`, `22_Development_Guide_For_A_Platform.md`
- B: `14_Gateway_Control_Spec.md`, `16_Access_and_Security_Roadmap.md`, `17_Observability_and_Alerting_Guide.md`, `18_Deployment_and_Release_Management.md`, `19_Incident_Response_Runbook.md`, `23_Development_Guide_For_B_Gateway.md`
- C: `15_Analytics_Spec.md`, `06_Event_Contract.md`, `17_Observability_and_Alerting_Guide.md`, `18_Deployment_and_Release_Management.md`, `20_Pricing_and_Cost_Policy.md`, `24_Development_Guide_For_C_Analytics.md`
