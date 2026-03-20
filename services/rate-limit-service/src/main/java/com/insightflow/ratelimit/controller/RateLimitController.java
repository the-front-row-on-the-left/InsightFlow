package com.insightflow.ratelimit.controller;

import java.util.Map;

import com.insightflow.ratelimit.controller.dto.RateLimitCheckRequest;
import com.insightflow.ratelimit.controller.dto.RateLimitCheckResponse;
import com.insightflow.ratelimit.service.RateLimitService;
import com.insightflow.common.api.ApiResponse;
import com.insightflow.common.api.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RateLimitController {

    private final RateLimitService rateLimitService;

    RateLimitController(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @PostMapping("/internal/rate-limit/check")
    public ApiResponse<RateLimitCheckResponse> checkRateLimit(@RequestBody RateLimitCheckRequest request) {
        return ApiResponses.ok(rateLimitService.check(request));
    }

    @GetMapping("/internal/rate-limit/counters")
    public ApiResponse<Map<String, Object>> getCounters() {
        return ApiResponses.ok(rateLimitService.counters());
    }
}
