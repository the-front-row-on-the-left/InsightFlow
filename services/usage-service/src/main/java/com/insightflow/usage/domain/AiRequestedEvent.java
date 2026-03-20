package com.insightflow.usage.domain;

import java.time.OffsetDateTime;

public record AiRequestedEvent(
        String eventId,
        String eventType,
        String requestId,
        String userId,
        String teamId,
        String serviceId,
        String workflowId,
        String model,
        OffsetDateTime requestedAt
) {

    public static final String EVENT_TYPE = "ai.requested";

    public AiRequestedEvent {
        eventType = eventType == null || eventType.isBlank() ? EVENT_TYPE : eventType;
    }
}
