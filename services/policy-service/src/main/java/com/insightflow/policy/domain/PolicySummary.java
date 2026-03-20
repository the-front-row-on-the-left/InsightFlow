package com.insightflow.policy.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PolicySummary(
        @JsonProperty("policy_id")
        String policyId,
        String name,
        @JsonProperty("scope_type")
        String scopeType,
        @JsonProperty("scope_id")
        String scopeId,
        String status
) {
}
