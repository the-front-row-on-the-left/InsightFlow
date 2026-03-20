package com.insightflow.policy.service;

import java.util.List;

import com.insightflow.policy.controller.dto.PolicyEvaluationRequest;
import com.insightflow.policy.controller.dto.PolicyEvaluationResponse;
import com.insightflow.policy.config.PolicyProperties;
import com.insightflow.policy.domain.PolicySummary;
import com.insightflow.policy.event.PolicyEventPublisher;
import com.insightflow.policy.repository.PolicyRepository;
import com.insightflow.common.error.BusinessException;
import com.insightflow.common.error.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class PolicyService {

    private final PolicyProperties properties;
    private final PolicyRepository policyRepository;
    private final PolicyEventPublisher eventPublisher;

    PolicyService(PolicyProperties properties,
                  PolicyRepository policyRepository,
                  PolicyEventPublisher eventPublisher) {
        this.properties = properties;
        this.policyRepository = policyRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<PolicySummary> summaries() {
        return policyRepository.findAll();
    }

    public PolicyEvaluationResponse evaluate(PolicyEvaluationRequest request) {
        validate(request);

        PolicyEvaluationResponse response;
        if (!properties.allowedServices().contains(request.serviceId())) {
            response = new PolicyEvaluationResponse(false, "SERVICE_NOT_ALLOWED", "SERVICE_ALLOWLIST", List.of("SERVICE_ALLOWLIST"));
        } else if (properties.blockedTeamId().equals(request.teamId()) && properties.blockedModel().equalsIgnoreCase(request.model())) {
            response = new PolicyEvaluationResponse(false, "TEAM_MODEL_DENY", "TEAM_MODEL_DENY",
                    List.of("SERVICE_ALLOWLIST", "TEAM_MODEL_DENY"));
        } else if (properties.budgetBlockedTeamId().equals(request.teamId())) {
            response = new PolicyEvaluationResponse(false, "TEAM_MONTHLY_BUDGET_EXCEEDED", "TEAM_MONTHLY_BUDGET",
                    List.of("SERVICE_ALLOWLIST", "TEAM_MONTHLY_BUDGET"));
        } else {
            response = new PolicyEvaluationResponse(true, null, "DEFAULT_ALLOW",
                    List.of("SERVICE_ALLOWLIST", "DEFAULT_ALLOW"));
        }

        eventPublisher.publish(request.teamId(), request, response);
        return response;
    }

    private void validate(PolicyEvaluationRequest request) {
        if (request == null || isBlank(request.requestId()) || isBlank(request.serviceId())
                || isBlank(request.userId()) || isBlank(request.teamId()) || isBlank(request.model())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST,
                    "request_id, service_id, user_id, team_id, and model are required.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
