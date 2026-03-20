package com.insightflow.usage.service;

import com.insightflow.usage.domain.AiCompletedEvent;
import com.insightflow.usage.domain.AiRequestedEvent;
import com.insightflow.usage.domain.TrackedUsageEvent;
import com.insightflow.usage.domain.UsageEventSnapshot;
import com.insightflow.usage.repository.UsageEventSnapshotRepository;
import com.insightflow.usage.repository.UsageRecordRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class UsageEventMergeService {

    private final UsageEventSnapshotRepository usageEventSnapshotRepository;
    private final UsageRecordRepository usageRecordRepository;
    private final UsageTrackedEventPublisher usageTrackedEventPublisher;

    public UsageEventMergeService(
            UsageEventSnapshotRepository usageEventSnapshotRepository,
            UsageRecordRepository usageRecordRepository,
            UsageTrackedEventPublisher usageTrackedEventPublisher
    ) {
        this.usageEventSnapshotRepository = usageEventSnapshotRepository;
        this.usageRecordRepository = usageRecordRepository;
        this.usageTrackedEventPublisher = usageTrackedEventPublisher;
    }

    public Optional<TrackedUsageEvent> handleRequested(AiRequestedEvent event) {
        usageEventSnapshotRepository.saveRequestedEvent(event);
        return usageEventSnapshotRepository.findByRequestId(event.requestId())
                .flatMap(this::publishIfReady);
    }

    public Optional<TrackedUsageEvent> handleCompleted(AiCompletedEvent event) {
        usageEventSnapshotRepository.saveCompletedEvent(event);
        return usageEventSnapshotRepository.findByRequestId(event.requestId())
                .flatMap(this::publishIfReady);
    }

    private Optional<TrackedUsageEvent> publishIfReady(UsageEventSnapshot snapshot) {
        if (!isReady(snapshot)) {
            return Optional.empty();
        }

        if (usageRecordRepository.existsByRequestId(snapshot.requestId())) {
            return Optional.empty();
        }

        TrackedUsageEvent trackedUsageEvent = new TrackedUsageEvent(
                "evt_usage_" + UUID.randomUUID(),
                TrackedUsageEvent.EVENT_TYPE,
                snapshot.requestId(),
                snapshot.userId(),
                snapshot.teamId(),
                snapshot.serviceId(),
                snapshot.workflowId(),
                resolveModel(snapshot),
                snapshot.status(),
                snapshot.promptTokens(),
                snapshot.completionTokens(),
                snapshot.totalTokens(),
                snapshot.latencyMs(),
                Boolean.TRUE.equals(snapshot.billable()),
                snapshot.completedAt(),
                OffsetDateTime.now()
        );
        usageRecordRepository.save(trackedUsageEvent);
        usageTrackedEventPublisher.publish(trackedUsageEvent);
        return Optional.of(trackedUsageEvent);
    }

    private boolean isReady(UsageEventSnapshot snapshot) {
        return hasText(snapshot.requestId())
                && hasText(snapshot.userId())
                && hasText(snapshot.teamId())
                && hasText(snapshot.serviceId())
                && hasText(snapshot.workflowId())
                && hasText(resolveModel(snapshot))
                && hasText(snapshot.status())
                && snapshot.promptTokens() != null
                && snapshot.completionTokens() != null
                && snapshot.totalTokens() != null
                && snapshot.latencyMs() != null
                && snapshot.billable() != null
                && snapshot.completedAt() != null;
    }

    private String resolveModel(UsageEventSnapshot snapshot) {
        return hasText(snapshot.completedModel()) ? snapshot.completedModel() : snapshot.requestedModel();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
