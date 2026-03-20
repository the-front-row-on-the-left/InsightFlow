# Recommendation Service ML/Scoring Evolution Plan

## 1. 문서 목적

이 문서는 현재 문서화된 Recommendation Service의 MVP 범위를 유지하면서, 향후 `recommendation-service`를 규칙 기반 FastAPI 엔진에서 하이브리드 ML/스코어링 파이프라인으로 발전시키기 위한 단계별 계획을 정리한다.

기준 문서는 아래와 같다.

- `docs/15_Analytics_Spec.md`
- `docs/04_Service_Boundary.md`
- `docs/03_System_Context_and_Architecture.md`
- `docs/05_API_Contract.md`
- `docs/06_Event_Contract.md`
- `docs/07_Data_Model.md`
- `docs/17_Observability_and_Alerting_Guide.md`
- `docs/12_Risk_and_Decision_Log.md`

이 계획의 핵심 전제는 다음과 같다.

- MVP는 계속 규칙 기반 추천을 사용한다.
- MVP 구현 우선순위는 FastAPI-first이다.
- 서비스 경계는 유지한다. Recommendation Service는 원천 실행을 변경하지 않고 추천만 제공한다.
- ML/스코어링은 MVP 완료 이후의 고도화 단계로 도입한다.

## 2. 현재 기준선

현재 문서 기준 Recommendation Service는 다음 역할에 집중한다.

- `usage.tracked`, `cost.calculated` 이벤트를 바탕으로 추천을 생성한다.
- `GET /api/recommendations?user_id={user_id}` 조회 API를 제공한다.
- 추천 결과와 생성 근거를 `recommendations` 및 `metadata_json`에 저장한다.
- 초기 추천 유형은 비용 최적화, 서비스 추천, workflow 저장 추천 정도로 제한한다.
- 추천은 배치/지연 허용 구조로 시작한다.

즉, 현재 MVP의 목표는 "좋은 추천 시스템"보다 "설명 가능한 추천 1종 이상을 안정적으로 제공하는 것"이다. 향후 ML/스코어링 파이프라인도 이 기준선을 깨지 않고 확장해야 한다.

## 3. 목표 상태

장기적으로 Recommendation Service는 아래 4계층 구조를 갖는 것을 목표로 한다.

1. 후보 생성: 규칙, 패턴 집계, 인기 서비스, workflow 재사용 신호로 추천 후보를 만든다.
2. 특성 계산: 비용, 사용 패턴, 허용 정책, 팀/서비스 맥락을 feature로 정규화한다.
3. 점수화: 룰 점수, 휴리스틱 점수, ML 점수를 조합해 랭킹한다.
4. 서빙/피드백: API 응답, 노출 로그, 수락/무시/전환 결과를 다시 데이터로 적재한다.

초기에는 1번과 2번 일부만 사용하고, 이후 3번을 점진적으로 도입한다. 최종적으로도 "정책 위반 가능 후보 제거"와 "설명 가능한 fallback 규칙"은 항상 남겨둔다.

## 4. 단계별 진화 계획

### 4.1 Phase 0 - MVP 규칙 기반 FastAPI 추천

목표

- FastAPI 기반 Recommendation Service가 단순 규칙으로 추천을 생성하고 조회 API를 안정적으로 제공한다.

구성

- 입력: `usage.tracked`, `cost.calculated`
- 처리: 고정 규칙 평가
- 저장: `recommendations` 테이블
- 조회: 사용자 기준 추천 조회

대표 규칙

- 최근 7일 동일 서비스의 고비용 요청이 임계치 이상이면 저비용 대체 모델 추천
- 특정 서비스 조합이 반복되면 workflow 저장 추천
- 팀/사용자 사용 패턴상 자주 쓰는 서비스 카테고리 추천

성공 기준

- 추천 1종 이상 생성 가능
- 추천 생성 근거가 `metadata_json`에 남음
- 운영자가 추천 생성 실패와 신선도를 추적 가능

주의

- 이 단계에서는 ML 모델 학습이나 실시간 피처 저장소를 도입하지 않는다.
- 추천 품질보다 설명 가능성과 운영 단순성을 우선한다.

### 4.2 Phase 1 - 스코어링 준비용 데이터/파이프라인 정비

목표

- 규칙 엔진은 유지하되, 향후 점수화에 필요한 데이터 계약과 저장 구조를 먼저 만든다.

추가할 것

- 추천 후보 생성과 최종 노출 로직 분리
- 추천 근거 metadata 표준화
- 추천 노출 로그, 클릭/저장/수락/무시 로그 적재
- 사용자/팀/서비스/workflow 단위 feature snapshot 배치 생성
- 학습/평가용 offline dataset 생성 잡

이 단계의 핵심 산출물

- `candidate_features` 성격의 배치 산출물
- `recommendation_impressions`, `recommendation_actions` 성격의 이벤트 또는 테이블
- 추천 사유 코드 체계
- 추천 대상별 최신 snapshot freshness 기준

비고

- API는 기존 조회 계약을 유지한다.
- 실제 사용자 응답은 아직 규칙 점수 또는 단순 휴리스틱 정렬로 충분하다.

### 4.3 Phase 2 - 하이브리드 룰 + 점수화 파이프라인

목표

- 배치 기반 후보 점수화를 도입하고, 온라인 조회 시에는 미리 계산된 결과를 우선 서빙한다.

구성 요소

- Candidate Generator
- Feature Builder
- Rule/Heuristic Scorer
- Recommendation Ranker
- Recommendation Store
- FastAPI Serving API

동작 방식

- 오프라인 배치가 사용자/팀/workflow별 추천 후보를 만들고 점수를 계산한다.
- 온라인 API는 사전 계산된 추천을 읽어 응답한다.
- 추천 사유와 주요 feature 요약을 함께 보관해 설명 가능성을 유지한다.

장점

- 온라인 지연시간을 크게 늘리지 않는다.
- 추천 로직을 규칙과 점수화 계층으로 분리할 수 있다.
- ML 도입 전에도 랭킹 품질을 점진적으로 개선할 수 있다.

### 4.4 Phase 3 - ML 랭킹/컨텍스트 스코어링 도입

목표

- 규칙과 휴리스틱 위에 ML 랭킹을 얹어 추천 우선순위를 고도화한다.

적용 범위

- 사용자별 모델 대체 추천 랭킹
- 서비스 추천 랭킹
- workflow 저장/재사용 추천 우선순위

권장 방식

- 초기는 gradient boosted tree 또는 가벼운 learning-to-rank 계열
- 설명 가능 feature 위주 시작
- shadow scoring으로 운영 검증 후 실제 랭킹 반영

원칙

- 정책 위반 후보는 ML 점수와 무관하게 제외
- ML 점수 unavailable 시 Phase 2 점수 또는 Phase 0 규칙으로 fallback
- low-confidence 구간은 탐색보다 보수적 추천 우선

## 5. 목표 아키텍처

```text
usage.tracked / cost.calculated / policy.checked / limit.applied
            ↓
     Feature Prep Jobs
            ↓
   Candidate Generator Layer
   - cost optimization rules
   - frequent service patterns
   - workflow reuse mining
   - popularity/trend candidates
            ↓
      Scoring / Ranking Layer
   - rule score
   - heuristic score
   - ML score
            ↓
   Recommendation Store + Explanation
            ↓
        FastAPI Serving API
            ↓
   impression / action / acceptance logs
            ↓
     Offline evaluation + retraining
```

아키텍처 원칙

- 서빙 API는 얇게 유지하고, 계산은 최대한 배치 또는 비동기 처리한다.
- 추천 결과와 설명 데이터는 함께 저장한다.
- 후보 생성, 특성 계산, 랭킹을 느슨하게 분리해 단계별 교체가 가능해야 한다.
- Platform이 소비하는 API 계약은 가능한 한 안정적으로 유지한다.

## 6. 데이터 요구사항

필수 입력 데이터

- `usage.tracked`: 요청 수, 토큰, 지연시간, 성공/실패, 모델, 서비스, workflow 맥락
- `cost.calculated`: 비용, 통화, 가격표 버전
- `policy.checked`: 허용/차단 범위, 적용 규칙
- `limit.applied`: quota 상태, 남은 여유
- 서비스 카탈로그 정보: 카테고리, 지원 모델, 가격 특성
- workflow 메타데이터: step 구성, 재사용 여부

추가로 확보해야 할 데이터

- recommendation impression 로그
- recommendation click/save/accept/dismiss 로그
- 추천 이후 실제 전환 결과
- 추천 생성 시점의 feature snapshot
- 규칙/모델 버전 정보

권장 저장 단위

- 원본 이벤트
- 일/주 단위 집계
- 추천 후보 스냅샷
- 최종 추천 결과
- 사용자 반응 로그
- 학습용 label dataset

데이터 품질 요구

- `request_id`, `user_id`, `team_id`, `service_id`, `model` 필드 누락률을 지속 추적한다.
- 늦게 도착한 이벤트를 재처리할 수 있어야 한다.
- 추천 생성 당시 사용한 가격표/룰/모델 버전을 재현할 수 있어야 한다.

## 7. 후보 feature 후보군

비용 최적화 feature

- 최근 7일/30일 평균 비용
- 동일 목적 추정 서비스 대비 비용 편차
- 대체 모델 사용 시 예상 절감액
- 가격표 버전별 변동 폭

사용 패턴 feature

- 사용자/팀별 서비스 사용 빈도
- 특정 workflow 반복 횟수
- 시간대/요일별 사용 패턴
- 최근 사용성 추세 증가율

품질/실행 feature

- 성공률
- 평균/상위 percentile 지연시간
- 재시도 비율
- 정책 차단 빈도

정책/제약 feature

- 팀 허용 모델 여부
- 예산 임계치 근접도
- rate limit 여유 수준
- 서비스 카테고리 허용 여부

추천 상호작용 feature

- 과거 추천 노출 횟수
- 클릭률
- 저장/수락 비율
- 유사 추천에 대한 최근 피로도

설명용 파생 feature

- "절감 예상 비용"
- "자주 함께 쓰는 서비스"
- "최근 반복 workflow"
- "정책상 허용되는 대체 모델"

## 8. Offline Evaluation 계획

목표

- 온라인 반영 전에 추천 품질, 커버리지, 절감 가능성, 설명 가능성을 정량 검증한다.

평가 데이터셋 원칙

- 시간 순 분할을 사용한다. 랜덤 분할만 사용하지 않는다.
- feature 생성 시점 이후의 정보가 누출되지 않게 한다.
- 사용자, 팀, 서비스별 cold-start 구간을 별도 평가한다.

핵심 지표

- Precision@K
- Recall@K
- NDCG@K
- Coverage
- Recommendation acceptance rate 예측력
- Cost saved@K
- False positive rate
- 정책 위반 후보 혼입률

룰/휴리스틱 단계 평가

- 각 규칙별 추천 생성 건수
- 규칙별 후속 수락률
- 규칙 설명의 일관성
- 후보 고갈률

ML 단계 추가 평가

- holdout 기간 uplift
- calibration
- feature null rate
- drift 감지

실험 방식

- 먼저 Phase 0 규칙을 baseline으로 고정한다.
- Phase 1/2의 휴리스틱 점수화가 baseline 대비 개선되는지 본다.
- ML 모델은 shadow scoring으로 baseline과 비교한 뒤 승격한다.

## 9. Online Serving 전략

기본 원칙

- 조회 API 계약은 유지하되, 내부 생성 방식만 진화시킨다.
- 온라인 요청 경로는 짧고 안정적으로 유지한다.
- 추천이 없거나 점수화가 실패하면 규칙 fallback 또는 빈 결과를 반환한다.

권장 응답 구조 확장

- 기존 추천 payload 유지
- 추가 가능 필드: `score`, `reason_code`, `generated_at`, `model_version`, `fallback_type`

온라인 처리 레벨

- 기본: 사전 계산된 추천 조회
- 선택: 요청 문맥이 있을 때 lightweight rerank
- 금지: 무거운 feature join이나 모델 학습성 계산을 사용자 요청 경로에서 직접 수행

지연시간 목표

- 추천 조회 p95는 기존 analytics 조회 수준을 유지
- 온라인 rerank가 들어가도 작은 feature set과 짧은 timeout을 사용

## 10. Batch vs Realtime Scoring 기준

배치 우선 대상

- 사용자별 비용 절감 추천
- 팀별 상위 대체 모델 추천
- 반복 workflow 저장 추천
- 인기 서비스/카테고리 추천

실시간 또는 준실시간이 필요한 경우

- 실행 직후 "이번 요청의 대체 모델 추천"
- 방금 완성된 workflow 기반 즉시 저장 추천
- 최근 예산 급증 탐지에 따른 즉시 절감 가이드

권장 전략

- 기본은 배치 생성 + 온라인 조회
- 필요한 일부만 event-triggered incremental scoring
- 완전 실시간 ML 서빙은 충분한 데이터와 운영 역량 확보 후 도입

판단 기준

- 사용자 가치가 즉시성에 크게 의존하는가
- feature freshness가 추천 품질을 크게 좌우하는가
- 운영 복잡도 증가를 감당할 수 있는가

## 11. Observability 계획

기존 `docs/17_Observability_and_Alerting_Guide.md`를 확장해 Recommendation 전용 지표를 강화한다.

필수 메트릭

- 추천 후보 생성 수
- 후보 필터링 비율
- 추천 생성 성공률/실패율
- 추천 신선도
- 배치 지연 및 backlog
- feature snapshot 생성 성공률
- null feature 비율
- fallback 비율
- recommendation impression 수
- click/save/accept/dismiss 비율
- 추정 절감액 합계

추가 로그 필드

- `recommendation_id`
- `candidate_count`
- `score`
- `score_breakdown`
- `reason_code`
- `rule_version`
- `model_version`
- `feature_snapshot_time`
- `fallback_type`

알림 후보

- 추천 생성 배치 미완료
- 추천 신선도 SLO 위반
- feature null rate 급증
- fallback rate 급증
- 정책 위반 후보 발견
- impression은 있는데 action 로그가 끊긴 상태

## 12. 주요 리스크와 대응

데이터 부족

- 초기에는 사용자 반응 로그가 부족하므로 규칙/휴리스틱 baseline을 오래 유지한다.

피드백 루프 왜곡

- 이미 많이 노출된 추천만 더 강해지는 현상을 막기 위해 holdout과 exploration budget을 제한적으로 운영한다.

정책/비용 기준 불일치

- 정책/가격표 버전을 feature와 추천 결과에 함께 저장해 재현성을 확보한다.

설명 가능성 저하

- ML 점수만 노출하지 말고 사람이 읽을 수 있는 reason code를 유지한다.

운영 복잡도 증가

- 후보 생성, feature, scoring, serving을 한 번에 분리하지 않고 Phase별로 나눈다.

실시간 서빙 비용 증가

- batch-first 전략을 유지하고, 진짜 필요한 유즈케이스만 incremental scoring을 붙인다.

동시 개발 충돌

- API 계약은 가급적 유지하고, 내부 저장소/피처 스키마는 문서 버전으로 관리한다.

## 13. 단계별 Rollout Plan

### Step 1. MVP 안정화

- FastAPI 규칙 기반 추천을 운영 가능한 수준으로 고정한다.
- 추천 생성 근거와 실패 사유를 모두 기록한다.

### Step 2. Shadow Data Collection

- 사용자 반응 로그와 feature snapshot을 수집하지만 랭킹에는 사용하지 않는다.
- offline dataset 품질을 검증한다.

### Step 3. Heuristic Scoring Launch

- 규칙 후보 위에 휴리스틱 점수를 얹되, 일부 내부 사용자 또는 특정 팀에만 노출한다.
- baseline 대비 커버리지와 수락률을 비교한다.

### Step 4. ML Shadow Scoring

- 실서빙은 유지하고, 백그라운드에서 ML 점수를 함께 계산해 baseline과 비교한다.
- drift, null feature, calibration 문제를 먼저 확인한다.

### Step 5. Partial Online Ranking

- 특정 추천 유형부터 ML 랭킹을 실제 반영한다.
- holdout 그룹을 유지해 uplift를 측정한다.

### Step 6. Controlled Expansion

- 추천 유형, 팀 범위, 실시간 유즈케이스를 점진 확장한다.
- 실패 시 항상 규칙 기반 fallback으로 즉시 복귀 가능해야 한다.

승격 기준 예시

- baseline 대비 acceptance uplift
- 추천 커버리지 유지 또는 개선
- 정책 위반 혼입 0
- 운영 오류율 허용 범위 이내
- 추천 신선도와 조회 지연 SLO 만족

## 14. 의사결정 가이드

지금 바로 해야 할 것

- MVP 규칙 목록과 추천 사유 코드를 문서화
- recommendation impression/action 이벤트 계약 추가
- 추천 결과에 버전 정보 저장
- feature snapshot에 필요한 최소 컬럼 합의

MVP 이후 바로 검토할 것

- 배치 주기와 freshness SLO
- candidate generator 분리 여부
- offline dataset 생성 파이프라인 위치
- rule score와 ML score 결합 방식

나중에 해도 되는 것

- 복잡한 실시간 feature store
- 완전 자동 retraining
- multi-armed bandit 등 탐색 최적화
- 개인화 딥러닝 랭킹

## 15. 결론

Recommendation Service의 다음 단계는 "MVP 규칙 엔진을 버리고 ML로 교체"가 아니다. 올바른 순서는 다음과 같다.

1. FastAPI-first 규칙 기반 MVP를 안정화한다.
2. 스코어링에 필요한 데이터와 피드백 루프를 먼저 만든다.
3. 배치 기반 후보/점수화 파이프라인을 도입한다.
4. 마지막에 ML 랭킹을 shadow mode와 제한적 rollout으로 붙인다.

이 순서를 지키면 현재 문서화된 서비스 경계, 조회 API, 운영 단순성을 유지하면서도 Recommendation Service를 점진적으로 ML-ready한 시스템으로 발전시킬 수 있다.
