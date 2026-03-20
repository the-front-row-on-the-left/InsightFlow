# Recommendation Service Rule-Based MVP Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the hard-coded recommendation stub with a rule-based MVP that evaluates structured usage snapshots and returns stable FastAPI recommendation payloads for `GET /api/recommendations`.

**Architecture:** Keep the existing FastAPI route contract and wrapped response shape, but split recommendation generation into focused units: typed snapshot input models, reusable rule evaluators, and an engine that loads snapshots, applies rules, and maps accepted rule outcomes into API response items. Use an in-service repository seam so later workers can swap the static dataset for real upstream usage and billing integrations without rewriting the evaluation pipeline.

**Tech Stack:** Python 3.12, FastAPI, Pydantic, pytest

---

## File Structure

- Modify: `services/recommendation-service/app/models/recommendation.py`
  - Extend the existing response models with the structured snapshot, rule context, and intermediate evaluation models needed by the engine.
- Modify: `services/recommendation-service/app/services/recommendation_engine.py`
  - Replace the single stub implementation with a small pipeline that loads snapshots, evaluates rules, filters rejected candidates, sorts accepted recommendations, and builds response items.
- Create: `services/recommendation-service/tests/test_recommendation_engine.py`
  - Cover rule evaluation behavior directly so the engine logic can evolve without overloading API tests.
- Modify: `services/recommendation-service/tests/test_api.py`
  - Keep the contract checks around the wrapped FastAPI response while asserting the new realistic recommendation payload.
- Create: `docs/recommendation-service/2026-03-20-rule-based-mvp-implementation-notes.md`
  - Capture the implementation log, rationale, and remaining gaps after code changes land.

## Chunk 1: Rule Evaluation Core

### Task 1: Lock in the desired engine behavior with failing tests

**Files:**
- Create: `services/recommendation-service/tests/test_recommendation_engine.py`
- Test: `services/recommendation-service/tests/test_recommendation_engine.py`

- [ ] **Step 1: Write the failing tests**

```python
def test_build_recommendations_returns_only_rule_accepted_candidates():
    engine = RecommendationEngine()

    response = engine.build_recommendations("u_demo_001")

    assert [item["service_id"] for item in response.model_dump(by_alias=True)["recommendations"]] == [
        "svc_support_chat",
        "svc_doc_summary",
    ]


def test_build_recommendations_returns_empty_list_when_no_snapshot_qualifies():
    engine = RecommendationEngine(snapshot_repository=StaticSnapshotRepository())

    response = engine.build_recommendations("u_no_savings")

    assert response.model_dump(by_alias=True)["recommendations"] == []
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `pytest services/recommendation-service/tests/test_recommendation_engine.py -q`
Expected: FAIL because the engine still returns the old single-item stub and lacks the repository/rule pipeline.

- [ ] **Step 3: Write the minimal rule-engine implementation**

```python
class RecommendationEngine:
    def __init__(
        self,
        snapshot_repository: SnapshotRepository | None = None,
        rules: Sequence[RecommendationRule] | None = None,
    ) -> None:
        self._snapshot_repository = snapshot_repository or StaticSnapshotRepository()
        self._rules = list(rules or [LowerCostRule(), LowerTokenRule()])

    def build_recommendations(self, user_id: str) -> RecommendationResponseData:
        snapshots, period = self._snapshot_repository.load(user_id)
        evaluations = self._evaluate_snapshots(snapshots)
        items = [self._to_recommendation(result) for result in evaluations if result.accepted]
        return RecommendationResponseData(user_id=user_id, period=period, recommendations=items)
```

- [ ] **Step 4: Run the engine tests to verify they pass**

Run: `pytest services/recommendation-service/tests/test_recommendation_engine.py -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add services/recommendation-service/app/models/recommendation.py services/recommendation-service/app/services/recommendation_engine.py services/recommendation-service/tests/test_recommendation_engine.py
git commit -m "feat: add rule-based recommendation engine core"
```

### Task 2: Cover confidence, filtering, and reason generation edges

**Files:**
- Modify: `services/recommendation-service/tests/test_recommendation_engine.py`
- Modify: `services/recommendation-service/app/services/recommendation_engine.py`

- [ ] **Step 1: Add failing tests for rule detail**

```python
def test_high_volume_snapshot_gets_high_confidence_reason():
    engine = RecommendationEngine(snapshot_repository=StaticSnapshotRepository())

    response = engine.build_recommendations("u_demo_001")
    first = response.model_dump(by_alias=True)["recommendations"][0]

    assert first["confidence"] == "high"
    assert "월 예상 비용" in first["reason"]


def test_policy_blocked_and_negative_savings_snapshots_are_filtered_out():
    engine = RecommendationEngine(snapshot_repository=StaticSnapshotRepository())

    response = engine.build_recommendations("u_demo_001")
    service_ids = [item.service_id for item in response.recommendations]

    assert "svc_policy_blocked" not in service_ids
    assert "svc_low_gain" not in service_ids
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `pytest services/recommendation-service/tests/test_recommendation_engine.py -q`
Expected: FAIL until confidence scoring, reason strings, and rule thresholds are implemented.

- [ ] **Step 3: Implement the minimal thresholds and explanation builder**

```python
if not snapshot.policy_allowed:
    return RuleEvaluationResult.reject(snapshot, "정책상 허용되지 않는 모델 후보입니다.")

if cost_savings < 100 or token_savings <= 0:
    return RuleEvaluationResult.reject(snapshot, "절감 효과가 임계값에 미달합니다.")

confidence = "high" if snapshot.monthly_requests >= 1000 else "medium"
reason = (
    f"월 예상 비용 {cost_savings:.2f} 절감과 "
    f"월 예상 토큰 {token_savings:,} 절감이 확인되어 후보 모델 전환을 권장합니다."
)
```

- [ ] **Step 4: Run the engine tests again**

Run: `pytest services/recommendation-service/tests/test_recommendation_engine.py -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add services/recommendation-service/app/services/recommendation_engine.py services/recommendation-service/tests/test_recommendation_engine.py
git commit -m "feat: score recommendation confidence and filtering"
```

## Chunk 2: FastAPI Contract and Service Documentation

### Task 3: Update the API contract tests to reflect the realistic payload

**Files:**
- Modify: `services/recommendation-service/tests/test_api.py`
- Test: `services/recommendation-service/tests/test_api.py`

- [ ] **Step 1: Write the failing API assertion update**

```python
def test_recommendations_return_wrapped_rule_based_payload(client):
    response = client.get("/api/recommendations", params={"user_id": "u_demo_001"})

    payload = response.json()
    assert payload["data"]["user_id"] == "u_demo_001"
    assert len(payload["data"]["recommendations"]) == 2
    assert payload["data"]["recommendations"][0]["confidence"] == "high"
```

- [ ] **Step 2: Run the API tests to verify they fail**

Run: `pytest services/recommendation-service/tests/test_api.py -q`
Expected: FAIL until the route reflects the new recommendation data ordering and details.

- [ ] **Step 3: Align the engine output with the API contract**

```python
recommendations = sorted(
    accepted_items,
    key=lambda item: (Decimal(item.estimated_monthly_savings), item.estimated_token_savings),
    reverse=True,
)
```

- [ ] **Step 4: Run the API tests to verify they pass**

Run: `pytest services/recommendation-service/tests/test_api.py -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add services/recommendation-service/tests/test_api.py services/recommendation-service/app/services/recommendation_engine.py
git commit -m "test: update recommendation api contract coverage"
```

### Task 4: Write the implementation notes and finish with full verification

**Files:**
- Create: `docs/recommendation-service/2026-03-20-rule-based-mvp-implementation-notes.md`

- [ ] **Step 1: Draft the implementation notes**

Document:
- Final architecture and new service responsibilities
- Why the API contract stayed stable
- Which rule thresholds were chosen for the MVP
- Remaining gaps for real upstream integrations and ML/scoring follow-on work

- [ ] **Step 2: Run the full service test suite**

Run: `pytest services/recommendation-service/tests -q`
Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add docs/recommendation-service/2026-03-20-rule-based-mvp-implementation-notes.md services/recommendation-service/tests
git commit -m "docs: capture recommendation service mvp notes"
```

## Suggested Commit Breakdown

1. `feat: add rule-based recommendation engine core`
2. `feat: score recommendation confidence and filtering`
3. `test: update recommendation api contract coverage`
4. `docs: capture recommendation service mvp notes`
