package com.insightflow.policy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.insightflow.common.api.ApiResponse;
import com.insightflow.common.api.ApiResponses;
import com.insightflow.policy.config.PolicyProperties;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = {"com.insightflow.policy", "com.insightflow.common"})
public class PolicyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PolicyServiceApplication.class, args);
    }

    @Bean
    NewTopic policyCheckedTopic(PolicyProperties properties) {
        return TopicBuilder.name(properties.policyCheckedTopic())
                .partitions(1)
                .replicas(1)
                .build();
    }
}

@RestController
class PolicyHealthController {

    @GetMapping("/health")
    public ApiResponse<StatusResponse> health() {
        return ApiResponses.ok(new StatusResponse("policy-service", "UP"));
    }

    record StatusResponse(
            String service,
            String status,
            @JsonProperty("evaluation_mode")
            String evaluationMode
    ) {
        StatusResponse(String service, String status) {
            this(service, status, "in-memory-rules");
        }
    }
}
