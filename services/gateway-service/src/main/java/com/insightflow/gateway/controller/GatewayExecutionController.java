package com.insightflow.gateway.controller;

import com.insightflow.common.api.ApiResponse;
import com.insightflow.common.api.ApiResponses;
import com.insightflow.common.web.InsightRequestContextHolder;
import com.insightflow.common.web.RequestContext;
import com.insightflow.gateway.client.AiOpsCoreServiceClient;
import com.insightflow.gateway.domain.ExecutionCreateRequest;
import com.insightflow.gateway.domain.ExecutionCreateResponse;
import com.insightflow.gateway.domain.ExecutionDetailResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GatewayExecutionController {

    private final AiOpsCoreServiceClient aiOpsCoreServiceClient;

    GatewayExecutionController(AiOpsCoreServiceClient aiOpsCoreServiceClient) {
        this.aiOpsCoreServiceClient = aiOpsCoreServiceClient;
    }

    @PostMapping("/api/executions")
    public ApiResponse<ExecutionCreateResponse> createExecution(@RequestBody ExecutionCreateRequest request) {
        RequestContext context = currentContext();
        return ApiResponses.ok(aiOpsCoreServiceClient.createExecution(
                request,
                context.requestId(),
                context.userId(),
                context.teamId(),
                context.userRole()
        ));
    }

    @GetMapping("/api/executions/{executionId}")
    public ApiResponse<ExecutionDetailResponse> getExecution(@PathVariable String executionId) {
        RequestContext context = currentContext();
        return ApiResponses.ok(aiOpsCoreServiceClient.getExecution(
                executionId,
                context.requestId(),
                context.userId(),
                context.teamId(),
                context.userRole()
        ));
    }

    @GetMapping("/internal/executions")
    public ApiResponse<Object> getExecutionOperations() {
        RequestContext context = currentContext();
        return ApiResponses.ok(aiOpsCoreServiceClient.operationsSnapshot(
                context.requestId(),
                context.userId(),
                context.teamId(),
                context.userRole()
        ));
    }

    private RequestContext currentContext() {
        return InsightRequestContextHolder.getCurrent()
                .orElseThrow(() -> new IllegalStateException("Missing request context."));
    }
}
