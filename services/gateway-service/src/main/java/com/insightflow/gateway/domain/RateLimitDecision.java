package com.insightflow.gateway.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RateLimitDecision(
        boolean allowed,
        String scope,
        @JsonProperty("scope_id")
        String scopeId,
        @JsonProperty("remaining_quota")
        int remainingQuota,
        String result,
        @JsonProperty("applied_rule")
        String appliedRule
) {
}
