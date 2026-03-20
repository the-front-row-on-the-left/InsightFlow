package com.insightflow.gateway.domain;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExecutionDetailResponse(
        @JsonProperty("execution_id")
        String executionId,
        @JsonProperty("request_id")
        String requestId,
        @JsonProperty("user_id")
        String userId,
        @JsonProperty("team_id")
        String teamId,
        @JsonProperty("service_id")
        String serviceId,
        @JsonProperty("workflow_id")
        String workflowId,
        String model,
        String status,
        @JsonProperty("error_code")
        String errorCode,
        @JsonProperty("policy_decision")
        PolicyDecision policyDecision,
        @JsonProperty("rate_limit_decision")
        RateLimitDecision rateLimitDecision,
        ExecutionResult result,
        @JsonProperty("created_at")
        Instant createdAt
) {
}
