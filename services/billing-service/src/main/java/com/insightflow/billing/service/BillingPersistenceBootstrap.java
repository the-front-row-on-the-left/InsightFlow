package com.insightflow.billing.service;

import com.insightflow.billing.config.BillingSeedProperties;
import com.insightflow.billing.domain.BillingRecord;
import com.insightflow.billing.domain.BillingRequestUsage;
import com.insightflow.billing.domain.PriceTableEntry;
import com.insightflow.billing.repository.BillingRecordRepository;
import com.insightflow.billing.repository.PricingTableRepository;
import java.util.Comparator;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class BillingPersistenceBootstrap implements ApplicationRunner {

    private final PricingTableRepository pricingTableRepository;
    private final BillingRecordRepository billingRecordRepository;
    private final BillingSeedProperties billingSeedProperties;
    private final BillingCostCalculator billingCostCalculator;

    public BillingPersistenceBootstrap(
            PricingTableRepository pricingTableRepository,
            BillingRecordRepository billingRecordRepository,
            BillingSeedProperties billingSeedProperties,
            BillingCostCalculator billingCostCalculator
    ) {
        this.pricingTableRepository = pricingTableRepository;
        this.billingRecordRepository = billingRecordRepository;
        this.billingSeedProperties = billingSeedProperties;
        this.billingCostCalculator = billingCostCalculator;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (pricingTableRepository.count() > 0) {
            seedBillingRecords();
            return;
        }
        pricingTableRepository.saveAll(priceEntries());
        seedBillingRecords();
    }

    private void seedBillingRecords() {
        billingSeedProperties.requests().stream()
                .map(BillingSeedProperties.RequestSeed::toBillingRequestUsage)
                .filter(usage -> !billingRecordRepository.existsByRequestId(usage.requestId()))
                .forEach(usage -> billingRecordRepository.save(
                        "seed_" + usage.requestId(),
                        billingCostCalculator.calculate(usage, resolvePriceTableEntry(usage))
                ));
    }

    private PriceTableEntry resolvePriceTableEntry(BillingRequestUsage usage) {
        return priceEntries().stream()
                .filter(entry -> entry.serviceId().equals(usage.serviceId()))
                .filter(entry -> entry.model().equals(usage.model()))
                .filter(entry -> entry.isActiveFor(usage.occurredAt()))
                .max(Comparator.comparing(PriceTableEntry::effectiveFrom))
                .orElseThrow();
    }

    private java.util.List<PriceTableEntry> priceEntries() {
        return billingSeedProperties.priceTables().stream()
                .map(BillingSeedProperties.PriceTableSeed::toPriceTableEntry)
                .toList();
    }
}
