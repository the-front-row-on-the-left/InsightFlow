package com.insightflow.policy;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.insightflow.common.api.ApiResponse;
import com.insightflow.common.api.ApiResponses;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = "com.insightflow")
public class PolicyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PolicyServiceApplication.class, args);
    }
}

@RestController
class PolicyController {

    @GetMapping("/health")
    public ApiResponse<StatusResponse> health() {
        return ApiResponses.ok(new StatusResponse("policy-service", "UP"));
    }

    @GetMapping("/internal/policies")
    public ApiResponse<List<PolicySummary>> getPolicies() {
        return ApiResponses.ok(List.of(
                new PolicySummary("pol_team_model_deny", "TEAM_MODEL_DENY", "team", "t_demo", "active")
        ));
    }

    @PostMapping("/internal/policies/evaluate")
    public ApiResponse<PolicyEvaluationResponse> evaluatePolicy(@RequestBody PolicyEvaluationRequest request) {
        return ApiResponses.ok(new PolicyEvaluationResponse(true, null, "DEFAULT_ALLOW"));
    }

    record StatusResponse(String service, String status) {
    }

    record PolicySummary(
            @JsonProperty("policy_id")
            String policyId,
            String name,
            @JsonProperty("scope_type")
            String scopeType,
            @JsonProperty("scope_id")
            String scopeId,
            String status
    ) {
    }

    record PolicyEvaluationRequest(
            @JsonProperty("service_id")
            String serviceId,
            @JsonProperty("team_id")
            String teamId,
            @JsonProperty("user_id")
            String userId,
            String model
    ) {
    }

    record PolicyEvaluationResponse(
            boolean allowed,
            @JsonProperty("reason_code")
            String reasonCode,
            @JsonProperty("matched_rule")
            String matchedRule
    ) {
    }
}
