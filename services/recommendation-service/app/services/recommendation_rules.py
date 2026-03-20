from __future__ import annotations

from typing import Protocol

from app.models.recommendation import RuleEvaluationResult, UsageSnapshot


class RecommendationRule(Protocol):
    def evaluate(self, snapshot: UsageSnapshot) -> RuleEvaluationResult:
        """Evaluate one snapshot and decide whether it qualifies for a recommendation."""


class SavingsRecommendationRule:
    def __init__(
        self,
        *,
        min_monthly_cost_savings: float = 25.0,
        min_monthly_token_savings: int = 10000,
    ) -> None:
        self._min_monthly_cost_savings = min_monthly_cost_savings
        self._min_monthly_token_savings = min_monthly_token_savings

    def evaluate(self, snapshot: UsageSnapshot) -> RuleEvaluationResult:
        if not snapshot.policy_allowed:
            return RuleEvaluationResult.reject(
                snapshot=snapshot,
                recommendation_type="lower_cost_model",
                reason="조직 정책상 허용되지 않는 후보 모델입니다.",
            )

        if snapshot.monthly_cost_savings <= 0:
            return RuleEvaluationResult.reject(
                snapshot=snapshot,
                recommendation_type="lower_cost_model",
                reason="후보 모델이 비용 절감 효과를 만들지 못합니다.",
            )

        if snapshot.monthly_token_savings <= 0:
            return RuleEvaluationResult.reject(
                snapshot=snapshot,
                recommendation_type="lower_cost_model",
                reason="후보 모델이 토큰 절감 효과를 만들지 못합니다.",
            )

        if (
            snapshot.monthly_cost_savings < self._min_monthly_cost_savings
            or snapshot.monthly_token_savings < self._min_monthly_token_savings
        ):
            return RuleEvaluationResult.reject(
                snapshot=snapshot,
                recommendation_type="lower_cost_model",
                reason="절감 규모가 MVP 임계값에 미달합니다.",
            )

        return RuleEvaluationResult.accept(
            snapshot=snapshot,
            recommendation_type="lower_cost_model",
            confidence=self._confidence(snapshot),
            reason=(
                "최근 7일 스냅샷 기준 월 예상 비용 "
                f"{snapshot.monthly_cost_savings:.2f} 및 월 예상 토큰 "
                f"{snapshot.monthly_token_savings:,} 절감이 확인되어 "
                f"{snapshot.candidate_model} 전환을 권장합니다."
            ),
        )

    def _confidence(self, snapshot: UsageSnapshot) -> str:
        if (
            snapshot.monthly_requests >= 1500
            and snapshot.cost_reduction_ratio >= 0.30
            and snapshot.token_reduction_ratio >= 0.20
        ):
            return "high"

        if snapshot.monthly_requests >= 500 and snapshot.cost_reduction_ratio >= 0.15:
            return "medium"

        return "low"
