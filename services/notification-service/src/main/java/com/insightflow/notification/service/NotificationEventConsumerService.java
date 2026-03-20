package com.insightflow.notification.service;

import com.insightflow.notification.domain.AnalyticsNotificationEvent;
import com.insightflow.notification.domain.CostCalculatedEvent;
import com.insightflow.notification.domain.InternalNotification;
import com.insightflow.notification.domain.LimitExceededEvent;
import com.insightflow.notification.domain.NotificationChannel;
import com.insightflow.notification.domain.NotificationPreferences;
import com.insightflow.notification.domain.NotificationSubscriptionPreference;
import com.insightflow.notification.repository.InternalNotificationRepository;
import com.insightflow.notification.repository.NotificationPreferencesRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class NotificationEventConsumerService {

    private static final BigDecimal COST_ALERT_THRESHOLD = new BigDecimal("1000.00");

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
        if (!isCostAlert(event)) {
            return 0;
        }
        return consumeEvent(event);
    }

    public int consume(LimitExceededEvent event) {
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
            case "cost.calculated" -> "Cost alert for " + valueOrFallback(event.serviceId(), "unknown-service");
            case "limit.exceeded" -> "Limit exceeded for " + valueOrFallback(event.serviceId(), "unknown-service");
            default -> "Analytics notification";
        };
    }

    private String messageFor(AnalyticsNotificationEvent event) {
        if (event instanceof CostCalculatedEvent costCalculatedEvent) {
            return "Request "
                    + costCalculatedEvent.requestId()
                    + " exceeded cost threshold at "
                    + costCalculatedEvent.currency()
                    + " "
                    + costCalculatedEvent.cost().toPlainString()
                    + " using "
                    + costCalculatedEvent.model();
        }

        if (event instanceof LimitExceededEvent limitExceededEvent) {
            return "Request "
                    + limitExceededEvent.requestId()
                    + " exceeded "
                    + limitExceededEvent.limitType()
                    + " threshold "
                    + limitExceededEvent.threshold()
                    + " with observed value "
                    + limitExceededEvent.observedValue();
        }

        return "New analytics notification for request " + event.requestId();
    }

    private String valueOrFallback(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private boolean isCostAlert(CostCalculatedEvent event) {
        return event.cost().compareTo(COST_ALERT_THRESHOLD) >= 0;
    }

    private record Recipient(String type, String id) {
    }
}
