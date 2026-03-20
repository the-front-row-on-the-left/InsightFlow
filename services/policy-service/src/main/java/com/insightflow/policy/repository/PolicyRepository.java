package com.insightflow.policy.repository;

import java.util.List;

import com.insightflow.policy.config.PolicyProperties;
import com.insightflow.policy.domain.PolicySummary;
import org.springframework.stereotype.Repository;

@Repository
public class PolicyRepository {

    private final PolicyProperties properties;

    public PolicyRepository(PolicyProperties properties) {
        this.properties = properties;
    }

    public List<PolicySummary> findAll() {
        return List.of(
                new PolicySummary("pol_team_model_deny", "TEAM_MODEL_DENY", "team", properties.blockedTeamId(), "active"),
                new PolicySummary("pol_team_budget_limit", "TEAM_MONTHLY_BUDGET", "team", properties.budgetBlockedTeamId(), "active"),
                new PolicySummary("pol_service_allowlist", "SERVICE_ALLOWLIST", "global", "all", "active")
        );
    }
}
