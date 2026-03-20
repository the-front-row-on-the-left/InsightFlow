package com.insightflow.billing.service;

import com.insightflow.billing.config.BillingKafkaProperties;
import com.insightflow.billing.domain.CalculatedCostEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class CostCalculatedPublisher {

    private final KafkaTemplate<String, CalculatedCostEvent> kafkaTemplate;
    private final BillingKafkaProperties billingKafkaProperties;

    public CostCalculatedPublisher(
            KafkaTemplate<String, CalculatedCostEvent> kafkaTemplate,
            BillingKafkaProperties billingKafkaProperties
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.billingKafkaProperties = billingKafkaProperties;
    }

    public void publish(CalculatedCostEvent event) {
        kafkaTemplate.send(billingKafkaProperties.costCalculated(), event.requestId(), event);
    }
}
