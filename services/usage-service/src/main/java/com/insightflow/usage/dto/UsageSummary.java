package com.insightflow.usage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UsageSummary(
        @JsonProperty("total_requests")
        int totalRequests,
        @JsonProperty("total_tokens")
        int totalTokens,
        @JsonProperty("avg_tokens_per_request")
        int avgTokensPerRequest,
        @JsonProperty("avg_latency_ms")
        int avgLatencyMs,
        @JsonProperty("succeeded_requests")
        int succeededRequests,
        @JsonProperty("failed_requests")
        int failedRequests,
        @JsonProperty("blocked_requests")
        int blockedRequests
) {
}
