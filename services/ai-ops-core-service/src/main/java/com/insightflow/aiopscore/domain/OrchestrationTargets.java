package com.insightflow.aiopscore.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OrchestrationTargets(
        String policy,
        @JsonProperty("rate_limit")
        String rateLimit,
        String provider
) {
}
