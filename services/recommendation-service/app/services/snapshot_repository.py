from __future__ import annotations

from typing import Protocol

from app.models.recommendation import RecommendationPeriod, SnapshotBatch, UsageSnapshot


class SnapshotRepository(Protocol):
    def load(self, user_id: str) -> SnapshotBatch:
        """Return the usage snapshots used to build recommendations for one user."""


class StaticSnapshotRepository:
    def __init__(
        self,
        dataset: dict[str, list[UsageSnapshot]] | None = None,
        period: RecommendationPeriod | None = None,
    ) -> None:
        self._dataset = dataset or self._default_dataset()
        self._period = period or RecommendationPeriod.model_validate(
            {
                "from": "2026-03-14",
                "to": "2026-03-20",
                "unit": "day",
            }
        )

    def load(self, user_id: str) -> SnapshotBatch:
        snapshots = [
            snapshot.model_copy(deep=True)
            for snapshot in self._dataset.get(user_id, [])
        ]
        return SnapshotBatch(period=self._period, snapshots=snapshots)

    def _default_dataset(self) -> dict[str, list[UsageSnapshot]]:
        return {
            "u_demo_001": [
                UsageSnapshot(
                    service_id="svc_support_chat",
                    current_model="gpt-4.1",
                    candidate_model="gpt-4.1-mini",
                    monthly_requests=2200,
                    avg_prompt_tokens=1600,
                    avg_completion_tokens=800,
                    current_cost_per_request=0.24,
                    candidate_cost_per_request=0.11,
                    current_total_tokens_per_request=2400,
                    candidate_total_tokens_per_request=1700,
                    policy_allowed=True,
                ),
                UsageSnapshot(
                    service_id="svc_doc_summary",
                    current_model="gpt-4o-mini",
                    candidate_model="gpt-4.1-mini",
                    monthly_requests=960,
                    avg_prompt_tokens=700,
                    avg_completion_tokens=240,
                    current_cost_per_request=0.12,
                    candidate_cost_per_request=0.06,
                    current_total_tokens_per_request=940,
                    candidate_total_tokens_per_request=820,
                    policy_allowed=True,
                ),
                UsageSnapshot(
                    service_id="svc_policy_blocked",
                    current_model="gpt-4.1",
                    candidate_model="gpt-4.1-mini",
                    monthly_requests=1800,
                    avg_prompt_tokens=1200,
                    avg_completion_tokens=450,
                    current_cost_per_request=0.19,
                    candidate_cost_per_request=0.08,
                    current_total_tokens_per_request=1650,
                    candidate_total_tokens_per_request=1100,
                    policy_allowed=False,
                ),
                UsageSnapshot(
                    service_id="svc_low_gain",
                    current_model="gpt-4.1-mini",
                    candidate_model="gpt-4.1-nano",
                    monthly_requests=400,
                    avg_prompt_tokens=380,
                    avg_completion_tokens=120,
                    current_cost_per_request=0.05,
                    candidate_cost_per_request=0.03,
                    current_total_tokens_per_request=500,
                    candidate_total_tokens_per_request=492,
                    policy_allowed=True,
                ),
            ],
            "u_no_savings": [
                UsageSnapshot(
                    service_id="svc_translation",
                    current_model="gpt-4.1-mini",
                    candidate_model="gpt-4.1",
                    monthly_requests=850,
                    avg_prompt_tokens=420,
                    avg_completion_tokens=210,
                    current_cost_per_request=0.07,
                    candidate_cost_per_request=0.09,
                    current_total_tokens_per_request=630,
                    candidate_total_tokens_per_request=700,
                    policy_allowed=True,
                )
            ],
        }
