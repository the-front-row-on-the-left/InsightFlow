package com.insightflow.billing.repository;

import com.insightflow.billing.domain.BillingRecord;
import java.util.List;

public interface BillingRecordRepository {

    List<BillingRecord> findAll();

    void save(String eventId, BillingRecord billingRecord);

    boolean existsByRequestId(String requestId);
}
