package com.insightflow.notification.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public record CostCalculatedEvent(
        String requestId,
        String userId,
        String teamId,
        String serviceId,
        String workflowId,
        String model,
        String currency,
        BigDecimal cost,
        Instant occurredAt
) implements AnalyticsNotificationEvent {

    @Override
    public String eventType() {
        return "cost.calculated";
    }

    @Override
    public Map<String, String> metadata() {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("service_id", serviceId);
        if (workflowId != null && !workflowId.isBlank()) {
            metadata.put("workflow_id", workflowId);
        }
        metadata.put("model", model);
        metadata.put("currency", currency);
        metadata.put("cost", cost.toPlainString());
        return Map.copyOf(metadata);
    }
}
