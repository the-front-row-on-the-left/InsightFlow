# Backlog & Milestones

## 1. MVP Epic

| Epic | 설명 | 담당 |
|------|------|------|
| EPIC-1 | 서비스 카탈로그/실행 UI | A |
| EPIC-2 | Workflow 생성/저장 | A |
| EPIC-3 | Gateway 오케스트레이션 및 요청 통제 | B |
| EPIC-4 | Policy Service / Rate Limit Service | B |
| EPIC-5 | Usage 수집/조회 | C |
| EPIC-6 | Billing 계산/조회 | C |
| EPIC-7 | Recommendation PoC | C |
| EPIC-8 | Notification 소비 구조 | C |
| EPIC-9 | 통합 및 대시보드 연결 | 전체 |

## 2. 4주 예시 일정

### Week 1
- 계약 문서 확정
- 기본 프로젝트 세팅
- Mock API / Mock Event 구성

### Week 2
- A: 카탈로그/실행 화면
- B: Gateway + Policy/Rate Limit 기본
- C: Usage/Billing 스키마 + Notification/Recommendation 소비기 골격

### Week 3
- A: Workflow 저장/조회
- B: Gateway 연동 + 에러 포맷
- C: 비용 집계 + 추천 로직 PoC + Notification 소비 연결

### Week 4
- 통합 테스트
- 데모 시나리오 정리
- 발표 자료용 스크린샷/지표 확보

## 3. 우선순위 백로그

| 우선순위 | 항목 | 담당 |
|----------|------|------|
| P0 | request_id 전파 | B |
| P0 | 실행 API 계약 | A, B |
| P0 | usage.tracked 이벤트 | B, C |
| P0 | 비용 계산 기준표 | C |
| P1 | Workflow 템플릿 저장 | A |
| P1 | 팀별 정책 설정 | B |
| P1 | Rate limit 기준표 확정 | B |
| P1 | 사용자 비용 조회 화면 | A, C |
| P2 | Notification 소비 구조 | C |
| P2 | 비용 최적화 추천 UI | A, C |

## 4. 데모 목표

- 단일 서비스 실행
- Workflow 실행
- 정책 차단 사례
- 비용 대시보드
- 추천 1건 노출
