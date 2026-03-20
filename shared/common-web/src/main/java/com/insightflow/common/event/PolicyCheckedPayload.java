package com.insightflow.common.event;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PolicyCheckedPayload(
        @JsonProperty("request_id")
        String requestId,
        String scope,
        @JsonProperty("scope_id")
        String scopeId,
        String result,
        @JsonProperty("rules_applied")
        List<String> rulesApplied
) {
}
