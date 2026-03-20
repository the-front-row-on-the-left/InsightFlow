package com.insightflow.common.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(String topic, EventEnvelope<?> envelope) {
        try {
            kafkaTemplate.send(topic, envelope.requestId(), envelope)
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            log.warn("event_publish_failed topic={} event_type={} request_id={}",
                                    topic, envelope.eventType(), envelope.requestId(), throwable);
                        }
                    });
        } catch (RuntimeException exception) {
            log.warn("event_publish_failed topic={} event_type={} request_id={}",
                    topic, envelope.eventType(), envelope.requestId(), exception);
        }
    }
}
