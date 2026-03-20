package com.insightflow.usage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "insightflow.kafka.topics")
public record UsageKafkaProperties(
        String aiRequested,
        String aiCompleted,
        String usageTracked
) {
}
