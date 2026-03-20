package com.insightflow.ratelimit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.insightflow.common.api.ApiResponse;
import com.insightflow.common.api.ApiResponses;
import com.insightflow.ratelimit.config.RateLimitProperties;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = {"com.insightflow.ratelimit", "com.insightflow.common"})
public class RateLimitServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RateLimitServiceApplication.class, args);
    }

    @Bean
    NewTopic limitAppliedTopic(RateLimitProperties properties) {
        return TopicBuilder.name(properties.limitAppliedTopic())
                .partitions(1)
                .replicas(1)
                .build();
    }
}

@RestController
class RateLimitHealthController {

    private final RateLimitProperties properties;

    RateLimitHealthController(RateLimitProperties properties) {
        this.properties = properties;
    }

    @GetMapping("/health")
    public ApiResponse<StatusResponse> health() {
        return ApiResponses.ok(new StatusResponse("rate-limit-service", "UP", properties.repositoryType()));
    }

    record StatusResponse(
            String service,
            String status,
            @JsonProperty("counter_mode")
            String counterMode
    ) {
    }
}
