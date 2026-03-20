package com.insightflow.aiopscore.controller;

import com.insightflow.aiopscore.domain.ExecutionCreateRequest;
import com.insightflow.aiopscore.domain.ExecutionCreateResponse;
import com.insightflow.aiopscore.domain.ExecutionDetailResponse;
import com.insightflow.aiopscore.service.AiOpsCoreExecutionService;
import com.insightflow.common.api.ApiResponse;
import com.insightflow.common.api.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiOpsCoreExecutionController {

    private final AiOpsCoreExecutionService executionService;

    AiOpsCoreExecutionController(AiOpsCoreExecutionService executionService) {
        this.executionService = executionService;
    }

    @PostMapping("/api/executions")
    public ApiResponse<ExecutionCreateResponse> createExecution(@RequestBody ExecutionCreateRequest request) {
        return ApiResponses.ok(executionService.createExecution(request));
    }

    @GetMapping("/api/executions/{executionId}")
    public ApiResponse<ExecutionDetailResponse> getExecution(@PathVariable String executionId) {
        return ApiResponses.ok(executionService.getExecution(executionId));
    }

    @GetMapping("/internal/executions")
    public ApiResponse<Object> getExecutionOperations() {
        return ApiResponses.ok(executionService.operationsSnapshot());
    }
}
