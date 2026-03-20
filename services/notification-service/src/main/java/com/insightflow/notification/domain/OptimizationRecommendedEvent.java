package com.insightflow.notification.domain;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public record OptimizationRecommendedEvent(
        String requestId,
        String userId,
        String teamId,
        String serviceId,
        String currentModel,
        String recommendedModel,
        String reason,
        Instant occurredAt
) implements AnalyticsNotificationEvent {

    @Override
    public String eventType() {
        return "optimization.recommended";
    }

    @Override
    public String workflowId() {
        return null;
    }

    @Override
    public Map<String, String> metadata() {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("service_id", serviceId);
        metadata.put("current_model", currentModel);
        metadata.put("recommended_model", recommendedModel);
        metadata.put("reason", reason);
        if (teamId != null && !teamId.isBlank()) {
            metadata.put("team_id", teamId);
        }
        return Map.copyOf(metadata);
    }
}
