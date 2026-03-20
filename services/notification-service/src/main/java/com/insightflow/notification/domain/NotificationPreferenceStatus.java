package com.insightflow.notification.domain;

public enum NotificationPreferenceStatus {
    ACTIVE("active"),
    MUTED("muted");

    private final String apiValue;

    NotificationPreferenceStatus(String apiValue) {
        this.apiValue = apiValue;
    }

    public String apiValue() {
        return apiValue;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }
}
