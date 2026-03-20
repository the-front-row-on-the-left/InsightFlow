package com.insightflow.usage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.insightflow.usage.domain.AiCompletedEvent;
import com.insightflow.usage.domain.AiRequestedEvent;
import com.insightflow.usage.domain.TrackedUsageEvent;
import com.insightflow.usage.domain.UsageEventSnapshot;
import com.insightflow.usage.repository.UsageEventSnapshotRepository;
import com.insightflow.usage.repository.UsageRecordRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UsageEventMergeServiceTest {

    @Mock
    private UsageEventSnapshotRepository usageEventSnapshotRepository;

    @Mock
    private UsageRecordRepository usageRecordRepository;

    @Mock
    private UsageTrackedEventPublisher usageTrackedEventPublisher;

    @InjectMocks
    private UsageEventMergeService usageEventMergeService;

    @Test
    void mergesAiRequestedAndAiCompletedIntoTrackedUsage() {
        AiCompletedEvent completedEvent = new AiCompletedEvent(
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
        UsageEventSnapshot snapshot = new UsageEventSnapshot(
                "req_001",
                "evt_requested_001",
                "evt_completed_001",
                "u_demo_001",
                "t_demo",
                "svc_doc_summary",
                "wf_weekly_digest",
                "gpt-4.1-mini",
                "gpt-4.1-mini",
                "SUCCESS",
                320,
                180,
                500,
                245,
                true,
                OffsetDateTime.parse("2026-03-20T09:00:00Z"),
                OffsetDateTime.parse("2026-03-20T09:00:04Z")
        );
        when(usageEventSnapshotRepository.findByRequestId("req_001")).thenReturn(Optional.of(snapshot));
        when(usageRecordRepository.existsByRequestId("req_001")).thenReturn(false);

        Optional<TrackedUsageEvent> result = usageEventMergeService.handleCompleted(completedEvent);

        assertThat(result).isPresent();
        verify(usageEventSnapshotRepository).saveCompletedEvent(completedEvent);

        ArgumentCaptor<TrackedUsageEvent> savedEvent = ArgumentCaptor.forClass(TrackedUsageEvent.class);
        verify(usageRecordRepository).save(savedEvent.capture());
        verify(usageTrackedEventPublisher).publish(savedEvent.getValue());
        assertThat(savedEvent.getValue().requestId()).isEqualTo("req_001");
        assertThat(savedEvent.getValue().status()).isEqualTo("SUCCESS");
        assertThat(savedEvent.getValue().totalTokens()).isEqualTo(500);
    }

    @Test
    void doesNotPublishTrackedUsageUntilSnapshotIsComplete() {
        AiRequestedEvent requestedEvent = new AiRequestedEvent(
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
        UsageEventSnapshot incompleteSnapshot = new UsageEventSnapshot(
                "req_001",
                "evt_requested_001",
                null,
                "u_demo_001",
                "t_demo",
                "svc_doc_summary",
                "wf_weekly_digest",
                "gpt-4.1-mini",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                OffsetDateTime.parse("2026-03-20T09:00:00Z"),
                null
        );
        when(usageEventSnapshotRepository.findByRequestId("req_001")).thenReturn(Optional.of(incompleteSnapshot));

        Optional<TrackedUsageEvent> result = usageEventMergeService.handleRequested(requestedEvent);

        assertThat(result).isEmpty();
        verify(usageEventSnapshotRepository).saveRequestedEvent(requestedEvent);
        verify(usageRecordRepository, never()).save(any());
        verify(usageTrackedEventPublisher, never()).publish(any());
    }

    @Test
    void skipsPublishWhenTrackedUsageAlreadyExistsForRequest() {
        AiCompletedEvent completedEvent = new AiCompletedEvent(
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
        UsageEventSnapshot snapshot = new UsageEventSnapshot(
                "req_001",
                "evt_requested_001",
                "evt_completed_001",
                "u_demo_001",
                "t_demo",
                "svc_doc_summary",
                "wf_weekly_digest",
                "gpt-4.1-mini",
                "gpt-4.1-mini",
                "SUCCESS",
                320,
                180,
                500,
                245,
                true,
                OffsetDateTime.parse("2026-03-20T09:00:00Z"),
                OffsetDateTime.parse("2026-03-20T09:00:04Z")
        );
        when(usageEventSnapshotRepository.findByRequestId("req_001")).thenReturn(Optional.of(snapshot));
        when(usageRecordRepository.existsByRequestId("req_001")).thenReturn(true);

        Optional<TrackedUsageEvent> result = usageEventMergeService.handleCompleted(completedEvent);

        assertThat(result).isEmpty();
        verify(usageRecordRepository, never()).save(any());
        verify(usageTrackedEventPublisher, never()).publish(any());
    }
}
