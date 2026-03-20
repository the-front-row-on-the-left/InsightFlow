package com.insightflow.usage.domain;

import java.time.OffsetDateTime;

public record AiCompletedEvent(
        String eventId,
        String eventType,
        String requestId,
        String model,
        String status,
        int promptTokens,
        int completionTokens,
        int totalTokens,
        int latencyMs,
        boolean billable,
        OffsetDateTime completedAt
) {

    public static final String EVENT_TYPE = "ai.completed";

    public AiCompletedEvent {
        eventType = eventType == null || eventType.isBlank() ? EVENT_TYPE : eventType;
    }
}
