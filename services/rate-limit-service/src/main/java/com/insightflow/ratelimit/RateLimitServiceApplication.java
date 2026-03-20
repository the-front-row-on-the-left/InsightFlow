package com.insightflow.ratelimit;

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
public class RateLimitServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RateLimitServiceApplication.class, args);
    }
}

@RestController
class RateLimitController {

    @GetMapping("/health")
    public ApiResponse<StatusResponse> health() {
        return ApiResponses.ok(new StatusResponse("rate-limit-service", "UP"));
    }

    @PostMapping("/internal/rate-limit/check")
    public ApiResponse<RateLimitCheckResponse> checkRateLimit(@RequestBody RateLimitCheckRequest request) {
        return ApiResponses.ok(new RateLimitCheckResponse(true, 100, "PASSED"));
    }

    record StatusResponse(String service, String status) {
    }

    record RateLimitCheckRequest(
            String scope,
            @JsonProperty("scope_id")
            String scopeId
    ) {
    }

    record RateLimitCheckResponse(
            boolean allowed,
            @JsonProperty("remaining_quota")
            int remainingQuota,
            String result
    ) {
    }
}
