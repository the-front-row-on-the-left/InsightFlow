package com.insightflow.ratelimit.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import com.insightflow.ratelimit.controller.dto.RateLimitCheckRequest;
import com.insightflow.ratelimit.controller.dto.RateLimitCheckResponse;
import com.insightflow.common.event.EventEnvelope;
import com.insightflow.common.event.KafkaEventPublisher;
import com.insightflow.common.event.LimitAppliedPayload;
import com.insightflow.ratelimit.config.RateLimitProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RateLimitEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RateLimitEventPublisher.class);

    private final RateLimitProperties properties;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final CopyOnWriteArrayList<EventEnvelope<LimitAppliedPayload>> events = new CopyOnWriteArrayList<>();

    RateLimitEventPublisher(RateLimitProperties properties, KafkaEventPublisher kafkaEventPublisher) {
        this.properties = properties;
        this.kafkaEventPublisher = kafkaEventPublisher;
    }

    public void publish(RateLimitCheckRequest request, RateLimitCheckResponse response) {
        LimitAppliedPayload payload = new LimitAppliedPayload(
                request.requestId(),
                response.scope(),
                response.scopeId(),
                response.result(),
                response.remainingQuota()
        );
        EventEnvelope<LimitAppliedPayload> envelope = new EventEnvelope<>(
                "evt_" + UUID.randomUUID(),
                "limit.applied",
                Instant.now().toString(),
                request.requestId(),
                payload
        );
        events.add(envelope);
        kafkaEventPublisher.publish(properties.limitAppliedTopic(), envelope);
        log.info("event=limit.applied request_id={} scope={} scope_id={} result={} remaining_quota={}",
                request.requestId(), response.scope(), response.scopeId(), response.result(), response.remainingQuota());
    }

    public List<EventEnvelope<LimitAppliedPayload>> snapshot() {
        return List.copyOf(events);
    }

    public void clear() {
        events.clear();
    }
}
