package com.insightflow.billing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record BillingScopeResponse(
        @JsonProperty("scope_type")
        String scopeType,
        @JsonProperty("scope_id")
        String scopeId,
        BillingPeriod period,
        String currency,
        @JsonProperty("price_table_version")
        String priceTableVersion,
        BillingSummary summary,
        List<BillingItem> items
) {
}
