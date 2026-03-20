package com.insightflow.usage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UsagePeriod(
        @JsonProperty("from")
        String from,
        @JsonProperty("to")
        String to,
        String unit
) {
}
