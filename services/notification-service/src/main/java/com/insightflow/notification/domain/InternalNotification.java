package com.insightflow.notification.domain;

import java.time.Instant;
import java.util.Map;

public record InternalNotification(
        String notificationId,
        String requestId,
        String eventType,
        NotificationChannel channel,
        String recipientType,
        String recipientId,
        String title,
        String message,
        String status,
        Instant occurredAt,
        Map<String, String> metadata
) {
    public InternalNotification {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public boolean matchesContext(String userId, String teamId) {
        return ("user".equals(recipientType) && recipientId.equals(userId))
                || ("team".equals(recipientType) && recipientId.equals(teamId));
    }
}
