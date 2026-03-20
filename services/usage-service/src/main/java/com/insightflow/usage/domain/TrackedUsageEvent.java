package com.insightflow.usage.domain;

import java.time.OffsetDateTime;

public record TrackedUsageEvent(
        String eventId,
        String eventType,
        String requestId,
        String userId,
        String teamId,
        String serviceId,
        String workflowId,
        String model,
        String status,
        int promptTokens,
        int completionTokens,
        int totalTokens,
        int latencyMs,
        boolean billable,
        OffsetDateTime occurredAt,
        OffsetDateTime trackedAt
) {

    public static final String EVENT_TYPE = "usage.tracked";

    public TrackedUsageEvent {
        eventType = eventType == null || eventType.isBlank() ? EVENT_TYPE : eventType;
    }
}
