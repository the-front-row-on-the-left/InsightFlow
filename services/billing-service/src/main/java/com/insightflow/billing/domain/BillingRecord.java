package com.insightflow.billing.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record BillingRecord(
        String requestId,
        String userId,
        String teamId,
        String workflowId,
        String serviceId,
        String model,
        String status,
        PricingModel pricingModel,
        String currency,
        String priceTableVersion,
        int promptTokens,
        int completionTokens,
        int totalTokens,
        boolean billable,
        BigDecimal inputUnitPrice,
        BigDecimal outputUnitPrice,
        BigDecimal requestUnitPrice,
        BigDecimal costBeforeRounding,
        BigDecimal totalCost,
        Instant occurredAt
) {
}
