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


def test_recommendations_return_wrapped_stub_payload(client):
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
                    "service_id": "svc_doc_summary",
                    "current_model": "gpt-4o-mini",
                    "recommended_model": "gpt-4.1-mini",
                    "estimated_monthly_savings": "1200.00",
                    "estimated_token_savings": 8640,
                    "confidence": "medium",
                    "reason": "최근 7일 동일 작업의 평균 비용과 토큰 사용량이 높아 더 저렴한 대체 모델로 전환할 수 있습니다.",
                }
            ],
        },
        "meta": {
            "request_id": "req_test_123",
        },
    }
