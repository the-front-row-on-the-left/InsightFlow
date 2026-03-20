package com.insightflow.gateway.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExecutionResult(
        String provider,
        String output,
        @JsonProperty("prompt_tokens")
        Integer promptTokens,
        @JsonProperty("completion_tokens")
        Integer completionTokens,
        @JsonProperty("total_tokens")
        Integer totalTokens,
        @JsonProperty("latency_ms")
        Long latencyMs
) {
}
