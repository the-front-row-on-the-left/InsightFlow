package com.insightflow.billing.dto;

public record BillingStatusResponse(
        String service,
        String status
) {
}
