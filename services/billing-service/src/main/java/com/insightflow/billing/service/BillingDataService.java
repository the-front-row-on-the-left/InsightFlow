package com.insightflow.billing.service;

import com.insightflow.billing.config.BillingSeedProperties;
import com.insightflow.billing.domain.BillingRecord;
import com.insightflow.billing.domain.BillingRequestUsage;
import com.insightflow.billing.domain.PriceTableEntry;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BillingDataService {

    private final List<PriceTableEntry> priceTableEntries;
    private final List<BillingRecord> billingRecords;

    public BillingDataService(BillingSeedProperties billingSeedProperties, BillingCostCalculator billingCostCalculator) {
        this.priceTableEntries = billingSeedProperties.priceTables().stream()
                .map(BillingSeedProperties.PriceTableSeed::toPriceTableEntry)
                .sorted(Comparator.comparing(PriceTableEntry::effectiveFrom))
                .toList();
        this.billingRecords = billingSeedProperties.requests().stream()
                .map(BillingSeedProperties.RequestSeed::toBillingRequestUsage)
                .map(usage -> billingCostCalculator.calculate(usage, resolvePriceTableEntry(usage)))
                .sorted(Comparator.comparing(BillingRecord::occurredAt).reversed())
                .toList();
    }

    public List<BillingRecord> getBillingRecords() {
        return billingRecords;
    }

    public List<PriceTableEntry> getPriceTableEntries(String version) {
        return priceTableEntries.stream()
                .filter(entry -> entry.priceTableVersion().equals(version))
                .toList();
    }

    public List<PriceTableEntry> getPriceTableEntries() {
        return priceTableEntries;
    }

    private PriceTableEntry resolvePriceTableEntry(BillingRequestUsage usage) {
        return priceTableEntries.stream()
                .filter(entry -> entry.serviceId().equals(usage.serviceId()))
                .filter(entry -> entry.model().equals(usage.model()))
                .filter(entry -> entry.isActiveFor(usage.occurredAt()))
                .max(Comparator.comparing(PriceTableEntry::effectiveFrom))
                .orElseThrow(() -> new IllegalStateException(
                        "No active price table entry for request " + usage.requestId()
                                + " service=" + usage.serviceId()
                                + " model=" + usage.model()
                ));
    }
}
