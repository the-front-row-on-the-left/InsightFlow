package com.insightflow.billing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "insightflow.kafka.topics")
public record BillingKafkaProperties(
        String usageTracked,
        String costCalculated
) {
}
