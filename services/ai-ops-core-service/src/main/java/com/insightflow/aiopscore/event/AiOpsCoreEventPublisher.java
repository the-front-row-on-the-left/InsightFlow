package com.insightflow.aiopscore.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import com.insightflow.aiopscore.config.AiOpsCoreProperties;
import com.insightflow.common.event.AiRequestedPayload;
import com.insightflow.common.event.EventEnvelope;
import com.insightflow.common.event.KafkaEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AiOpsCoreEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AiOpsCoreEventPublisher.class);

    private final AiOpsCoreProperties properties;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final CopyOnWriteArrayList<EventEnvelope<AiRequestedPayload>> events = new CopyOnWriteArrayList<>();

    AiOpsCoreEventPublisher(AiOpsCoreProperties properties, KafkaEventPublisher kafkaEventPublisher) {
        this.properties = properties;
        this.kafkaEventPublisher = kafkaEventPublisher;
    }

    public void publishAiRequested(AiRequestedPayload payload) {
        EventEnvelope<AiRequestedPayload> envelope = new EventEnvelope<>(
                "evt_" + UUID.randomUUID(),
                "ai.requested",
                Instant.now().toString(),
                payload.requestId(),
                payload
        );
        events.add(envelope);
        kafkaEventPublisher.publish(properties.aiRequestedTopic(), envelope);
        log.info("event=ai.requested request_id={} service_id={} workflow_id={} input_size={}",
                payload.requestId(), payload.serviceId(), payload.workflowId(), payload.inputSize());
    }

    public List<EventEnvelope<AiRequestedPayload>> snapshotAiRequested() {
        return List.copyOf(events);
    }

    public void clear() {
        events.clear();
    }
}
