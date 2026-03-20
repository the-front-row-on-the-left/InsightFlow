package com.insightflow.aiopscore.domain;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExecutionCreateRequest(
        @JsonProperty("service_id")
        String serviceId,
        @JsonProperty("workflow_id")
        String workflowId,
        Map<String, Object> input,
        String model
) {
}
