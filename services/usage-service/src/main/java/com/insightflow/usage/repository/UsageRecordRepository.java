package com.insightflow.usage.repository;

import com.insightflow.usage.domain.TrackedUsageEvent;
import com.insightflow.usage.domain.UsageRecord;
import java.util.List;

public interface UsageRecordRepository {

    List<UsageRecord> findAll();

    void save(TrackedUsageEvent trackedUsageEvent);

    boolean existsByRequestId(String requestId);
}
