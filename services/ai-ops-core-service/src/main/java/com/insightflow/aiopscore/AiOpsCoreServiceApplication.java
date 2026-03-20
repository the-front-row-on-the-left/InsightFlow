package com.insightflow.aiopscore;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.insightflow.aiopscore.config.AiOpsCoreProperties;
import com.insightflow.common.api.ApiResponse;
import com.insightflow.common.api.ApiResponses;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@SpringBootApplication(scanBasePackages = {"com.insightflow.aiopscore", "com.insightflow.common"})
public class AiOpsCoreServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiOpsCoreServiceApplication.class, args);
    }

    @Bean
    RestClient restClient(RestClient.Builder builder) {
        return builder.build();
    }

    @Bean
    NewTopic aiRequestedTopic(AiOpsCoreProperties properties) {
        return TopicBuilder.name(properties.aiRequestedTopic())
                .partitions(1)
                .replicas(1)
                .build();
    }
}

@RestController
class AiOpsCoreHealthController {

    @GetMapping("/health")
    public ApiResponse<HealthResponse> health() {
        return ApiResponses.ok(new HealthResponse("ai-ops-core-service", "UP"));
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
