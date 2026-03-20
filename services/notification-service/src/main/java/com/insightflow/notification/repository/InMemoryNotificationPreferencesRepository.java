package com.insightflow.notification.repository;

import com.insightflow.notification.config.NotificationContextDefaults;
import com.insightflow.notification.domain.NotificationChannel;
import com.insightflow.notification.domain.NotificationPreferenceStatus;
import com.insightflow.notification.domain.NotificationPreferences;
import com.insightflow.notification.domain.NotificationSubscriptionPreference;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryNotificationPreferencesRepository implements NotificationPreferencesRepository {

    private static final List<NotificationSubscriptionPreference> DEFAULT_SUBSCRIPTIONS = List.of(
            new NotificationSubscriptionPreference(
                    "optimization.recommended",
                    NotificationChannel.TEAM_DIGEST,
                    NotificationPreferenceStatus.ACTIVE
            ),
            new NotificationSubscriptionPreference(
                    "cost.calculated",
                    NotificationChannel.TEAM_DIGEST,
                    NotificationPreferenceStatus.ACTIVE
            ),
            new NotificationSubscriptionPreference(
                    "optimization.recommended",
                    NotificationChannel.USER_INBOX,
                    NotificationPreferenceStatus.ACTIVE
            )
    );

    private final NotificationContextDefaults notificationContextDefaults;

    public InMemoryNotificationPreferencesRepository(NotificationContextDefaults notificationContextDefaults) {
        this.notificationContextDefaults = notificationContextDefaults;
    }

    @Override
    public NotificationPreferences findByContext(String userId, String teamId) {
        return new NotificationPreferences(
                resolveUserId(userId),
                resolveTeamId(teamId),
                DEFAULT_SUBSCRIPTIONS
        );
    }

    private String resolveUserId(String userId) {
        return hasText(userId) ? userId : notificationContextDefaults.defaultUserId();
    }

    private String resolveTeamId(String teamId) {
        return hasText(teamId) ? teamId : notificationContextDefaults.defaultTeamId();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
