package com.insightflow.notification.service;

import com.insightflow.notification.domain.AnalyticsNotificationEvent;
import com.insightflow.notification.domain.CostCalculatedEvent;
import com.insightflow.notification.domain.InternalNotification;
import com.insightflow.notification.domain.NotificationChannel;
import com.insightflow.notification.domain.NotificationPreferences;
import com.insightflow.notification.domain.NotificationSubscriptionPreference;
import com.insightflow.notification.domain.OptimizationRecommendedEvent;
import com.insightflow.notification.repository.InternalNotificationRepository;
import com.insightflow.notification.repository.NotificationPreferencesRepository;
import org.springframework.stereotype.Service;

@Service
public class NotificationEventConsumerService {

    private final NotificationPreferencesRepository notificationPreferencesRepository;
    private final InternalNotificationRepository internalNotificationRepository;

    public NotificationEventConsumerService(
            NotificationPreferencesRepository notificationPreferencesRepository,
            InternalNotificationRepository internalNotificationRepository
    ) {
        this.notificationPreferencesRepository = notificationPreferencesRepository;
        this.internalNotificationRepository = internalNotificationRepository;
    }

    public int consume(CostCalculatedEvent event) {
        return consumeEvent(event);
    }

    public int consume(OptimizationRecommendedEvent event) {
        return consumeEvent(event);
    }

    private int consumeEvent(AnalyticsNotificationEvent event) {
        NotificationPreferences preferences = notificationPreferencesRepository.findByContext(event.userId(), event.teamId());

        int persistedCount = 0;
        for (NotificationSubscriptionPreference preference : preferences.subscriptions()) {
            if (!preference.isActiveFor(event.eventType())) {
                continue;
            }

            InternalNotification notification = buildNotification(event, preference);
            if (notification != null && internalNotificationRepository.saveIfAbsent(notification)) {
                persistedCount++;
            }
        }

        return persistedCount;
    }

    private InternalNotification buildNotification(
            AnalyticsNotificationEvent event,
            NotificationSubscriptionPreference preference
    ) {
        Recipient recipient = resolveRecipient(event, preference.channel());
        if (recipient == null) {
            return null;
        }

        return new InternalNotification(
                notificationId(event, preference.channel(), recipient),
                event.requestId(),
                event.eventType(),
                preference.channel(),
                recipient.type(),
                recipient.id(),
                titleFor(event),
                messageFor(event),
                "ready",
                event.occurredAt(),
                event.metadata()
        );
    }

    private Recipient resolveRecipient(AnalyticsNotificationEvent event, NotificationChannel channel) {
        if (channel == NotificationChannel.TEAM_DIGEST) {
            return hasText(event.teamId()) ? new Recipient("team", event.teamId()) : null;
        }

        return hasText(event.userId()) ? new Recipient("user", event.userId()) : null;
    }

    private String notificationId(AnalyticsNotificationEvent event, NotificationChannel channel, Recipient recipient) {
        return event.eventType()
                + ":"
                + event.requestId()
                + ":"
                + channel.apiValue()
                + ":"
                + recipient.type()
                + ":"
                + recipient.id();
    }

    private String titleFor(AnalyticsNotificationEvent event) {
        return switch (event.eventType()) {
            case "cost.calculated" -> "Cost calculated for " + valueOrFallback(event.serviceId(), "unknown-service");
            case "optimization.recommended" ->
                    "Optimization recommendation ready for " + valueOrFallback(event.serviceId(), "unknown-service");
            default -> "Analytics notification";
        };
    }

    private String messageFor(AnalyticsNotificationEvent event) {
        if (event instanceof CostCalculatedEvent costCalculatedEvent) {
            return "Request "
                    + costCalculatedEvent.requestId()
                    + " cost "
                    + costCalculatedEvent.currency()
                    + " "
                    + costCalculatedEvent.cost().toPlainString()
                    + " using "
                    + costCalculatedEvent.model();
        }

        if (event instanceof OptimizationRecommendedEvent optimizationRecommendedEvent) {
            return "Switch from "
                    + optimizationRecommendedEvent.currentModel()
                    + " to "
                    + optimizationRecommendedEvent.recommendedModel()
                    + " because "
                    + optimizationRecommendedEvent.reason();
        }

        return "New analytics notification for request " + event.requestId();
    }

    private String valueOrFallback(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record Recipient(String type, String id) {
    }
}
