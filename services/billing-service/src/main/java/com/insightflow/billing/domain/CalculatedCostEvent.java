package com.insightflow.billing.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CalculatedCostEvent(
        String eventId,
        String eventType,
        String requestId,
        String userId,
        String teamId,
        String workflowId,
        String serviceId,
        String model,
        String currency,
        BigDecimal costAmount,
        String priceTableVersion,
        boolean billable,
        String status,
        OffsetDateTime calculatedAt
) {

    public static final String EVENT_TYPE = "cost.calculated";

    public CalculatedCostEvent {
        eventType = eventType == null || eventType.isBlank() ? EVENT_TYPE : eventType;
    }
}
