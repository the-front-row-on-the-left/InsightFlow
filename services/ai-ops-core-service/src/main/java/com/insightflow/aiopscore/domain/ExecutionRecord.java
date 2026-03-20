package com.insightflow.aiopscore.domain;

import java.time.Instant;

public record ExecutionRecord(
        String executionId,
        String requestId,
        String userId,
        String teamId,
        String serviceId,
        String workflowId,
        String model,
        String status,
        String errorCode,
        PolicyDecision policyDecision,
        RateLimitDecision rateLimitDecision,
        ExecutionResult result,
        Instant createdAt
) {
    public ExecutionDetailResponse toDetailResponse() {
        return new ExecutionDetailResponse(
                executionId,
                requestId,
                userId,
                teamId,
                serviceId,
                workflowId,
                model,
                status,
                errorCode,
                policyDecision,
                rateLimitDecision,
                result,
                createdAt
        );
    }

    public ExecutionCreateResponse toCreateResponse() {
        return new ExecutionCreateResponse(
                executionId,
                requestId,
                serviceId,
                workflowId,
                status,
                new OrchestrationTargets(
                        "policy-service",
                        "rate-limit-service",
                        result == null || result.provider() == null ? "pending" : result.provider()
                ),
                result
        );
    }
}
