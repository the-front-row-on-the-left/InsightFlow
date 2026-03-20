package com.insightflow.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightflow.notification.domain.CostCalculatedEvent;
import com.insightflow.notification.domain.LimitExceededEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationKafkaConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationEventConsumerService notificationEventConsumerService;

    public NotificationKafkaConsumer(ObjectMapper objectMapper,
                                     NotificationEventConsumerService notificationEventConsumerService) {
        this.objectMapper = objectMapper;
        this.notificationEventConsumerService = notificationEventConsumerService;
    }

    @KafkaListener(topics = "${insightflow.kafka.topics.cost-calculated}", groupId = "${spring.application.name}")
    public void consumeCostCalculated(String payload) {
        notificationEventConsumerService.consume(readValue(payload, CostCalculatedEvent.class));
    }

    @KafkaListener(topics = "${insightflow.kafka.topics.limit-exceeded}", groupId = "${spring.application.name}")
    public void consumeLimitExceeded(String payload) {
        notificationEventConsumerService.consume(readValue(payload, LimitExceededEvent.class));
    }

    private <T> T readValue(String payload, Class<T> type) {
        try {
            return objectMapper.readValue(payload, type);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Failed to deserialize Kafka payload for " + type.getSimpleName(), exception);
        }
    }
}
