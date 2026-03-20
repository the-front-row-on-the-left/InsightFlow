package com.insightflow.gateway.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PolicyDecision(
        boolean allowed,
        @JsonProperty("reason_code")
        String reasonCode,
        @JsonProperty("matched_rule")
        String matchedRule
) {
}
