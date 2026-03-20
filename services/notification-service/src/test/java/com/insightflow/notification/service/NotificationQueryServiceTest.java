package com.insightflow.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.insightflow.notification.config.NotificationContextDefaults;
import com.insightflow.notification.domain.CostCalculatedEvent;
import com.insightflow.notification.domain.OptimizationRecommendedEvent;
import com.insightflow.notification.repository.InMemoryInternalNotificationRepository;
import com.insightflow.notification.repository.InMemoryNotificationPreferencesRepository;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class NotificationQueryServiceTest {

    @Test
    void returnsPreferenceBackedSubscriptionsForDefaultContext() {
        var defaults = new NotificationContextDefaults("u_demo_001", "t_demo");
        var service = new NotificationQueryService(
                new InMemoryNotificationPreferencesRepository(defaults),
                new InMemoryInternalNotificationRepository(),
                defaults
        );

        assertThat(service.getSubscriptions())
                .extracting(
                        com.insightflow.notification.dto.NotificationSubscription::eventType,
                        com.insightflow.notification.dto.NotificationSubscription::channel,
                        com.insightflow.notification.dto.NotificationSubscription::status
                )
                .containsExactly(
                        tuple("optimization.recommended", "team_digest", "active"),
                        tuple("cost.calculated", "team_digest", "active"),
                        tuple("optimization.recommended", "user_inbox", "active")
                );
    }

    @Test
    void returnsNotificationsInOccurredAtDescendingOrder() {
        var defaults = new NotificationContextDefaults("u_demo_001", "t_demo");
        var preferencesRepository = new InMemoryNotificationPreferencesRepository(defaults);
        var internalNotificationRepository = new InMemoryInternalNotificationRepository();
        var consumerService = new NotificationEventConsumerService(preferencesRepository, internalNotificationRepository);
        var service = new NotificationQueryService(
                preferencesRepository,
                internalNotificationRepository,
                defaults
        );

        consumerService.consume(new CostCalculatedEvent(
                "req_cost_001",
                "u_demo_001",
                "t_demo",
                "svc_doc_summary",
                "wf_monthly_report",
                "gpt-4o-mini",
                "KRW",
                new BigDecimal("184.23"),
                Instant.parse("2026-03-20T10:15:30Z")
        ));
        consumerService.consume(new OptimizationRecommendedEvent(
                "req_opt_001",
                "u_demo_001",
                "",
                "svc_doc_summary",
                "gpt-4o-mini",
                "gpt-4.1-mini",
                "lower_cost_similar_task",
                Instant.parse("2026-03-20T10:20:30Z")
        ));

        assertThat(service.getNotifications())
                .extracting(
                        com.insightflow.notification.dto.InternalNotificationResponse::eventType,
                        com.insightflow.notification.dto.InternalNotificationResponse::recipientType,
                        com.insightflow.notification.dto.InternalNotificationResponse::recipientId,
                        com.insightflow.notification.dto.InternalNotificationResponse::channel
                )
                .containsExactly(
                        tuple("optimization.recommended", "user", "u_demo_001", "user_inbox"),
                        tuple("cost.calculated", "team", "t_demo", "team_digest")
                );
    }
}
