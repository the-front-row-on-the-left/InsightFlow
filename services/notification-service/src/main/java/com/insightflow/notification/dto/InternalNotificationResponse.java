package com.insightflow.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record InternalNotificationResponse(
        @JsonProperty("notification_id")
        String notificationId,
        @JsonProperty("request_id")
        String requestId,
        @JsonProperty("event_type")
        String eventType,
        @JsonProperty("recipient_type")
        String recipientType,
        @JsonProperty("recipient_id")
        String recipientId,
        String channel,
        String title,
        String message,
        String status,
        @JsonProperty("occurred_at")
        String occurredAt,
        Map<String, String> metadata
) {
}
