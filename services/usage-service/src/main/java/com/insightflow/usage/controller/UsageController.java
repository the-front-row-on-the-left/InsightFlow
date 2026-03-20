package com.insightflow.usage.controller;

import com.insightflow.common.api.ApiResponse;
import com.insightflow.common.api.ApiResponses;
import com.insightflow.usage.dto.UsageScopeResponse;
import com.insightflow.usage.dto.UsageStatusResponse;
import com.insightflow.usage.service.UsageQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UsageController {

    private final UsageQueryService usageQueryService;

    public UsageController(UsageQueryService usageQueryService) {
        this.usageQueryService = usageQueryService;
    }

    @GetMapping("/health")
    public ApiResponse<UsageStatusResponse> health() {
        return ApiResponses.ok(new UsageStatusResponse("usage-service", "UP"));
    }

    @GetMapping("/api/usage/users/{userId}")
    public ApiResponse<UsageScopeResponse> getUserUsage(@PathVariable String userId) {
        return ApiResponses.ok(usageQueryService.getUserUsage(userId));
    }

    @GetMapping("/api/usage/teams/{teamId}")
    public ApiResponse<UsageScopeResponse> getTeamUsage(@PathVariable String teamId) {
        return ApiResponses.ok(usageQueryService.getTeamUsage(teamId));
    }

    @GetMapping("/api/usage/services/{serviceId}")
    public ApiResponse<UsageScopeResponse> getServiceUsage(@PathVariable String serviceId) {
        return ApiResponses.ok(usageQueryService.getServiceUsage(serviceId));
    }
}
