package com.insightflow.billing.service;

import com.insightflow.billing.domain.BillingRecord;
import com.insightflow.billing.domain.PriceTableEntry;
import com.insightflow.billing.repository.BillingRecordRepository;
import com.insightflow.billing.repository.PricingTableRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BillingDataService {

    private final PricingTableRepository pricingTableRepository;
    private final BillingRecordRepository billingRecordRepository;

    public BillingDataService(
            PricingTableRepository pricingTableRepository,
            BillingRecordRepository billingRecordRepository
    ) {
        this.pricingTableRepository = pricingTableRepository;
        this.billingRecordRepository = billingRecordRepository;
    }

    public List<BillingRecord> getBillingRecords() {
        return billingRecordRepository.findAll();
    }

    public List<PriceTableEntry> getPriceTableEntries(String version) {
        return pricingTableRepository.findByVersion(version);
    }

    public List<PriceTableEntry> getPriceTableEntries() {
        return pricingTableRepository.findAll();
    }
}
