package com.insightflow.notification.repository;

import com.insightflow.notification.domain.NotificationPreferences;

public interface NotificationPreferencesRepository {

    NotificationPreferences findByContext(String userId, String teamId);
}
