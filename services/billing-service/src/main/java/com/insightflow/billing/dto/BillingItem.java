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
        String costBeforeRounding,
        String status,
        boolean billable,
        @JsonProperty("pricing_model")
        String pricingModel,
        @JsonProperty("prompt_tokens")
        int promptTokens,
        @JsonProperty("completion_tokens")
        int completionTokens,
        @JsonProperty("total_tokens")
        int totalTokens,
        @JsonProperty("price_table_version")
        String priceTableVersion,
        @JsonProperty("occurred_at")
        String occurredAt
) {
}
