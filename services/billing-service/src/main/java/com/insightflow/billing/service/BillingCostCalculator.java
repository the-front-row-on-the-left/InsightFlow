package com.insightflow.billing.service;

import com.insightflow.billing.domain.BillingRecord;
import com.insightflow.billing.domain.BillingRequestUsage;
import com.insightflow.billing.domain.PriceTableEntry;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class BillingCostCalculator {

    public BillingRecord calculate(BillingRequestUsage usage, PriceTableEntry priceTableEntry) {
        BigDecimal costBeforeRounding = switch (priceTableEntry.pricingModel()) {
            case PER_TOKEN -> perTokenCost(usage, priceTableEntry);
            case PER_REQUEST, FIXED -> normalizeScale(priceTableEntry.unitPriceRequest());
        };

        BigDecimal totalCost = costBeforeRounding.setScale(2, RoundingMode.HALF_UP);

        return new BillingRecord(
                usage.requestId(),
                usage.userId(),
                usage.teamId(),
                usage.workflowId(),
                usage.serviceId(),
                usage.model(),
                usage.status(),
                priceTableEntry.pricingModel(),
                priceTableEntry.currency(),
                priceTableEntry.priceTableVersion(),
                usage.promptTokens(),
                usage.completionTokens(),
                usage.totalTokens(),
                usage.billable(),
                normalizeScale(priceTableEntry.unitPriceInput()),
                normalizeScale(priceTableEntry.unitPriceOutput()),
                normalizeScale(priceTableEntry.unitPriceRequest()),
                costBeforeRounding,
                totalCost,
                usage.occurredAt()
        );
    }

    private BigDecimal perTokenCost(BillingRequestUsage usage, PriceTableEntry priceTableEntry) {
        BigDecimal inputCost = BigDecimal.valueOf(usage.promptTokens())
                .multiply(priceTableEntry.unitPriceInput());
        BigDecimal outputCost = BigDecimal.valueOf(usage.completionTokens())
                .multiply(priceTableEntry.unitPriceOutput());
        return normalizeScale(inputCost.add(outputCost));
    }

    private BigDecimal normalizeScale(BigDecimal value) {
        return value.setScale(4, RoundingMode.HALF_UP);
    }
}
