package com.insightflow.usage.domain;

import java.time.OffsetDateTime;

public record UsageEventSnapshot(
        String requestId,
        String requestedEventId,
        String completedEventId,
        String userId,
        String teamId,
        String serviceId,
        String workflowId,
        String requestedModel,
        String completedModel,
        String status,
        Integer promptTokens,
        Integer completionTokens,
        Integer totalTokens,
        Integer latencyMs,
        Boolean billable,
        OffsetDateTime requestedAt,
        OffsetDateTime completedAt
) {
}
