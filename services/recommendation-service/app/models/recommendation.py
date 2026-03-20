from __future__ import annotations

from typing import Literal

from pydantic import BaseModel, Field


class HealthData(BaseModel):
    service: str
    status: str


class RecommendationPeriod(BaseModel):
    from_: str = Field(alias="from")
    to: str
    unit: str


class RecommendationItem(BaseModel):
    type: str
    service_id: str = Field(alias="service_id")
    current_model: str = Field(alias="current_model")
    recommended_model: str = Field(alias="recommended_model")
    estimated_monthly_savings: str = Field(alias="estimated_monthly_savings")
    estimated_token_savings: int = Field(alias="estimated_token_savings")
    confidence: Literal["low", "medium", "high"]
    reason: str


class RecommendationResponseData(BaseModel):
    user_id: str = Field(alias="user_id")
    period: RecommendationPeriod
    recommendations: list[RecommendationItem]


class UsageSnapshot(BaseModel):
    service_id: str
    current_model: str
    candidate_model: str
    monthly_requests: int
    avg_prompt_tokens: int
    avg_completion_tokens: int
    current_cost_per_request: float
    candidate_cost_per_request: float
    current_total_tokens_per_request: int
    candidate_total_tokens_per_request: int
    policy_allowed: bool

    @property
    def monthly_cost_savings(self) -> float:
        return (self.current_cost_per_request - self.candidate_cost_per_request) * self.monthly_requests

    @property
    def monthly_token_savings(self) -> int:
        return (
            self.current_total_tokens_per_request - self.candidate_total_tokens_per_request
        ) * self.monthly_requests

    @property
    def cost_reduction_ratio(self) -> float:
        if self.current_cost_per_request == 0:
            return 0.0
        return (self.current_cost_per_request - self.candidate_cost_per_request) / self.current_cost_per_request

    @property
    def token_reduction_ratio(self) -> float:
        if self.current_total_tokens_per_request == 0:
            return 0.0
        return (
            self.current_total_tokens_per_request - self.candidate_total_tokens_per_request
        ) / self.current_total_tokens_per_request


class SnapshotBatch(BaseModel):
    period: RecommendationPeriod
    snapshots: list[UsageSnapshot]


class RuleEvaluationResult(BaseModel):
    accepted: bool
    recommendation_type: str
    snapshot: UsageSnapshot
    confidence: Literal["low", "medium", "high"] = "low"
    reason: str
    estimated_monthly_savings: float = 0.0
    estimated_token_savings: int = 0

    @classmethod
    def accept(
        cls,
        *,
        snapshot: UsageSnapshot,
        recommendation_type: str,
        confidence: Literal["low", "medium", "high"],
        reason: str,
    ) -> "RuleEvaluationResult":
        return cls(
            accepted=True,
            recommendation_type=recommendation_type,
            snapshot=snapshot,
            confidence=confidence,
            reason=reason,
            estimated_monthly_savings=snapshot.monthly_cost_savings,
            estimated_token_savings=snapshot.monthly_token_savings,
        )

    @classmethod
    def reject(
        cls,
        *,
        snapshot: UsageSnapshot,
        recommendation_type: str,
        reason: str,
    ) -> "RuleEvaluationResult":
        return cls(
            accepted=False,
            recommendation_type=recommendation_type,
            snapshot=snapshot,
            reason=reason,
        )
