package com.insightflow.notification;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.insightflow.notification.domain.CostCalculatedEvent;
import com.insightflow.notification.domain.LimitExceededEvent;
import com.insightflow.notification.repository.InternalNotificationRepository;
import com.insightflow.notification.service.NotificationEventConsumerService;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationServiceApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private NotificationEventConsumerService notificationEventConsumerService;

    @Autowired
    private InternalNotificationRepository internalNotificationRepository;

    @BeforeEach
    void clearNotifications() {
        internalNotificationRepository.clear();
    }

    @Test
    void returnsHealth() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.service").value("notification-service"))
                .andExpect(jsonPath("$.data.status").value("UP"));
    }

    @Test
    void returnsSubscriptions() throws Exception {
        mockMvc.perform(get("/internal/notifications/subscriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].event_type").value("limit.exceeded"))
                .andExpect(jsonPath("$.data[0].channel").value("team_digest"))
                .andExpect(jsonPath("$.data[0].status").value("active"))
                .andExpect(jsonPath("$.data[1].event_type").value("cost.calculated"))
                .andExpect(jsonPath("$.data[1].channel").value("team_digest"))
                .andExpect(jsonPath("$.data[1].status").value("active"))
                .andExpect(jsonPath("$.data[2].event_type").value("limit.exceeded"))
                .andExpect(jsonPath("$.data[2].channel").value("user_inbox"))
                .andExpect(jsonPath("$.data[2].status").value("active"));
    }

    @Test
    void returnsInternalNotifications() throws Exception {
        notificationEventConsumerService.consume(new CostCalculatedEvent(
                "req_cost_001",
                "u_demo_001",
                "t_demo",
                "svc_doc_summary",
                "wf_monthly_report",
                "gpt-4o-mini",
                "KRW",
                new BigDecimal("1184.23"),
                Instant.parse("2026-03-20T10:15:30Z")
        ));
        notificationEventConsumerService.consume(new LimitExceededEvent(
                "req_limit_001",
                "u_demo_001",
                "t_demo",
                "svc_doc_summary",
                "wf_monthly_report",
                "DAILY_TOKEN",
                "10000",
                "12550",
                Instant.parse("2026-03-20T10:20:30Z")
        ));

        mockMvc.perform(get("/internal/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[?(@.event_type=='limit.exceeded' && @.recipient_type=='user' && @.recipient_id=='u_demo_001' && @.channel=='user_inbox')]").isNotEmpty())
                .andExpect(jsonPath("$.data[?(@.event_type=='limit.exceeded' && @.recipient_type=='team' && @.recipient_id=='t_demo' && @.channel=='team_digest')]").isNotEmpty())
                .andExpect(jsonPath("$.data[?(@.event_type=='cost.calculated' && @.recipient_type=='team' && @.recipient_id=='t_demo' && @.channel=='team_digest')]").isNotEmpty());
    }

    @Test
    void registersNotificationQueryServiceBean() {
        org.assertj.core.api.Assertions.assertThat(applicationContext.containsBean("notificationQueryService")).isTrue();
    }
}
