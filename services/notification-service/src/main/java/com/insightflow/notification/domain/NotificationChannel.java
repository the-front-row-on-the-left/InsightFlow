package com.insightflow.notification.domain;

public enum NotificationChannel {
    TEAM_DIGEST("team_digest"),
    USER_INBOX("user_inbox");

    private final String apiValue;

    NotificationChannel(String apiValue) {
        this.apiValue = apiValue;
    }

    public String apiValue() {
        return apiValue;
    }
}
