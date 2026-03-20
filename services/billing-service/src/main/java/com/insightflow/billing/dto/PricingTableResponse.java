package com.insightflow.billing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PricingTableResponse(
        @JsonProperty("price_table_version")
        String priceTableVersion,
        List<PricingTableEntryResponse> entries
) {
}
