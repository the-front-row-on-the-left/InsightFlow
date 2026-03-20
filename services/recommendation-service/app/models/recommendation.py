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
