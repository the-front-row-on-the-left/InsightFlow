package com.insightflow.usage.domain;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record UsageRecord(
        String requestId,
        String userId,
        String teamId,
        String serviceId,
        String workflowId,
        String model,
        String status,
        String policyResult,
        String limitResult,
        int promptTokens,
        int completionTokens,
        int totalTokens,
        int latencyMs,
        OffsetDateTime requestedAt
) {

    public LocalDate requestedOn() {
        return requestedAt.toLocalDate();
    }
}
