from __future__ import annotations

from collections.abc import Sequence

from app.models.recommendation import (
    RecommendationItem,
    RecommendationResponseData,
    RuleEvaluationResult,
    UsageSnapshot,
)
from app.services.recommendation_rules import RecommendationRule, SavingsRecommendationRule
from app.services.snapshot_repository import SnapshotRepository, StaticSnapshotRepository


class RecommendationEngine:
    def __init__(
        self,
        snapshot_repository: SnapshotRepository | None = None,
        rules: Sequence[RecommendationRule] | None = None,
    ) -> None:
        self._snapshot_repository = snapshot_repository or StaticSnapshotRepository()
        self._rules = list(rules or [SavingsRecommendationRule()])

    def build_recommendations(self, user_id: str) -> RecommendationResponseData:
        snapshot_batch = self._snapshot_repository.load(user_id)
        accepted_evaluations = [
            evaluation
            for evaluation in (self._evaluate_snapshot(snapshot) for snapshot in snapshot_batch.snapshots)
            if evaluation.accepted
        ]
        accepted_evaluations.sort(
            key=lambda evaluation: (
                evaluation.estimated_monthly_savings,
                evaluation.estimated_token_savings,
            ),
            reverse=True,
        )

        return RecommendationResponseData(
            user_id=user_id,
            period=snapshot_batch.period,
            recommendations=[
                self._to_recommendation(evaluation)
                for evaluation in accepted_evaluations
            ],
        )

    def _evaluate_snapshot(self, snapshot: UsageSnapshot) -> RuleEvaluationResult:
        last_result: RuleEvaluationResult | None = None
        for rule in self._rules:
            result = rule.evaluate(snapshot)
            last_result = result
            if result.accepted:
                return result
        return last_result or RuleEvaluationResult.reject(
            snapshot=snapshot,
            recommendation_type="lower_cost_model",
            reason="적용 가능한 추천 규칙이 없습니다.",
        )

    def _to_recommendation(self, evaluation: RuleEvaluationResult) -> RecommendationItem:
        snapshot = evaluation.snapshot
        return RecommendationItem(
            type=evaluation.recommendation_type,
            service_id=snapshot.service_id,
            current_model=snapshot.current_model,
            recommended_model=snapshot.candidate_model,
            estimated_monthly_savings=f"{evaluation.estimated_monthly_savings:.2f}",
            estimated_token_savings=evaluation.estimated_token_savings,
            confidence=evaluation.confidence,
            reason=evaluation.reason,
        )
