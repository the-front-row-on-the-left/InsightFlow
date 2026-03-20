package com.insightflow.billing.controller;

import com.insightflow.billing.dto.BillingScopeResponse;
import com.insightflow.billing.dto.BillingStatusResponse;
import com.insightflow.billing.dto.PricingTableResponse;
import com.insightflow.billing.service.BillingQueryService;
import com.insightflow.common.api.ApiResponse;
import com.insightflow.common.api.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BillingController {

    private final BillingQueryService billingQueryService;

    public BillingController(BillingQueryService billingQueryService) {
        this.billingQueryService = billingQueryService;
    }

    @GetMapping("/health")
    public ApiResponse<BillingStatusResponse> health() {
        return ApiResponses.ok(new BillingStatusResponse("billing-service", "UP"));
    }

    @GetMapping("/api/billing/users/{userId}")
    public ApiResponse<BillingScopeResponse> getUserBilling(@PathVariable String userId) {
        return ApiResponses.ok(billingQueryService.getUserBilling(userId));
    }

    @GetMapping("/api/billing/teams/{teamId}")
    public ApiResponse<BillingScopeResponse> getTeamBilling(@PathVariable String teamId) {
        return ApiResponses.ok(billingQueryService.getTeamBilling(teamId));
    }

    @GetMapping("/api/billing/workflows/{workflowId}")
    public ApiResponse<BillingScopeResponse> getWorkflowBilling(@PathVariable String workflowId) {
        return ApiResponses.ok(billingQueryService.getWorkflowBilling(workflowId));
    }

    @GetMapping("/api/billing/pricing-tables/{version}")
    public ApiResponse<PricingTableResponse> getPricingTable(@PathVariable String version) {
        return ApiResponses.ok(billingQueryService.getPricingTable(version));
    }
}
