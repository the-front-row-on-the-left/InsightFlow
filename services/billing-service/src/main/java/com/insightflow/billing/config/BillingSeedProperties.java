package com.insightflow.billing.config;

import com.insightflow.billing.domain.BillingRequestUsage;
import com.insightflow.billing.domain.PriceTableEntry;
import com.insightflow.billing.domain.PricingModel;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "insightflow.billing")
public record BillingSeedProperties(
        List<PriceTableSeed> priceTables,
        List<RequestSeed> requests
) {

    public BillingSeedProperties {
        priceTables = priceTables == null ? List.of() : List.copyOf(priceTables);
        requests = requests == null ? List.of() : List.copyOf(requests);
    }

    public record PriceTableSeed(
            String priceTableVersion,
            String serviceId,
            String model,
            String pricingModel,
            String currency,
            BigDecimal unitPriceInput,
            BigDecimal unitPriceOutput,
            BigDecimal unitPriceRequest,
            Instant effectiveFrom,
            Instant effectiveTo,
            String status
    ) {

        public PriceTableEntry toPriceTableEntry() {
            return new PriceTableEntry(
                    priceTableVersion,
                    serviceId,
                    model,
                    PricingModel.fromCode(pricingModel),
                    currency,
                    defaultDecimal(unitPriceInput),
                    defaultDecimal(unitPriceOutput),
                    defaultDecimal(unitPriceRequest),
                    effectiveFrom,
                    effectiveTo,
                    status
            );
        }
    }

    public record RequestSeed(
            String requestId,
            String userId,
            String teamId,
            String workflowId,
            String serviceId,
            String model,
            String status,
            int promptTokens,
            int completionTokens,
            boolean billable,
            Instant occurredAt
    ) {

        public BillingRequestUsage toBillingRequestUsage() {
            return new BillingRequestUsage(
                    requestId,
                    userId,
                    teamId,
                    workflowId,
                    serviceId,
                    model,
                    status,
                    promptTokens,
                    completionTokens,
                    billable,
                    occurredAt
            );
        }
    }

    private static BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
