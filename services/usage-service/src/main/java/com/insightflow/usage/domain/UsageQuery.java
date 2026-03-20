package com.insightflow.usage.domain;

import java.time.LocalDate;

public record UsageQuery(
        UsageScopeType scopeType,
        String scopeId,
        LocalDate from,
        LocalDate to,
        String unit
) {
}
