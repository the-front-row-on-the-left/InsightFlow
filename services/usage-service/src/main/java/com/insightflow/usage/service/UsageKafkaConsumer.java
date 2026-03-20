package com.insightflow.usage.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightflow.usage.domain.AiCompletedEvent;
import com.insightflow.usage.domain.AiRequestedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UsageKafkaConsumer {

    private final ObjectMapper objectMapper;
    private final UsageEventMergeService usageEventMergeService;

    public UsageKafkaConsumer(ObjectMapper objectMapper,
                              UsageEventMergeService usageEventMergeService) {
        this.objectMapper = objectMapper;
        this.usageEventMergeService = usageEventMergeService;
    }

    @KafkaListener(topics = "${insightflow.kafka.topics.ai-requested}", groupId = "${spring.application.name}")
    public void consumeRequested(String payload) {
        usageEventMergeService.handleRequested(readValue(payload, AiRequestedEvent.class));
    }

    @KafkaListener(topics = "${insightflow.kafka.topics.ai-completed}", groupId = "${spring.application.name}")
    public void consumeCompleted(String payload) {
        usageEventMergeService.handleCompleted(readValue(payload, AiCompletedEvent.class));
    }

    private <T> T readValue(String payload, Class<T> type) {
        try {
            return objectMapper.readValue(payload, type);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Failed to deserialize Kafka payload for " + type.getSimpleName(), exception);
        }
    }
}
