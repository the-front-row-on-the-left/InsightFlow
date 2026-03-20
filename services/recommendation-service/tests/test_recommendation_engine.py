from app.services.recommendation_engine import RecommendationEngine


def test_build_recommendations_returns_sorted_rule_accepted_candidates():
    engine = RecommendationEngine()

    response = engine.build_recommendations("u_demo_001")

    assert response.period.model_dump(by_alias=True) == {
        "from": "2026-03-14",
        "to": "2026-03-20",
        "unit": "day",
    }
    assert [item.service_id for item in response.recommendations] == [
        "svc_support_chat",
        "svc_doc_summary",
    ]


def test_build_recommendations_returns_empty_list_when_no_snapshot_qualifies():
    engine = RecommendationEngine()

    response = engine.build_recommendations("u_no_savings")

    assert response.recommendations == []


def test_high_volume_snapshot_gets_high_confidence_reason():
    engine = RecommendationEngine()

    response = engine.build_recommendations("u_demo_001")
    recommendation = response.recommendations[0]

    assert recommendation.confidence == "high"
    assert recommendation.estimated_monthly_savings == "286.00"
    assert recommendation.estimated_token_savings == 1540000
    assert recommendation.reason == (
        "최근 7일 스냅샷 기준 월 예상 비용 286.00 및 월 예상 토큰 1,540,000 절감이 확인되어 "
        "gpt-4.1-mini 전환을 권장합니다."
    )


def test_policy_blocked_and_low_impact_snapshots_are_filtered_out():
    engine = RecommendationEngine()

    response = engine.build_recommendations("u_demo_001")
    service_ids = [item.service_id for item in response.recommendations]

    assert "svc_policy_blocked" not in service_ids
    assert "svc_low_gain" not in service_ids
