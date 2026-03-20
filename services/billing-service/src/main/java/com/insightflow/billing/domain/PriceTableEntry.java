package com.insightflow.billing.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record PriceTableEntry(
        String priceTableVersion,
        String serviceId,
        String model,
        PricingModel pricingModel,
        String currency,
        BigDecimal unitPriceInput,
        BigDecimal unitPriceOutput,
        BigDecimal unitPriceRequest,
        Instant effectiveFrom,
        Instant effectiveTo,
        String status
) {

    public boolean isActiveFor(Instant occurredAt) {
        boolean startsOnOrBefore = !effectiveFrom.isAfter(occurredAt);
        boolean noEnd = effectiveTo == null;
        boolean endsAfter = noEnd || effectiveTo.isAfter(occurredAt) || effectiveTo.equals(occurredAt);
        return startsOnOrBefore && endsAfter && "active".equalsIgnoreCase(status);
    }
}
