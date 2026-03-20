package com.insightflow.policy.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PolicyEvaluationRequest(
        @JsonProperty("request_id")
        String requestId,
        @JsonProperty("service_id")
        String serviceId,
        @JsonProperty("workflow_id")
        String workflowId,
        @JsonProperty("team_id")
        String teamId,
        @JsonProperty("user_id")
        String userId,
        String model
) {
}
