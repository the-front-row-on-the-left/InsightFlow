package com.insightflow.policy.controller.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PolicyEvaluationResponse(
        boolean allowed,
        @JsonProperty("reason_code")
        String reasonCode,
        @JsonProperty("matched_rule")
        String matchedRule,
        @JsonProperty("rules_applied")
        List<String> rulesApplied
) {
}
