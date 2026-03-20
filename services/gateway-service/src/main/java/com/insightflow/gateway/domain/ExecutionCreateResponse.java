package com.insightflow.gateway.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExecutionCreateResponse(
        @JsonProperty("execution_id")
        String executionId,
        @JsonProperty("request_id")
        String requestId,
        @JsonProperty("service_id")
        String serviceId,
        @JsonProperty("workflow_id")
        String workflowId,
        String status,
        @JsonProperty("orchestration_targets")
        OrchestrationTargets orchestrationTargets,
        ExecutionResult result
) {
}
