package com.insightflow.policy.controller;

import java.util.List;
import java.util.Map;

import com.insightflow.policy.controller.dto.PolicyEvaluationRequest;
import com.insightflow.policy.controller.dto.PolicyEvaluationResponse;
import com.insightflow.policy.domain.PolicySummary;
import com.insightflow.policy.event.PolicyEventPublisher;
import com.insightflow.policy.service.PolicyService;
import com.insightflow.common.api.ApiResponse;
import com.insightflow.common.api.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PolicyController {

    private final PolicyService policyService;
    private final PolicyEventPublisher eventPublisher;

    PolicyController(PolicyService policyService, PolicyEventPublisher eventPublisher) {
        this.policyService = policyService;
        this.eventPublisher = eventPublisher;
    }

    @GetMapping("/internal/policies")
    public ApiResponse<List<PolicySummary>> getPolicies() {
        return ApiResponses.ok(policyService.summaries());
    }

    @PostMapping("/internal/policies/evaluate")
    public ApiResponse<PolicyEvaluationResponse> evaluatePolicy(@RequestBody PolicyEvaluationRequest request) {
        return ApiResponses.ok(policyService.evaluate(request));
    }

    @GetMapping("/internal/policies/events")
    public ApiResponse<Map<String, Object>> getPublishedEvents() {
        return ApiResponses.ok(Map.of("events", eventPublisher.snapshot()));
    }
}
