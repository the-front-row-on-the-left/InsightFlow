package com.insightflow.recommendation;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.insightflow.common.api.ApiResponse;
import com.insightflow.common.api.ApiResponses;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = "com.insightflow")
public class RecommendationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecommendationServiceApplication.class, args);
    }
}

@RestController
class RecommendationController {

    @GetMapping("/health")
    public ApiResponse<StatusResponse> health() {
        return ApiResponses.ok(new StatusResponse("recommendation-service", "UP"));
    }

    @GetMapping("/api/recommendations")
    public ApiResponse<RecommendationResponse> getRecommendations(@RequestParam("user_id") String userId) {
        return ApiResponses.ok(new RecommendationResponse(userId, List.of()));
    }

    record StatusResponse(String service, String status) {
    }

    record RecommendationResponse(
            @JsonProperty("user_id")
            String userId,
            List<String> recommendations
    ) {
    }
}
