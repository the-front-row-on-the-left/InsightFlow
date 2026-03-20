package com.insightflow.billing;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.insightflow.common.api.ApiResponse;
import com.insightflow.common.api.ApiResponses;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = "com.insightflow")
public class BillingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BillingServiceApplication.class, args);
    }
}

@RestController
class BillingController {

    @GetMapping("/health")
    public ApiResponse<StatusResponse> health() {
        return ApiResponses.ok(new StatusResponse("billing-service", "UP"));
    }

    @GetMapping("/api/billing/teams/{teamId}")
    public ApiResponse<BillingResponse> getTeamBilling(@PathVariable String teamId) {
        return ApiResponses.ok(new BillingResponse(teamId, "KRW", "0.00", List.of()));
    }

    record StatusResponse(String service, String status) {
    }

    record BillingResponse(
            @JsonProperty("team_id")
            String teamId,
            String currency,
            @JsonProperty("total_cost")
            String totalCost,
            List<String> items
    ) {
    }
}
