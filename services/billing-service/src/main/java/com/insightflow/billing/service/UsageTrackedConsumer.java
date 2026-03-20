package com.insightflow.billing.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightflow.billing.domain.BillingRecord;
import com.insightflow.billing.domain.BillingRequestUsage;
import com.insightflow.billing.domain.CalculatedCostEvent;
import com.insightflow.billing.domain.PriceTableEntry;
import com.insightflow.billing.domain.UsageTrackedEvent;
import com.insightflow.billing.repository.BillingRecordRepository;
import com.insightflow.billing.repository.PricingTableRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.UUID;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UsageTrackedConsumer {

    private final ObjectMapper objectMapper;
    private final PricingTableRepository pricingTableRepository;
    private final BillingRecordRepository billingRecordRepository;
    private final BillingCostCalculator billingCostCalculator;
    private final CostCalculatedPublisher costCalculatedPublisher;

    public UsageTrackedConsumer(
            ObjectMapper objectMapper,
            PricingTableRepository pricingTableRepository,
            BillingRecordRepository billingRecordRepository,
            BillingCostCalculator billingCostCalculator,
            CostCalculatedPublisher costCalculatedPublisher
    ) {
        this.objectMapper = objectMapper;
        this.pricingTableRepository = pricingTableRepository;
        this.billingRecordRepository = billingRecordRepository;
        this.billingCostCalculator = billingCostCalculator;
        this.costCalculatedPublisher = costCalculatedPublisher;
    }

    @KafkaListener(topics = "${insightflow.kafka.topics.usage-tracked}", groupId = "${spring.application.name}")
    public void consume(String payload) {
        UsageTrackedEvent event = readValue(payload, UsageTrackedEvent.class);
        if (billingRecordRepository.existsByRequestId(event.requestId())) {
            return;
        }

        BillingRequestUsage usage = new BillingRequestUsage(
                event.requestId(),
                event.userId(),
                event.teamId(),
                event.workflowId(),
                event.serviceId(),
                event.model(),
                event.status(),
                event.promptTokens(),
                event.completionTokens(),
                event.billable(),
                event.occurredAt().toInstant()
        );
        PriceTableEntry priceTableEntry = pricingTableRepository.findAll().stream()
                .filter(entry -> entry.serviceId().equals(event.serviceId()))
                .filter(entry -> entry.model().equals(event.model()))
                .filter(entry -> entry.isActiveFor(usage.occurredAt()))
                .max(Comparator.comparing(PriceTableEntry::effectiveFrom))
                .orElseThrow(() -> new IllegalStateException("No active pricing entry for " + event.requestId()));

        BillingRecord billingRecord = billingCostCalculator.calculate(usage, priceTableEntry);
        billingRecordRepository.save(event.eventId(), billingRecord);
        costCalculatedPublisher.publish(new CalculatedCostEvent(
                "evt_cost_" + UUID.randomUUID(),
                CalculatedCostEvent.EVENT_TYPE,
                billingRecord.requestId(),
                billingRecord.userId(),
                billingRecord.teamId(),
                billingRecord.workflowId(),
                billingRecord.serviceId(),
                billingRecord.model(),
                billingRecord.currency(),
                billingRecord.totalCost(),
                billingRecord.priceTableVersion(),
                billingRecord.billable(),
                billingRecord.status(),
                OffsetDateTime.now(ZoneOffset.UTC)
        ));
    }

    private <T> T readValue(String payload, Class<T> type) {
        try {
            return objectMapper.readValue(payload, type);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Failed to deserialize Kafka payload for " + type.getSimpleName(), exception);
        }
    }
}
