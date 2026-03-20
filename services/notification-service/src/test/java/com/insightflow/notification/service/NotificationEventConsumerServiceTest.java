package com.insightflow.notification.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.insightflow.notification.config.NotificationContextDefaults;
import com.insightflow.notification.domain.CostCalculatedEvent;
import com.insightflow.notification.domain.NotificationChannel;
import com.insightflow.notification.domain.NotificationPreferenceStatus;
import com.insightflow.notification.domain.NotificationPreferences;
import com.insightflow.notification.domain.NotificationSubscriptionPreference;
import com.insightflow.notification.domain.OptimizationRecommendedEvent;
import com.insightflow.notification.repository.InMemoryInternalNotificationRepository;
import com.insightflow.notification.repository.InMemoryNotificationPreferencesRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class NotificationEventConsumerServiceTest {

    @Test
    void consumesCostCalculatedEventIntoTeamDigestNotification() {
        var defaults = new NotificationContextDefaults("u_demo_001", "t_demo");
        var internalNotificationRepository = new InMemoryInternalNotificationRepository();
        var service = new NotificationEventConsumerService(
                new InMemoryNotificationPreferencesRepository(defaults),
                internalNotificationRepository
        );

        int persistedCount = service.consume(new CostCalculatedEvent(
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

        assertThat(persistedCount).isEqualTo(1);
        assertThat(internalNotificationRepository.findByContext("u_demo_001", "t_demo"))
                .singleElement()
                .satisfies(notification -> {
                    assertThat(notification.eventType()).isEqualTo("cost.calculated");
                    assertThat(notification.recipientType()).isEqualTo("team");
                    assertThat(notification.recipientId()).isEqualTo("t_demo");
                    assertThat(notification.channel()).isEqualTo(NotificationChannel.TEAM_DIGEST);
                    assertThat(notification.title()).contains("svc_doc_summary");
                });
    }

    @Test
    void consumesOptimizationRecommendedEventIntoUserInboxWhenUserPreferenceIsActive() {
        var defaults = new NotificationContextDefaults("u_demo_001", "t_demo");
        var internalNotificationRepository = new InMemoryInternalNotificationRepository();
        var service = new NotificationEventConsumerService(
                new InMemoryNotificationPreferencesRepository(defaults),
                internalNotificationRepository
        );

        int persistedCount = service.consume(new OptimizationRecommendedEvent(
                "req_opt_001",
                "u_demo_001",
                "",
                "svc_doc_summary",
                "gpt-4o-mini",
                "gpt-4.1-mini",
                "lower_cost_similar_task",
                Instant.parse("2026-03-20T10:20:30Z")
        ));

        assertThat(persistedCount).isEqualTo(1);
        assertThat(internalNotificationRepository.findByContext("u_demo_001", "t_demo"))
                .singleElement()
                .satisfies(notification -> {
                    assertThat(notification.eventType()).isEqualTo("optimization.recommended");
                    assertThat(notification.recipientType()).isEqualTo("user");
                    assertThat(notification.recipientId()).isEqualTo("u_demo_001");
                    assertThat(notification.channel()).isEqualTo(NotificationChannel.USER_INBOX);
                    assertThat(notification.message()).contains("gpt-4.1-mini");
                });
    }

    @Test
    void ignoresDuplicateEventDeliveryForSameAudience() {
        var defaults = new NotificationContextDefaults("u_demo_001", "t_demo");
        var internalNotificationRepository = new InMemoryInternalNotificationRepository();
        var service = new NotificationEventConsumerService(
                new InMemoryNotificationPreferencesRepository(defaults),
                internalNotificationRepository
        );
        var event = new CostCalculatedEvent(
                "req_cost_002",
                "u_demo_001",
                "t_demo",
                "svc_doc_summary",
                "wf_monthly_report",
                "gpt-4o-mini",
                "KRW",
                new BigDecimal("231.55"),
                Instant.parse("2026-03-20T10:25:30Z")
        );

        int firstPersistedCount = service.consume(event);
        int secondPersistedCount = service.consume(event);

        assertThat(firstPersistedCount).isEqualTo(1);
        assertThat(secondPersistedCount).isZero();
        assertThat(internalNotificationRepository.findByContext("u_demo_001", "t_demo")).hasSize(1);
    }

    @Test
    void skipsMutedPreferences() {
        var internalNotificationRepository = new InMemoryInternalNotificationRepository();
        var service = new NotificationEventConsumerService(
                (userId, teamId) -> new NotificationPreferences(
                        "u_demo_001",
                        "t_demo",
                        List.of(new NotificationSubscriptionPreference(
                                "cost.calculated",
                                NotificationChannel.TEAM_DIGEST,
                                NotificationPreferenceStatus.MUTED
                        ))
                ),
                internalNotificationRepository
        );

        int persistedCount = service.consume(new CostCalculatedEvent(
                "req_cost_003",
                "u_demo_001",
                "t_demo",
                "svc_doc_summary",
                "wf_monthly_report",
                "gpt-4o-mini",
                "KRW",
                new BigDecimal("99.10"),
                Instant.parse("2026-03-20T10:30:30Z")
        ));

        assertThat(persistedCount).isZero();
        assertThat(internalNotificationRepository.findByContext("u_demo_001", "t_demo")).isEmpty();
    }
}
