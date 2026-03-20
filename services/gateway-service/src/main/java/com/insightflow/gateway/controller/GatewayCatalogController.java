package com.insightflow.gateway.controller;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.insightflow.common.api.ApiResponse;
import com.insightflow.common.api.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class GatewayCatalogController {

    @GetMapping("/api/catalog/services")
    public ApiResponse<List<ServiceCatalogItem>> getCatalogServices(@RequestParam(required = false) String category,
                                                                    @RequestParam(required = false) String tag,
                                                                    @RequestParam(required = false) String keyword) {
        return ApiResponses.ok(List.of(
                new ServiceCatalogItem("svc_doc_summary", "Document Summary", "analysis", "per_token"),
                new ServiceCatalogItem("svc_report_generator", "Report Generator", "report", "per_request")
        ));
    }

    @GetMapping("/api/catalog/services/{serviceId}")
    public ApiResponse<ServiceDetail> getCatalogService(@PathVariable String serviceId) {
        return ApiResponses.ok(new ServiceDetail(
                serviceId,
                "Sample Service",
                "analysis",
                "per_token",
                List.of("gpt-4o-mini", "gpt-4.1-mini"),
                "Gateway-backed catalog stub for platform integration."
        ));
    }

    @PostMapping("/api/workflows")
    public ApiResponse<WorkflowResponse> createWorkflow(@RequestBody WorkflowCreateRequest request) {
        return ApiResponses.ok(new WorkflowResponse(
                "wf_" + UUID.randomUUID(),
                request.name(),
                request.steps()
        ));
    }

    @GetMapping("/api/workflows/{workflowId}")
    public ApiResponse<WorkflowResponse> getWorkflow(@PathVariable String workflowId) {
        return ApiResponses.ok(new WorkflowResponse(
                workflowId,
                "Sample Workflow",
                List.of(new WorkflowStep("svc_doc_summary"), new WorkflowStep("svc_report_generator"))
        ));
    }

    record ServiceCatalogItem(
            @JsonProperty("service_id")
            String serviceId,
            String name,
            String category,
            @JsonProperty("pricing_model")
            String pricingModel
    ) {
    }

    record ServiceDetail(
            @JsonProperty("service_id")
            String serviceId,
            String name,
            String category,
            @JsonProperty("pricing_model")
            String pricingModel,
            @JsonProperty("supported_models")
            List<String> supportedModels,
            String description
    ) {
    }

    record WorkflowCreateRequest(
            String name,
            List<WorkflowStep> steps
    ) {
    }

    record WorkflowResponse(
            @JsonProperty("workflow_id")
            String workflowId,
            String name,
            List<WorkflowStep> steps
    ) {
    }

    record WorkflowStep(
            @JsonProperty("service_id")
            String serviceId
    ) {
    }
}
