package com.insightflow.usage.repository;

import com.insightflow.usage.domain.AiCompletedEvent;
import com.insightflow.usage.domain.AiRequestedEvent;
import com.insightflow.usage.domain.UsageEventSnapshot;
import java.util.Optional;

public interface UsageEventSnapshotRepository {

    Optional<UsageEventSnapshot> findByRequestId(String requestId);

    void saveRequestedEvent(AiRequestedEvent event);

    void saveCompletedEvent(AiCompletedEvent event);

    long count();
}
