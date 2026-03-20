package com.insightflow.aiopscore.event;

import java.time.OffsetDateTime;

public record AiCompletedEventPayload(
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
}
