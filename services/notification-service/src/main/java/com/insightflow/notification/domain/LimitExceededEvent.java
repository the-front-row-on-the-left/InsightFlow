package com.insightflow.notification.domain;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public record LimitExceededEvent(
        String requestId,
        String userId,
        String teamId,
        String serviceId,
        String workflowId,
        String limitType,
        String threshold,
        String observedValue,
        Instant occurredAt
) implements AnalyticsNotificationEvent {

    @Override
    public String eventType() {
        return "limit.exceeded";
    }

    @Override
    public Map<String, String> metadata() {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("service_id", serviceId);
        if (workflowId != null && !workflowId.isBlank()) {
            metadata.put("workflow_id", workflowId);
        }
        metadata.put("limit_type", limitType);
        metadata.put("threshold", threshold);
        metadata.put("observed_value", observedValue);
        return Map.copyOf(metadata);
    }
}
