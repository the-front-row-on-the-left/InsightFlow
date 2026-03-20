package com.insightflow.usage.service;

import com.insightflow.usage.domain.AiCompletedEvent;
import com.insightflow.usage.domain.AiRequestedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UsageKafkaConsumer {

    private final UsageEventMergeService usageEventMergeService;

    public UsageKafkaConsumer(UsageEventMergeService usageEventMergeService) {
        this.usageEventMergeService = usageEventMergeService;
    }

    @KafkaListener(topics = "${insightflow.kafka.topics.ai-requested}", groupId = "${spring.application.name}")
    public void consumeRequested(AiRequestedEvent event) {
        usageEventMergeService.handleRequested(event);
    }

    @KafkaListener(topics = "${insightflow.kafka.topics.ai-completed}", groupId = "${spring.application.name}")
    public void consumeCompleted(AiCompletedEvent event) {
        usageEventMergeService.handleCompleted(event);
    }
}
