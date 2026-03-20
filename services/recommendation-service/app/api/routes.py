from fastapi import APIRouter, Request

from app.models.recommendation import HealthData
from app.models.response import wrap_success
from app.services.recommendation_engine import RecommendationEngine


router = APIRouter()
recommendation_engine = RecommendationEngine()


@router.get("/health")
def health(request: Request):
    return wrap_success(
        HealthData(service="recommendation-service", status="UP"),
        request_id=request.state.request_id,
    )


@router.get("/api/recommendations")
def get_recommendations(user_id: str, request: Request):
    return wrap_success(
        recommendation_engine.build_recommendations(user_id),
        request_id=request.state.request_id,
    )
