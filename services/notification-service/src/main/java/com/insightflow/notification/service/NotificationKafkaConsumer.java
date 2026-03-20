package com.insightflow.notification.service;

import com.insightflow.notification.domain.CostCalculatedEvent;
import com.insightflow.notification.domain.LimitExceededEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationKafkaConsumer {

    private final NotificationEventConsumerService notificationEventConsumerService;

    public NotificationKafkaConsumer(NotificationEventConsumerService notificationEventConsumerService) {
        this.notificationEventConsumerService = notificationEventConsumerService;
    }

    @KafkaListener(topics = "${insightflow.kafka.topics.cost-calculated}", groupId = "${spring.application.name}")
    public void consumeCostCalculated(CostCalculatedEvent event) {
        notificationEventConsumerService.consume(event);
    }

    @KafkaListener(topics = "${insightflow.kafka.topics.limit-exceeded}", groupId = "${spring.application.name}")
    public void consumeLimitExceeded(LimitExceededEvent event) {
        notificationEventConsumerService.consume(event);
    }
}
