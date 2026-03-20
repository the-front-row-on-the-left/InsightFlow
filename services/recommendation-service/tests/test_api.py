def test_health_returns_wrapped_status_with_request_id(client):
    response = client.get("/health")

    assert response.status_code == 200

    payload = response.json()
    assert payload["success"] is True
    assert payload["data"] == {
        "service": "recommendation-service",
        "status": "UP",
    }
    assert payload["meta"]["request_id"].startswith("req_")
    assert response.headers["X-Request-Id"] == payload["meta"]["request_id"]


def test_recommendations_return_wrapped_rule_based_payload(client):
    response = client.get(
        "/api/recommendations",
        params={"user_id": "u_demo_001"},
        headers={"X-Request-Id": "req_test_123"},
    )

    assert response.status_code == 200
    assert response.headers["X-Request-Id"] == "req_test_123"
    assert response.json() == {
        "success": True,
        "data": {
            "user_id": "u_demo_001",
            "period": {
                "from": "2026-03-14",
                "to": "2026-03-20",
                "unit": "day",
            },
            "recommendations": [
                {
                    "type": "lower_cost_model",
                    "service_id": "svc_support_chat",
                    "current_model": "gpt-4.1",
                    "recommended_model": "gpt-4.1-mini",
                    "estimated_monthly_savings": "286.00",
                    "estimated_token_savings": 1540000,
                    "confidence": "high",
                    "reason": "최근 7일 스냅샷 기준 월 예상 비용 286.00 및 월 예상 토큰 1,540,000 절감이 확인되어 gpt-4.1-mini 전환을 권장합니다.",
                },
                {
                    "type": "lower_cost_model",
                    "service_id": "svc_doc_summary",
                    "current_model": "gpt-4o-mini",
                    "recommended_model": "gpt-4.1-mini",
                    "estimated_monthly_savings": "57.60",
                    "estimated_token_savings": 115200,
                    "confidence": "medium",
                    "reason": "최근 7일 스냅샷 기준 월 예상 비용 57.60 및 월 예상 토큰 115,200 절감이 확인되어 gpt-4.1-mini 전환을 권장합니다.",
                }
            ],
        },
        "meta": {
            "request_id": "req_test_123",
        },
    }


def test_recommendations_return_empty_wrapped_list_when_nothing_qualifies(client):
    response = client.get(
        "/api/recommendations",
        params={"user_id": "u_no_savings"},
    )

    assert response.status_code == 200

    payload = response.json()
    assert payload["success"] is True
    assert payload["data"] == {
        "user_id": "u_no_savings",
        "period": {
            "from": "2026-03-14",
            "to": "2026-03-20",
            "unit": "day",
        },
        "recommendations": [],
    }
    assert payload["meta"]["request_id"].startswith("req_")
