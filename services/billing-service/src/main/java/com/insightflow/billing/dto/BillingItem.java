package com.insightflow.billing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BillingItem(
        @JsonProperty("request_id")
        String requestId,
        @JsonProperty("service_id")
        String serviceId,
        String model,
        @JsonProperty("total_cost")
        String totalCost,
        @JsonProperty("cost_before_rounding")
        String costBeforeRounding
) {
}
