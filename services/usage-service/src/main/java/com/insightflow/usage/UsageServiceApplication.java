package com.insightflow.usage;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.insightflow.common.api.ApiResponse;
import com.insightflow.common.api.ApiResponses;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = {"com.insightflow.usage", "com.insightflow.common"})
public class UsageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UsageServiceApplication.class, args);
    }
}

@RestController
class UsageController {

    @GetMapping("/health")
    public ApiResponse<StatusResponse> health() {
        return ApiResponses.ok(new StatusResponse("usage-service", "UP"));
    }

    @GetMapping("/api/usage/users/{userId}")
    public ApiResponse<UsageResponse> getUserUsage(@PathVariable String userId) {
        return ApiResponses.ok(new UsageResponse(userId, 0, List.of()));
    }

    record StatusResponse(String service, String status) {
    }

    record UsageResponse(
            @JsonProperty("user_id")
            String userId,
            @JsonProperty("total_requests")
            int totalRequests,
            List<String> items
    ) {
    }
}
