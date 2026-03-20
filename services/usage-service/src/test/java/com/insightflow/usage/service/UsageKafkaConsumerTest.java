package com.insightflow.usage.service;

import static org.mockito.Mockito.verify;

import com.insightflow.usage.domain.AiCompletedEvent;
import com.insightflow.usage.domain.AiRequestedEvent;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UsageKafkaConsumerTest {

    @Mock
    private UsageEventMergeService usageEventMergeService;

    @InjectMocks
    private UsageKafkaConsumer usageKafkaConsumer;

    @Test
    void delegatesRequestedEventsToMergeService() {
        AiRequestedEvent event = new AiRequestedEvent(
                "evt_requested_001",
                null,
                "req_001",
                "u_demo_001",
                "t_demo",
                "svc_doc_summary",
                "wf_weekly_digest",
                "gpt-4.1-mini",
                OffsetDateTime.parse("2026-03-20T09:00:00Z")
        );

        usageKafkaConsumer.consumeRequested(event);

        verify(usageEventMergeService).handleRequested(event);
    }

    @Test
    void delegatesCompletedEventsToMergeService() {
        AiCompletedEvent event = new AiCompletedEvent(
                "evt_completed_001",
                null,
                "req_001",
                "gpt-4.1-mini",
                "SUCCESS",
                320,
                180,
                500,
                245,
                true,
                OffsetDateTime.parse("2026-03-20T09:00:04Z")
        );

        usageKafkaConsumer.consumeCompleted(event);

        verify(usageEventMergeService).handleCompleted(event);
    }
}
