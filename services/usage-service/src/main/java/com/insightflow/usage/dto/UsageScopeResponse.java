package com.insightflow.usage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record UsageScopeResponse(
        @JsonProperty("scope_type")
        String scopeType,
        @JsonProperty("scope_id")
        String scopeId,
        UsagePeriod period,
        UsageSummary summary,
        List<UsageItem> items
) {
}
