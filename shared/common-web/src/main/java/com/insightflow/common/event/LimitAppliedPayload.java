package com.insightflow.common.event;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LimitAppliedPayload(
        @JsonProperty("request_id")
        String requestId,
        String scope,
        @JsonProperty("scope_id")
        String scopeId,
        String result,
        @JsonProperty("remaining_quota")
        int remainingQuota
) {
}
