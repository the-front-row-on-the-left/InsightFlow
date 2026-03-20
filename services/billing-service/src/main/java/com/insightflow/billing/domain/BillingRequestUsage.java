package com.insightflow.billing.domain;

import java.time.Instant;

public record BillingRequestUsage(
        String requestId,
        String userId,
        String teamId,
        String workflowId,
        String serviceId,
        String model,
        String status,
        int promptTokens,
        int completionTokens,
        boolean billable,
        Instant occurredAt
) {

    public int totalTokens() {
        return promptTokens + completionTokens;
    }
}
