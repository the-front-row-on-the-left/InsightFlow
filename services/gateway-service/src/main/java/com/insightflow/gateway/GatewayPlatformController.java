package com.insightflow.gateway;

import com.insightflow.common.api.ApiResponse;
import com.insightflow.common.api.ApiResponses;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(
        origins = {
                "http://127.0.0.1:4173",
                "http://localhost:4173",
                "http://127.0.0.1:5173",
                "http://localhost:5173"
        },
        allowedHeaders = "*"
)
class GatewayPlatformController {

    private final GatewayPlatformService gatewayPlatformService;

    GatewayPlatformController(GatewayPlatformService gatewayPlatformService) {
        this.gatewayPlatformService = gatewayPlatformService;
    }

    @GetMapping("/api/catalog/services")
    ApiResponse<List<ServiceCatalogItemResponse>> getCatalogServices(@RequestParam(required = false) String category,
                                                                    @RequestParam(required = false) String tag,
                                                                    @RequestParam(required = false) String keyword) {
        return ApiResponses.ok(gatewayPlatformService.listCatalogServices(category, tag, keyword));
    }

    @GetMapping("/api/catalog/services/{serviceId}")
    ApiResponse<ServiceDetailResponse> getCatalogService(@PathVariable String serviceId) {
        return ApiResponses.ok(gatewayPlatformService.getCatalogService(serviceId));
    }

    @PostMapping("/api/workflows")
    ApiResponse<WorkflowRecord> createWorkflow(@RequestBody WorkflowCreateRequest request) {
        return ApiResponses.ok(gatewayPlatformService.createWorkflow(request));
    }

    @GetMapping("/api/workflows/{workflowId}")
    ApiResponse<WorkflowRecord> getWorkflow(@PathVariable String workflowId) {
        return ApiResponses.ok(gatewayPlatformService.getWorkflow(workflowId));
    }

    @PostMapping("/api/executions")
    ApiResponse<ExecutionCreateResponse> createExecution(@RequestBody ExecutionCreateRequest request) {
        return ApiResponses.ok(gatewayPlatformService.createExecution(request));
    }

    @GetMapping("/api/executions/{executionId}")
    ApiResponse<ExecutionRecord> getExecution(@PathVariable String executionId) {
        return ApiResponses.ok(gatewayPlatformService.getExecution(executionId));
    }
}
