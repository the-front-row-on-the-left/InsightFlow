package com.insightflow.policy.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PolicyProperties {

    private final String blockedTeamId;
    private final String blockedModel;
    private final String budgetBlockedTeamId;
    private final List<String> allowedServices;
    private final String policyCheckedTopic;

    PolicyProperties(@Value("${insightflow.policy.team-model-deny.team-id:t_blocked}") String blockedTeamId,
                     @Value("${insightflow.policy.team-model-deny.model:gpt-4o}") String blockedModel,
                     @Value("${insightflow.policy.team-monthly-budget.blocked-team-id:t_budget_blocked}") String budgetBlockedTeamId,
                     @Value("${insightflow.policy.service-allowlist:svc_doc_summary,svc_report_generator}") String allowedServices,
                     @Value("${insightflow.kafka.topics.policy-checked:policy.checked}") String policyCheckedTopic) {
        this.blockedTeamId = blockedTeamId;
        this.blockedModel = blockedModel;
        this.budgetBlockedTeamId = budgetBlockedTeamId;
        this.allowedServices = Arrays.stream(allowedServices.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
        this.policyCheckedTopic = policyCheckedTopic;
    }

    public String blockedTeamId() {
        return blockedTeamId;
    }

    public String blockedModel() {
        return blockedModel;
    }

    public String budgetBlockedTeamId() {
        return budgetBlockedTeamId;
    }

    public List<String> allowedServices() {
        return allowedServices;
    }

    public String policyCheckedTopic() {
        return policyCheckedTopic;
    }
}
