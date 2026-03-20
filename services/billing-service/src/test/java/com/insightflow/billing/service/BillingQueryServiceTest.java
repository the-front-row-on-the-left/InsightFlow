package com.insightflow.billing.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.insightflow.billing.config.BillingSeedProperties;
import com.insightflow.billing.domain.BillingRecord;
import com.insightflow.billing.domain.BillingRequestUsage;
import com.insightflow.billing.domain.PriceTableEntry;
import com.insightflow.billing.dto.BillingScopeResponse;
import com.insightflow.billing.dto.PricingTableResponse;
import com.insightflow.billing.repository.BillingRecordRepository;
import com.insightflow.billing.repository.PricingTableRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;

class BillingQueryServiceTest {

    private final BillingQueryService billingQueryService = new BillingQueryService(
            new BillingDataService(
                    new InMemoryPricingTableRepository(seedProperties()),
                    new InMemoryBillingRecordRepository(seedProperties(), new BillingCostCalculator())
            )
    );

    @Test
    void aggregatesUserBillingFromCalculatedRequestRecords() {
        BillingScopeResponse response = billingQueryService.getUserBilling("u_demo_001");

        assertThat(response.scopeType()).isEqualTo("user");
        assertThat(response.scopeId()).isEqualTo("u_demo_001");
        assertThat(response.currency()).isEqualTo("KRW");
        assertThat(response.priceTableVersion()).isEqualTo("2026-03-v1");
        assertThat(response.summary().totalCost()).isEqualTo("174.40");
        assertThat(response.summary().costBeforeRounding()).isEqualTo("174.4000");
        assertThat(response.summary().itemCount()).isEqualTo(1);
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().pricingModel()).isEqualTo("per_token");
        assertThat(response.items().getFirst().promptTokens()).isEqualTo(900);
        assertThat(response.items().getFirst().completionTokens()).isEqualTo(320);
    }

    @Test
    void aggregatesTeamBillingAfterSummingUnroundedCosts() {
        BillingScopeResponse response = billingQueryService.getTeamBilling("t_demo");

        assertThat(response.scopeType()).isEqualTo("team");
        assertThat(response.scopeId()).isEqualTo("t_demo");
        assertThat(response.summary().totalCost()).isEqualTo("417.20");
        assertThat(response.summary().costBeforeRounding()).isEqualTo("417.2000");
        assertThat(response.summary().itemCount()).isEqualTo(2);
        assertThat(response.items()).extracting("requestId")
                .containsExactly("req_t_002", "req_u_001");
    }

    @Test
    void returnsPriceTableVersionDetails() {
        PricingTableResponse response = billingQueryService.getPricingTable("2026-03-v1");

        assertThat(response.priceTableVersion()).isEqualTo("2026-03-v1");
        assertThat(response.entries()).hasSize(4);
        assertThat(response.entries().getFirst().serviceId()).isEqualTo("svc_doc_summary");
        assertThat(response.entries().getFirst().pricingModel()).isEqualTo("per_token");
        assertThat(response.entries().getFirst().unitPriceInput()).isEqualTo("0.08");
        assertThat(response.entries().getLast().pricingModel()).isEqualTo("fixed");
    }

    private BillingSeedProperties seedProperties() {
        return new BillingSeedProperties(
                List.of(
                        new BillingSeedProperties.PriceTableSeed(
                                "2026-03-v1",
                                "svc_doc_summary",
                                "gpt-4o-mini",
                                "per_token",
                                "KRW",
                                new BigDecimal("0.08"),
                                new BigDecimal("0.32"),
                                BigDecimal.ZERO,
                                Instant.parse("2026-03-01T00:00:00Z"),
                                null,
                                "active"
                        ),
                        new BillingSeedProperties.PriceTableSeed(
                                "2026-03-v1",
                                "svc_report_generator",
                                "gpt-4.1-mini",
                                "per_token",
                                "KRW",
                                new BigDecimal("0.10"),
                                new BigDecimal("0.30"),
                                BigDecimal.ZERO,
                                Instant.parse("2026-03-01T00:00:00Z"),
                                null,
                                "active"
                        ),
                        new BillingSeedProperties.PriceTableSeed(
                                "2026-03-v1",
                                "svc_ocr_scan",
                                "ocr-basic",
                                "per_request",
                                "KRW",
                                BigDecimal.ZERO,
                                BigDecimal.ZERO,
                                new BigDecimal("120.00"),
                                Instant.parse("2026-03-01T00:00:00Z"),
                                null,
                                "active"
                        ),
                        new BillingSeedProperties.PriceTableSeed(
                                "2026-03-v1",
                                "svc_internal_assistant",
                                "internal-fixed",
                                "fixed",
                                "KRW",
                                BigDecimal.ZERO,
                                BigDecimal.ZERO,
                                new BigDecimal("4900.00"),
                                Instant.parse("2026-03-01T00:00:00Z"),
                                null,
                                "active"
                        )
                ),
                List.of(
                        new BillingSeedProperties.RequestSeed(
                                "req_u_001",
                                "u_demo_001",
                                "t_demo",
                                "wf_monthly_report",
                                "svc_doc_summary",
                                "gpt-4o-mini",
                                "SUCCEEDED",
                                900,
                                320,
                                true,
                                Instant.parse("2026-03-20T09:00:00Z")
                        ),
                        new BillingSeedProperties.RequestSeed(
                                "req_t_002",
                                "u_ops_002",
                                "t_demo",
                                "wf_monthly_report",
                                "svc_report_generator",
                                "gpt-4.1-mini",
                                "SUCCEEDED",
                                1048,
                                460,
                                true,
                                Instant.parse("2026-03-20T10:00:00Z")
                        ),
                        new BillingSeedProperties.RequestSeed(
                                "req_t_003",
                                "u_ops_003",
                                "t_ops",
                                "wf_incident_triage",
                                "svc_ocr_scan",
                                "ocr-basic",
                                "SUCCEEDED",
                                0,
                                0,
                                true,
                                Instant.parse("2026-03-19T12:00:00Z")
                        ),
                        new BillingSeedProperties.RequestSeed(
                                "req_fixed_001",
                                "u_finance_001",
                                "t_finance",
                                "wf_finance_close",
                                "svc_internal_assistant",
                                "internal-fixed",
                                "REFERENCE",
                                0,
                                0,
                                false,
                                Instant.parse("2026-03-18T08:00:00Z")
                        )
                )
        );
    }

    private static final class InMemoryPricingTableRepository implements PricingTableRepository {

        private final List<PriceTableEntry> entries;

        private InMemoryPricingTableRepository(BillingSeedProperties seedProperties) {
            this.entries = seedProperties.priceTables().stream()
                    .map(BillingSeedProperties.PriceTableSeed::toPriceTableEntry)
                    .sorted(Comparator.comparing(PriceTableEntry::effectiveFrom))
                    .toList();
        }

        @Override
        public List<PriceTableEntry> findAll() {
            return entries;
        }

        @Override
        public List<PriceTableEntry> findByVersion(String version) {
            return entries.stream()
                    .filter(entry -> entry.priceTableVersion().equals(version))
                    .toList();
        }

        @Override
        public void saveAll(List<PriceTableEntry> entries) {
            throw new UnsupportedOperationException("Test repository is read-only");
        }

        @Override
        public long count() {
            return entries.size();
        }
    }

    private static final class InMemoryBillingRecordRepository implements BillingRecordRepository {

        private final List<BillingRecord> records;

        private InMemoryBillingRecordRepository(BillingSeedProperties seedProperties, BillingCostCalculator calculator) {
            List<PriceTableEntry> priceTableEntries = seedProperties.priceTables().stream()
                    .map(BillingSeedProperties.PriceTableSeed::toPriceTableEntry)
                    .sorted(Comparator.comparing(PriceTableEntry::effectiveFrom))
                    .toList();
            this.records = seedProperties.requests().stream()
                    .map(BillingSeedProperties.RequestSeed::toBillingRequestUsage)
                    .map(usage -> calculator.calculate(usage, resolvePriceTableEntry(priceTableEntries, usage)))
                    .sorted(Comparator.comparing(BillingRecord::occurredAt).reversed())
                    .toList();
        }

        @Override
        public List<BillingRecord> findAll() {
            return records;
        }

        @Override
        public void save(String eventId, BillingRecord billingRecord) {
            throw new UnsupportedOperationException("Test repository is read-only");
        }

        @Override
        public boolean existsByRequestId(String requestId) {
            return records.stream().anyMatch(record -> record.requestId().equals(requestId));
        }

        private PriceTableEntry resolvePriceTableEntry(List<PriceTableEntry> priceTableEntries, BillingRequestUsage usage) {
            return priceTableEntries.stream()
                    .filter(entry -> entry.serviceId().equals(usage.serviceId()))
                    .filter(entry -> entry.model().equals(usage.model()))
                    .filter(entry -> entry.isActiveFor(usage.occurredAt()))
                    .max(Comparator.comparing(PriceTableEntry::effectiveFrom))
                    .orElseThrow();
        }
    }
}
