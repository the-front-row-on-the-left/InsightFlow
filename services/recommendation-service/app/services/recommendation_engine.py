from __future__ import annotations

from app.models.recommendation import RecommendationItem, RecommendationPeriod, RecommendationResponseData, UsageSnapshot


class RecommendationEngine:
    def build_recommendations(self, user_id: str) -> RecommendationResponseData:
        snapshots = self._load_usage_snapshots(user_id)

        recommendations = [
            self._to_recommendation(snapshot)
            for snapshot in snapshots
            if snapshot.policy_allowed and snapshot.candidate_cost_per_request < snapshot.current_cost_per_request
        ]

        return RecommendationResponseData(
            user_id=user_id,
            period=RecommendationPeriod.model_validate(
                {
                    "from": "2026-03-14",
                    "to": "2026-03-20",
                    "unit": "day",
                }
            ),
            recommendations=recommendations,
        )

    def _to_recommendation(self, snapshot: UsageSnapshot) -> RecommendationItem:
        monthly_cost_savings = (
            snapshot.current_cost_per_request - snapshot.candidate_cost_per_request
        ) * snapshot.monthly_requests
        monthly_token_savings = (
            snapshot.current_total_tokens_per_request - snapshot.candidate_total_tokens_per_request
        ) * snapshot.monthly_requests

        return RecommendationItem(
            type="lower_cost_model",
            service_id=snapshot.service_id,
            current_model=snapshot.current_model,
            recommended_model=snapshot.candidate_model,
            estimated_monthly_savings=f"{monthly_cost_savings:.2f}",
            estimated_token_savings=monthly_token_savings,
            confidence="medium",
            reason=(
                "최근 7일 동일 작업의 평균 비용과 토큰 사용량이 높아 "
                "더 저렴한 대체 모델로 전환할 수 있습니다."
            ),
        )

    def _load_usage_snapshots(self, user_id: str) -> list[UsageSnapshot]:
        return [
            UsageSnapshot(
                service_id="svc_doc_summary",
                current_model="gpt-4o-mini",
                candidate_model="gpt-4.1-mini",
                monthly_requests=96,
                avg_prompt_tokens=700,
                avg_completion_tokens=240,
                current_cost_per_request=25.00,
                candidate_cost_per_request=12.50,
                current_total_tokens_per_request=940,
                candidate_total_tokens_per_request=850,
                policy_allowed=True,
            )
        ]
