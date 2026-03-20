package com.insightflow.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NotificationSubscription(
        @JsonProperty("event_type")
        String eventType,
        String channel,
        String status
) {
}
