package com.insightflow.usage.bootstrap;

import com.insightflow.usage.domain.TrackedUsageEvent;
import com.insightflow.usage.domain.UsageRecord;
import com.insightflow.usage.repository.InMemoryUsageRecordRepository;
import com.insightflow.usage.repository.UsageRecordRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class UsagePersistenceBootstrap implements ApplicationRunner {

    private final UsageRecordRepository usageRecordRepository;

    public UsagePersistenceBootstrap(UsageRecordRepository usageRecordRepository) {
        this.usageRecordRepository = usageRecordRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        InMemoryUsageRecordRepository.seededRecords().stream()
                .filter(record -> !usageRecordRepository.existsByRequestId(record.requestId()))
                .map(this::toTrackedUsageEvent)
                .forEach(usageRecordRepository::save);
    }

    private TrackedUsageEvent toTrackedUsageEvent(UsageRecord record) {
        return new TrackedUsageEvent(
                "seed_" + record.requestId(),
                TrackedUsageEvent.EVENT_TYPE,
                record.requestId(),
                record.userId(),
                record.teamId(),
                record.serviceId(),
                record.workflowId(),
                record.model(),
                record.status(),
                record.promptTokens(),
                record.completionTokens(),
                record.totalTokens(),
                record.latencyMs(),
                !"BLOCKED".equals(record.status()),
                record.requestedAt(),
                record.requestedAt()
        );
    }
}
