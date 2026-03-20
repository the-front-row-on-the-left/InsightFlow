package com.insightflow.notification.dto;

public record NotificationStatusResponse(
        String service,
        String status
) {
}
