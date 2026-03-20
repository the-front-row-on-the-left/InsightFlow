package com.insightflow.usage.service;

import com.insightflow.usage.config.UsageKafkaProperties;
import com.insightflow.usage.domain.TrackedUsageEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class UsageTrackedEventPublisher {

    private final KafkaTemplate<String, TrackedUsageEvent> kafkaTemplate;
    private final UsageKafkaProperties usageKafkaProperties;

    public UsageTrackedEventPublisher(
            KafkaTemplate<String, TrackedUsageEvent> kafkaTemplate,
            UsageKafkaProperties usageKafkaProperties
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.usageKafkaProperties = usageKafkaProperties;
    }

    public void publish(TrackedUsageEvent trackedUsageEvent) {
        kafkaTemplate.send(usageKafkaProperties.usageTracked(), trackedUsageEvent.requestId(), trackedUsageEvent);
    }
}
