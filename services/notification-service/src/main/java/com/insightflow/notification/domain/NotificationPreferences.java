package com.insightflow.notification.domain;

import java.util.List;

public record NotificationPreferences(
        String userId,
        String teamId,
        List<NotificationSubscriptionPreference> subscriptions
) {
    public NotificationPreferences {
        subscriptions = List.copyOf(subscriptions);
    }
}
