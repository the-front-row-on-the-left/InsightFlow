# AI Platform Spec (Owner A)

## 1. 목표

사용자가 AI 서비스를 탐색하고, 단일 실행 또는 Workflow 형태로 실행하며, 결과를 저장/재사용할 수 있게 한다.

## 2. 화면 범위

| 화면 | 목적 |
|------|------|
| 서비스 목록 | 카탈로그 조회 |
| 서비스 상세 | 설명 / 예시 / 비용 표시 |
| Workflow 생성 | 서비스 조합 |
| 실행 화면 | 입력값 제출 / 결과 확인 |
| 내 실행 내역 | 결과 및 상태 조회 |
| 비용/추천 요약 | 분석 결과 소비 |

## 3. 기능 명세

### 서비스 카탈로그
- 카테고리/태그/키워드 필터
- 서비스 비용 모델 표시
- 추천 뱃지 표시 가능

### Workflow
- 최소 2단계 step 저장 가능
- step은 `service_id` 기준
- 템플릿 이름 저장 가능
- 초기에는 drag-and-drop 없이 리스트 방식

### 실행
- 단일 서비스 실행
- workflow_id 기반 실행
- 응답 상태 표시: 진행중 / 성공 / 실패
- request_id를 UI 상태에 보존

## 4. A가 먼저 만들면 좋은 API Mock

- `GET /api/catalog/services`
- `GET /api/catalog/services/{id}`
- `POST /api/workflows`
- `GET /api/workflows/{id}`
- `POST /api/executions`
- `GET /api/executions/{id}`
- `GET /api/recommendations?user_id=...`

## 5. UI 데이터 모델

```json
{
  "service_id": "svc_doc_summary",
  "name": "문서 요약 AI",
  "category": "analysis",
  "pricing_model": "per_token",
  "supported_models": ["gpt-4o-mini"]
}
```

## 6. 개발 우선순위

1. 서비스 목록
2. 서비스 실행
3. 실행 결과 조회
4. Workflow 저장/조회
5. 추천/비용 요약

## 7. 완료 조건

- 화면 3개 이상 동작
- mock → real API 전환 가능
- 정책 차단 메시지 처리
- 비용/추천 조회 화면 최소 1개
