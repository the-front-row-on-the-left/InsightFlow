package com.insightflow.billing.service;

import com.insightflow.billing.domain.BillingRecord;
import com.insightflow.billing.domain.PriceTableEntry;
import com.insightflow.billing.dto.BillingItem;
import com.insightflow.billing.dto.BillingPeriod;
import com.insightflow.billing.dto.BillingScopeResponse;
import com.insightflow.billing.dto.BillingSummary;
import com.insightflow.billing.dto.PricingTableEntryResponse;
import com.insightflow.billing.dto.PricingTableResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import org.springframework.stereotype.Service;

@Service
public class BillingQueryService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final BillingDataService billingDataService;

    public BillingQueryService(BillingDataService billingDataService) {
        this.billingDataService = billingDataService;
    }

    public BillingScopeResponse getUserBilling(String userId) {
        return getScopeBilling("user", userId, BillingRecord::userId);
    }

    public BillingScopeResponse getTeamBilling(String teamId) {
        return getScopeBilling("team", teamId, BillingRecord::teamId);
    }

    public BillingScopeResponse getWorkflowBilling(String workflowId) {
        return getScopeBilling("workflow", workflowId, BillingRecord::workflowId);
    }

    public PricingTableResponse getPricingTable(String version) {
        List<PricingTableEntryResponse> entries = billingDataService.getPriceTableEntries(version).stream()
                .map(this::toPricingTableEntryResponse)
                .toList();
        return new PricingTableResponse(version, entries);
    }

    private BillingScopeResponse getScopeBilling(String scopeType, String scopeId, Function<BillingRecord, String> scopeExtractor) {
        List<BillingRecord> records = billingDataService.getBillingRecords().stream()
                .filter(record -> scopeExtractor.apply(record).equals(scopeId))
                .sorted(Comparator.comparing(BillingRecord::occurredAt).reversed().thenComparing(BillingRecord::requestId))
                .toList();

        BigDecimal totalBeforeRounding = records.stream()
                .map(BillingRecord::costBeforeRounding)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);
        BigDecimal totalCost = totalBeforeRounding.setScale(2, RoundingMode.HALF_UP);

        String currency = records.isEmpty() ? defaultCurrency() : records.getFirst().currency();
        String priceTableVersion = records.isEmpty() ? latestPriceTableVersion() : records.getFirst().priceTableVersion();
        boolean billable = !records.isEmpty() && records.stream().allMatch(BillingRecord::billable);

        return new BillingScopeResponse(
                scopeType,
                scopeId,
                defaultPeriod(),
                currency,
                priceTableVersion,
                new BillingSummary(formatMoney(totalCost), formatDetailed(totalBeforeRounding), billable, records.size()),
                records.stream().map(this::toBillingItem).toList()
        );
    }

    private BillingItem toBillingItem(BillingRecord record) {
        return new BillingItem(
                record.requestId(),
                record.serviceId(),
                record.model(),
                formatMoney(record.totalCost()),
                formatDetailed(record.costBeforeRounding()),
                record.status(),
                record.billable(),
                record.pricingModel().code(),
                record.promptTokens(),
                record.completionTokens(),
                record.totalTokens(),
                record.priceTableVersion(),
                record.occurredAt().toString()
        );
    }

    private PricingTableEntryResponse toPricingTableEntryResponse(PriceTableEntry entry) {
        return new PricingTableEntryResponse(
                entry.serviceId(),
                entry.model(),
                entry.pricingModel().code(),
                entry.currency(),
                entry.unitPriceInput().toPlainString(),
                entry.unitPriceOutput().toPlainString(),
                entry.unitPriceRequest().toPlainString(),
                entry.effectiveFrom().toString(),
                entry.effectiveTo() == null ? null : entry.effectiveTo().toString(),
                entry.status()
        );
    }

    private BillingPeriod defaultPeriod() {
        List<BillingRecord> records = billingDataService.getBillingRecords();
        if (records.isEmpty()) {
            return new BillingPeriod("2026-03-01", "2026-03-01", "day");
        }

        String from = records.stream()
                .map(BillingRecord::occurredAt)
                .min(Comparator.naturalOrder())
                .orElseThrow()
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
                .withDayOfMonth(1)
                .format(DATE_FORMATTER);
        String to = records.stream()
                .map(BillingRecord::occurredAt)
                .max(Comparator.naturalOrder())
                .orElseThrow()
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
                .format(DATE_FORMATTER);
        return new BillingPeriod(from, to, "day");
    }

    private String latestPriceTableVersion() {
        return billingDataService.getPriceTableEntries().stream()
                .map(PriceTableEntry::priceTableVersion)
                .max(String::compareTo)
                .orElse("unknown");
    }

    private String defaultCurrency() {
        return billingDataService.getPriceTableEntries().stream()
                .map(PriceTableEntry::currency)
                .findFirst()
                .orElse("KRW");
    }

    private String formatMoney(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String formatDetailed(BigDecimal amount) {
        return amount.setScale(4, RoundingMode.HALF_UP).toPlainString();
    }
}
