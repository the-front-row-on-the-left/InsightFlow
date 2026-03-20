package com.insightflow.gateway;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.insightflow.common.api.ApiResponse;
import com.insightflow.common.api.ApiResponses;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = {"com.insightflow.gateway", "com.insightflow.common"})
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }

    @Bean
    RestClient restClient(RestClient.Builder builder) {
        return builder.build();
    }
}

@RestController
class GatewayHealthController {

    @GetMapping("/health")
    public ApiResponse<HealthResponse> health() {
        return ApiResponses.ok(new HealthResponse("gateway-service", "UP"));
    }

    record HealthResponse(
            String service,
            String status,
            @JsonProperty("api_base_path")
            String apiBasePath
    ) {
        HealthResponse(String service, String status) {
            this(service, status, "/api");
        }
    }
}
