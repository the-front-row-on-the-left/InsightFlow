package com.insightflow.notification.domain;

public record NotificationSubscriptionPreference(
        String eventType,
        NotificationChannel channel,
        NotificationPreferenceStatus status
) {
    public boolean isActiveFor(String candidateEventType) {
        return eventType.equals(candidateEventType) && status.isActive();
    }
}
