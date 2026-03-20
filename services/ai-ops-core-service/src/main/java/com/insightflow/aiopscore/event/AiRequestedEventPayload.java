package com.insightflow.aiopscore.event;

import java.time.OffsetDateTime;

public record AiRequestedEventPayload(
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
}
