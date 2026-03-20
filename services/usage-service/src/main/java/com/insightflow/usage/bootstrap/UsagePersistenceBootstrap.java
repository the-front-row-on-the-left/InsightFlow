package com.insightflow.usage.bootstrap;

import com.insightflow.usage.repository.InMemoryUsageRecordRepository;
import com.insightflow.usage.repository.JdbcUsageRecordRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UsagePersistenceBootstrap implements ApplicationRunner {

    private final JdbcUsageRecordRepository usageRecordRepository;
    private final boolean seedDataEnabled;

    public UsagePersistenceBootstrap(
            JdbcUsageRecordRepository usageRecordRepository,
            @Value("${insightflow.usage.seed-data-enabled:true}") boolean seedDataEnabled
    ) {
        this.usageRecordRepository = usageRecordRepository;
        this.seedDataEnabled = seedDataEnabled;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!seedDataEnabled || usageRecordRepository.count() > 0) {
            return;
        }

        InMemoryUsageRecordRepository.seededRecords().forEach(usageRecordRepository::saveSeedRecord);
    }
}
