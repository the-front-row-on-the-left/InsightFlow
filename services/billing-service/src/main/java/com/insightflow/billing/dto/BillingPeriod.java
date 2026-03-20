package com.insightflow.billing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BillingPeriod(
        @JsonProperty("from")
        String from,
        @JsonProperty("to")
        String to,
        String unit
) {
}
