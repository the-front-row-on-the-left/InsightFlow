package com.insightflow.common.event;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiRequestedPayload(
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
        String model,
        @JsonProperty("input_size")
        int inputSize
) {
}
