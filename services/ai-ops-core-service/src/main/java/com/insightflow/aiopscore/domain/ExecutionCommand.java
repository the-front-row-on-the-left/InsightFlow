package com.insightflow.aiopscore.domain;

import java.util.Map;

public record ExecutionCommand(
        String executionId,
        String requestId,
        String userId,
        String teamId,
        String userRole,
        String serviceId,
        String workflowId,
        String model,
        Map<String, Object> input
) {
    public int inputSize() {
        return input.toString().length();
    }
}
