package com.insightflow.billing.repository;

import com.insightflow.billing.domain.PriceTableEntry;
import java.util.List;

public interface PricingTableRepository {

    List<PriceTableEntry> findAll();

    List<PriceTableEntry> findByVersion(String version);

    void saveAll(List<PriceTableEntry> entries);

    long count();
}
