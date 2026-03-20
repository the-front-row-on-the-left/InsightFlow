package com.insightflow.usage.controller;

import com.insightflow.common.api.ApiResponse;
import com.insightflow.common.api.ApiResponses;
import com.insightflow.usage.dto.UsageScopeResponse;
import com.insightflow.usage.dto.UsageStatusResponse;
import com.insightflow.usage.service.UsageQueryService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ApiResponse<UsageScopeResponse> getUserUsage(
            @PathVariable("userId") String userId,
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(name = "unit", required = false) String unit
    ) {
        return ApiResponses.ok(usageQueryService.getUserUsage(userId, from, to, unit));
    }

    @GetMapping("/api/usage/teams/{teamId}")
    public ApiResponse<UsageScopeResponse> getTeamUsage(
            @PathVariable("teamId") String teamId,
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(name = "unit", required = false) String unit
    ) {
        return ApiResponses.ok(usageQueryService.getTeamUsage(teamId, from, to, unit));
    }

    @GetMapping("/api/usage/services/{serviceId}")
    public ApiResponse<UsageScopeResponse> getServiceUsage(
            @PathVariable("serviceId") String serviceId,
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(name = "unit", required = false) String unit
    ) {
        return ApiResponses.ok(usageQueryService.getServiceUsage(serviceId, from, to, unit));
    }
}
