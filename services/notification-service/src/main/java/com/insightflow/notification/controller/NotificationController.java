package com.insightflow.notification.controller;

import com.insightflow.common.api.ApiResponse;
import com.insightflow.common.api.ApiResponses;
import com.insightflow.notification.dto.NotificationStatusResponse;
import com.insightflow.notification.dto.NotificationSubscription;
import com.insightflow.notification.service.NotificationQueryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController {

    private final NotificationQueryService notificationQueryService;

    public NotificationController(NotificationQueryService notificationQueryService) {
        this.notificationQueryService = notificationQueryService;
    }

    @GetMapping("/health")
    public ApiResponse<NotificationStatusResponse> health() {
        return ApiResponses.ok(new NotificationStatusResponse("notification-service", "UP"));
    }

    @GetMapping("/internal/notifications/subscriptions")
    public ApiResponse<List<NotificationSubscription>> getSubscriptions() {
        return ApiResponses.ok(notificationQueryService.getSubscriptions());
    }
}
