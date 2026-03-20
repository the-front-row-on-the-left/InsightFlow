from __future__ import annotations

from collections import defaultdict

import httpx

from app.core.config import settings
from app.models.recommendation import RecommendationPeriod, SnapshotBatch, UsageSnapshot
from app.services.snapshot_repository import StaticSnapshotRepository


class HttpSnapshotRepository:
    def __init__(self, timeout: float = 3.0) -> None:
        self._timeout = timeout
        self._fallback = StaticSnapshotRepository()

    def load(self, user_id: str) -> SnapshotBatch:
        try:
            usage_payload = self._get_json(
                f"{settings.usage_service_base_url}/api/usage/users/{user_id}"
            )
            billing_payload = self._get_json(
                f"{settings.billing_service_base_url}/api/billing/users/{user_id}"
            )
            return self._build_snapshot_batch(user_id, usage_payload["data"], billing_payload["data"])
        except Exception:
            return self._fallback.load(user_id)

    def _get_json(self, url: str) -> dict:
        response = httpx.get(url, timeout=self._timeout)
        response.raise_for_status()
        payload = response.json()
        if not payload.get("success"):
            raise ValueError(f"Unsuccessful response from {url}")
        return payload

    def _build_snapshot_batch(self, user_id: str, usage_data: dict, billing_data: dict) -> SnapshotBatch:
        billing_by_request = {
            item["request_id"]: item
            for item in billing_data.get("items", [])
        }
        grouped_usage: dict[tuple[str, str], list[dict]] = defaultdict(list)
        for item in usage_data.get("items", []):
            grouped_usage[(item["service_id"], item["model"])].append(item)

        snapshots: list[UsageSnapshot] = []
        for (service_id, model), items in grouped_usage.items():
            billed_items = [
                billing_by_request[item["request_id"]]
                for item in items
                if item["request_id"] in billing_by_request
            ]
            if not billed_items:
                continue
            candidate_model = self._candidate_model(model)
            if candidate_model is None:
                continue

            avg_prompt_tokens = round(sum(item["prompt_tokens"] for item in items) / len(items))
            avg_completion_tokens = round(sum(item["completion_tokens"] for item in items) / len(items))
            avg_total_tokens = round(sum(item["total_tokens"] for item in items) / len(items))
            avg_cost = sum(float(item["total_cost"]) for item in billed_items) / len(billed_items)

            snapshots.append(
                UsageSnapshot(
                    service_id=service_id,
                    current_model=model,
                    candidate_model=candidate_model,
                    monthly_requests=max(len(items) * 30, len(items)),
                    avg_prompt_tokens=avg_prompt_tokens,
                    avg_completion_tokens=avg_completion_tokens,
                    current_cost_per_request=round(avg_cost, 2),
                    candidate_cost_per_request=round(avg_cost * 0.55, 2),
                    current_total_tokens_per_request=avg_total_tokens,
                    candidate_total_tokens_per_request=max(round(avg_total_tokens * 0.75), 1),
                    policy_allowed=all(item["limit_result"] != "NOT_APPLIED" for item in items),
                )
            )

        return SnapshotBatch(
            period=RecommendationPeriod.model_validate(usage_data["period"]),
            snapshots=snapshots,
        )

    def _candidate_model(self, current_model: str) -> str | None:
        candidates = {
            "gpt-4.1": "gpt-4.1-mini",
            "gpt-4o-mini": "gpt-4.1-mini",
            "gpt-4.1-mini": "gpt-4.1-nano",
        }
        return candidates.get(current_model)
