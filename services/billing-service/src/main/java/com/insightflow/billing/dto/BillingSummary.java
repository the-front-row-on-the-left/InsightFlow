package com.insightflow.billing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BillingSummary(
        @JsonProperty("total_cost")
        String totalCost,
        @JsonProperty("cost_before_rounding")
        String costBeforeRounding,
        boolean billable,
        @JsonProperty("item_count")
        int itemCount
) {
}
