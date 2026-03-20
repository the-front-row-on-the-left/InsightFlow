package com.insightflow.billing.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.insightflow.billing.domain.BillingRecord;
import com.insightflow.billing.domain.BillingRequestUsage;
import com.insightflow.billing.domain.PriceTableEntry;
import com.insightflow.billing.domain.PricingModel;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class BillingCostCalculatorTest {

    private final BillingCostCalculator billingCostCalculator = new BillingCostCalculator();

    @Test
    void calculatesPerTokenCostUsingInputAndOutputUnitPrices() {
        PriceTableEntry priceTableEntry = new PriceTableEntry(
                "2026-03-v1",
                "svc_doc_summary",
                "gpt-4o-mini",
                PricingModel.PER_TOKEN,
                "KRW",
                new BigDecimal("0.08"),
                new BigDecimal("0.32"),
                BigDecimal.ZERO,
                Instant.parse("2026-03-01T00:00:00Z"),
                null,
                "active"
        );
        BillingRequestUsage usage = new BillingRequestUsage(
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
        );

        BillingRecord billingRecord = billingCostCalculator.calculate(usage, priceTableEntry);

        assertThat(billingRecord.costBeforeRounding()).isEqualByComparingTo("174.4000");
        assertThat(billingRecord.totalCost()).isEqualByComparingTo("174.40");
        assertThat(billingRecord.priceTableVersion()).isEqualTo("2026-03-v1");
        assertThat(billingRecord.billable()).isTrue();
    }

    @Test
    void calculatesPerRequestCostFromRequestUnitPrice() {
        PriceTableEntry priceTableEntry = new PriceTableEntry(
                "2026-03-v1",
                "svc_ocr_scan",
                "ocr-basic",
                PricingModel.PER_REQUEST,
                "KRW",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("120.00"),
                Instant.parse("2026-03-01T00:00:00Z"),
                null,
                "active"
        );
        BillingRequestUsage usage = new BillingRequestUsage(
                "req_t_003",
                "u_ops_002",
                "t_demo",
                "wf_monthly_report",
                "svc_ocr_scan",
                "ocr-basic",
                "SUCCEEDED",
                0,
                0,
                true,
                Instant.parse("2026-03-20T10:00:00Z")
        );

        BillingRecord billingRecord = billingCostCalculator.calculate(usage, priceTableEntry);

        assertThat(billingRecord.costBeforeRounding()).isEqualByComparingTo("120.0000");
        assertThat(billingRecord.totalCost()).isEqualByComparingTo("120.00");
        assertThat(billingRecord.pricingModel()).isEqualTo(PricingModel.PER_REQUEST);
    }

    @Test
    void treatsFixedPricingAsConfiguredReferenceCost() {
        PriceTableEntry priceTableEntry = new PriceTableEntry(
                "2026-03-v1",
                "svc_internal_assistant",
                "internal-fixed",
                PricingModel.FIXED,
                "KRW",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("4900.00"),
                Instant.parse("2026-03-01T00:00:00Z"),
                null,
                "active"
        );
        BillingRequestUsage usage = new BillingRequestUsage(
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
                Instant.parse("2026-03-20T11:00:00Z")
        );

        BillingRecord billingRecord = billingCostCalculator.calculate(usage, priceTableEntry);

        assertThat(billingRecord.costBeforeRounding()).isEqualByComparingTo("4900.0000");
        assertThat(billingRecord.totalCost()).isEqualByComparingTo("4900.00");
        assertThat(billingRecord.billable()).isFalse();
        assertThat(billingRecord.currency()).isEqualTo("KRW");
    }
}
