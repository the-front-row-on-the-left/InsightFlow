package com.insightflow.ratelimit.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RateLimitCheckRequest(
        @JsonProperty("request_id")
        String requestId,
        @JsonProperty("user_id")
        String userId,
        @JsonProperty("team_id")
        String teamId,
        @JsonProperty("service_id")
        String serviceId,
        @JsonProperty("workflow_id")
        String workflowId,
        String model
) {
}
