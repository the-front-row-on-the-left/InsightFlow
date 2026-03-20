package com.insightflow.notification.service;

import com.insightflow.notification.config.NotificationContextDefaults;
import com.insightflow.notification.domain.InternalNotification;
import com.insightflow.notification.domain.NotificationPreferences;
import com.insightflow.notification.dto.InternalNotificationResponse;
import com.insightflow.notification.dto.NotificationSubscription;
import com.insightflow.notification.repository.InternalNotificationRepository;
import com.insightflow.notification.repository.NotificationPreferencesRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class NotificationQueryService {

    private final NotificationPreferencesRepository notificationPreferencesRepository;
    private final InternalNotificationRepository internalNotificationRepository;
    private final NotificationContextDefaults notificationContextDefaults;

    public NotificationQueryService(
            NotificationPreferencesRepository notificationPreferencesRepository,
            InternalNotificationRepository internalNotificationRepository,
            NotificationContextDefaults notificationContextDefaults
    ) {
        this.notificationPreferencesRepository = notificationPreferencesRepository;
        this.internalNotificationRepository = internalNotificationRepository;
        this.notificationContextDefaults = notificationContextDefaults;
    }

    public List<NotificationSubscription> getSubscriptions() {
        return getSubscriptions(
                notificationContextDefaults.defaultUserId(),
                notificationContextDefaults.defaultTeamId()
        );
    }

    public List<NotificationSubscription> getSubscriptions(String userId, String teamId) {
        NotificationPreferences preferences = notificationPreferencesRepository.findByContext(
                resolveUserId(userId),
                resolveTeamId(teamId)
        );

        return preferences.subscriptions().stream()
                .map(preference -> new NotificationSubscription(
                        preference.eventType(),
                        preference.channel().apiValue(),
                        preference.status().apiValue()
                ))
                .toList();
    }

    public List<InternalNotificationResponse> getNotifications() {
        return getNotifications(
                notificationContextDefaults.defaultUserId(),
                notificationContextDefaults.defaultTeamId()
        );
    }

    public List<InternalNotificationResponse> getNotifications(String userId, String teamId) {
        return internalNotificationRepository.findByContext(resolveUserId(userId), resolveTeamId(teamId)).stream()
                .map(this::toResponse)
                .toList();
    }

    private InternalNotificationResponse toResponse(InternalNotification notification) {
        return new InternalNotificationResponse(
                notification.notificationId(),
                notification.requestId(),
                notification.eventType(),
                notification.recipientType(),
                notification.recipientId(),
                notification.channel().apiValue(),
                notification.title(),
                notification.message(),
                notification.status(),
                notification.occurredAt().toString(),
                notification.metadata()
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
