package com.insightflow.policy.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import com.insightflow.common.event.KafkaEventPublisher;
import com.insightflow.policy.controller.dto.PolicyEvaluationRequest;
import com.insightflow.policy.controller.dto.PolicyEvaluationResponse;
import com.insightflow.policy.config.PolicyProperties;
import com.insightflow.common.event.EventEnvelope;
import com.insightflow.common.event.PolicyCheckedPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PolicyEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PolicyEventPublisher.class);

    private final PolicyProperties properties;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final CopyOnWriteArrayList<EventEnvelope<PolicyCheckedPayload>> events = new CopyOnWriteArrayList<>();

    PolicyEventPublisher(PolicyProperties properties, KafkaEventPublisher kafkaEventPublisher) {
        this.properties = properties;
        this.kafkaEventPublisher = kafkaEventPublisher;
    }

    public void publish(String teamId, PolicyEvaluationRequest request, PolicyEvaluationResponse response) {
        PolicyCheckedPayload payload = new PolicyCheckedPayload(
                request.requestId(),
                "team",
                teamId,
                response.allowed() ? "ALLOWED" : "BLOCKED",
                response.rulesApplied()
        );
        EventEnvelope<PolicyCheckedPayload> envelope = new EventEnvelope<>(
                "evt_" + UUID.randomUUID(),
                "policy.checked",
                Instant.now().toString(),
                request.requestId(),
                payload
        );
        events.add(envelope);
        kafkaEventPublisher.publish(properties.policyCheckedTopic(), envelope);
        log.info("event=policy.checked request_id={} team_id={} result={} matched_rule={}",
                request.requestId(), teamId, payload.result(), response.matchedRule());
    }

    public List<EventEnvelope<PolicyCheckedPayload>> snapshot() {
        return List.copyOf(events);
    }

    public void clear() {
        events.clear();
    }
}
