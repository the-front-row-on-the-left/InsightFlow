package com.insightflow.billing.domain;

import java.time.OffsetDateTime;

public record UsageTrackedEvent(
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
}
