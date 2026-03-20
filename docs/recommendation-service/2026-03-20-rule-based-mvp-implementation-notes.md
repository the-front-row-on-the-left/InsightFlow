# Recommendation Service Rule-Based MVP Implementation Notes

## Summary

This change moves the FastAPI recommendation worker from a single hard-coded stub to a small rule-based MVP that evaluates structured usage snapshots and emits ranked savings recommendations. The public runtime remains FastAPI and the existing wrapped response shape for `GET /api/recommendations` is preserved.

## Significant Changes

### 1. Added typed recommendation evaluation primitives

File:
- `services/recommendation-service/app/models/recommendation.py`

Changes:
- Kept the existing API response models intact.
- Extended the domain with:
  - `SnapshotBatch` for a period-scoped collection of usage snapshots
  - `RuleEvaluationResult` for accepted and rejected rule outcomes
  - computed `UsageSnapshot` properties for monthly cost savings, monthly token savings, and reduction ratios

Rationale:
- The old engine recomputed everything inline and had no stable intermediate structure for rule evaluation.
- These types make later additions like multiple rule families, ML rescoring, or explanation audits possible without changing the API route contract.

### 2. Added a snapshot repository seam

File:
- `services/recommendation-service/app/services/snapshot_repository.py`

Changes:
- Introduced `SnapshotRepository` as a protocol boundary.
- Added `StaticSnapshotRepository` with per-user structured snapshot fixtures and a shared recommendation period.

Rationale:
- The worker still needs in-service data for this MVP, but the engine should not care where snapshots come from.
- This gives future workers a clear swap point for usage-service, billing-service, or analytics-backed snapshot loading.

### 3. Added explicit recommendation rule evaluation

File:
- `services/recommendation-service/app/services/recommendation_rules.py`

Changes:
- Introduced `RecommendationRule` as the rule interface.
- Implemented `SavingsRecommendationRule`, which:
  - rejects policy-blocked candidates
  - rejects candidates without positive cost savings
  - rejects candidates without positive token savings
  - rejects candidates below MVP thresholds
  - assigns confidence by request volume and relative savings
  - generates a deterministic Korean explanation string with concrete monthly savings figures

Rationale:
- The user request called for structured snapshots and clear rule evaluation rather than a stub.
- This rule keeps the behavior simple, auditable, and extensible while still producing realistic recommendation output.

### 4. Replaced the stub engine with a ranked rule pipeline

File:
- `services/recommendation-service/app/services/recommendation_engine.py`

Changes:
- Added constructor injection for `snapshot_repository` and `rules`.
- Load snapshots through the repository boundary.
- Evaluate snapshots through the rule list.
- Keep only accepted results.
- Sort recommendations by estimated monthly savings and then token savings.
- Map accepted evaluations back into the existing `RecommendationItem` response model.

Rationale:
- This preserves the FastAPI route contract while making the service internals ready for more sophisticated scoring later.
- The ranking step gives predictable payload ordering for API consumers and tests.

### 5. Expanded automated coverage

Files:
- `services/recommendation-service/tests/test_recommendation_engine.py`
- `services/recommendation-service/tests/test_api.py`

Changes:
- Added engine-level tests for:
  - accepted candidate ordering
  - empty results when no snapshot qualifies
  - high-confidence recommendation output
  - filtering of policy-blocked and low-impact snapshots
- Updated API tests to validate:
  - the wrapped response structure still holds
  - the endpoint returns the new ranked payload
  - an empty recommendation list is returned cleanly for a non-qualifying user

Rationale:
- Most of the behavior risk now lives inside the rule engine, so direct unit coverage is needed there.
- API tests stay focused on contract stability and request/response wrapping.

## Rule Thresholds Used in This MVP

- Candidate must be policy-allowed.
- Candidate must produce positive monthly cost savings.
- Candidate must produce positive monthly token savings.
- Candidate must meet both of these thresholds:
  - monthly cost savings >= `25.00`
  - monthly token savings >= `10,000`
- Confidence is assigned as:
  - `high` for large-volume workloads with strong relative savings
  - `medium` for moderate-volume workloads with meaningful cost reduction
  - `low` otherwise

These thresholds are intentionally simple and deterministic so later scoring phases can replace or augment them without needing to redesign the whole service.

## API Contract Notes

Unchanged:
- FastAPI runtime
- `GET /api/recommendations`
- `user_id` query parameter
- wrapped response envelope with `success`, `data`, and `meta.request_id`
- recommendation item field names and aliases

Changed internally:
- recommendation items are now driven by repository-loaded snapshot inputs and rule evaluation instead of one hard-coded stub item

## Remaining Gaps

1. Snapshot loading is still static fixture data and not yet connected to real usage, billing, or policy services.
2. The rule engine currently returns only one recommendation type, `lower_cost_model`, even though the internals are ready for more rule classes.
3. There is no persistence, caching, or audit trail for rejected evaluations yet.
4. Confidence scoring is heuristic and deterministic; it is not calibrated against historical outcomes.
5. The service still operates synchronously and does not yet call any upstream systems.
6. Verification in this workspace used a temporary Python 3.11 virtual environment because Python 3.12 was not available locally and the default Python 3.14 environment could not install `pydantic-core` for this dependency set.

## Verification

Verified with:
- `/tmp/recommendation-service-venv/bin/python -m pytest services/recommendation-service/tests/test_recommendation_engine.py -q`
- `/tmp/recommendation-service-venv/bin/python -m pytest services/recommendation-service/tests/test_api.py -q`
- `/tmp/recommendation-service-venv/bin/python -m pytest services/recommendation-service/tests -q`

Note:
- `services/recommendation-service/pyproject.toml` declares `>=3.12`, so a follow-up run on a Python 3.12 environment is still advisable before integration.
