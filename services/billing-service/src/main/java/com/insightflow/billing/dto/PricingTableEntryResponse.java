package com.insightflow.billing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PricingTableEntryResponse(
        @JsonProperty("service_id")
        String serviceId,
        String model,
        @JsonProperty("pricing_model")
        String pricingModel,
        String currency,
        @JsonProperty("unit_price_input")
        String unitPriceInput,
        @JsonProperty("unit_price_output")
        String unitPriceOutput,
        @JsonProperty("unit_price_request")
        String unitPriceRequest,
        @JsonProperty("effective_from")
        String effectiveFrom,
        @JsonProperty("effective_to")
        String effectiveTo,
        String status
) {
}
