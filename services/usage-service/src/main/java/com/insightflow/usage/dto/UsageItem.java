package com.insightflow.usage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UsageItem(
        @JsonProperty("request_id")
        String requestId,
        @JsonProperty("service_id")
        String serviceId,
        @JsonProperty("workflow_id")
        String workflowId,
        String model,
        String status,
        @JsonProperty("policy_result")
        String policyResult,
        @JsonProperty("limit_result")
        String limitResult,
        @JsonProperty("prompt_tokens")
        int promptTokens,
        @JsonProperty("completion_tokens")
        int completionTokens,
        @JsonProperty("total_tokens")
        int totalTokens,
        @JsonProperty("latency_ms")
        int latencyMs,
        @JsonProperty("requested_at")
        String requestedAt
) {
}
