package com.insightflow.usage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UsageItem(
        @JsonProperty("request_id")
        String requestId,
        @JsonProperty("service_id")
        String serviceId,
        String model,
        String status,
        @JsonProperty("total_tokens")
        int totalTokens,
        @JsonProperty("latency_ms")
        int latencyMs
) {
}
