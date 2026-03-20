package com.insightflow.notification;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.insightflow.common.api.ApiResponse;
import com.insightflow.common.api.ApiResponses;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = "com.insightflow")
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}

@RestController
class NotificationController {

    @GetMapping("/health")
    public ApiResponse<StatusResponse> health() {
        return ApiResponses.ok(new StatusResponse("notification-service", "UP"));
    }

    @GetMapping("/internal/notifications/subscriptions")
    public ApiResponse<List<NotificationSubscription>> getSubscriptions() {
        return ApiResponses.ok(List.of(
                new NotificationSubscription("optimization.recommended", "team_digest", "active")
        ));
    }

    record StatusResponse(String service, String status) {
    }

    record NotificationSubscription(
            @JsonProperty("event_type")
            String eventType,
            String channel,
            String status
    ) {
    }
}
