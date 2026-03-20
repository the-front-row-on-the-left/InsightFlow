package com.insightflow.aiopscore.event;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import com.insightflow.aiopscore.config.AiOpsCoreProperties;
import com.insightflow.aiopscore.domain.ExecutionCommand;
import com.insightflow.aiopscore.domain.ExecutionResult;
import com.insightflow.common.event.KafkaEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AiOpsCoreEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AiOpsCoreEventPublisher.class);

    private final AiOpsCoreProperties properties;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final CopyOnWriteArrayList<AiRequestedEventPayload> requestedEvents = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<AiCompletedEventPayload> completedEvents = new CopyOnWriteArrayList<>();

    AiOpsCoreEventPublisher(AiOpsCoreProperties properties, KafkaEventPublisher kafkaEventPublisher) {
        this.properties = properties;
        this.kafkaEventPublisher = kafkaEventPublisher;
    }

    public void publishAiRequested(ExecutionCommand command) {
        AiRequestedEventPayload payload = new AiRequestedEventPayload(
                "evt_" + UUID.randomUUID(),
                "ai.requested",
                command.requestId(),
                command.userId(),
                command.teamId(),
                command.serviceId(),
                command.workflowId(),
                command.model(),
                OffsetDateTime.now(ZoneOffset.UTC)
        );
        requestedEvents.add(payload);
        kafkaEventPublisher.publish(properties.aiRequestedTopic(), payload.requestId(), payload);
        log.info("event=ai.requested request_id={} service_id={} workflow_id={} input_size={}",
                command.requestId(), command.serviceId(), command.workflowId(), command.inputSize());
    }

    public void publishAiCompleted(ExecutionCommand command, String status, ExecutionResult result, boolean billable) {
        int promptTokens = result == null || result.promptTokens() == null ? 0 : result.promptTokens();
        int completionTokens = result == null || result.completionTokens() == null ? 0 : result.completionTokens();
        int totalTokens = result == null || result.totalTokens() == null ? 0 : result.totalTokens();
        int latencyMs = result == null || result.latencyMs() == null ? 0 : result.latencyMs().intValue();

        AiCompletedEventPayload payload = new AiCompletedEventPayload(
                "evt_" + UUID.randomUUID(),
                "ai.completed",
                command.requestId(),
                command.model(),
                status,
                promptTokens,
                completionTokens,
                totalTokens,
                latencyMs,
                billable,
                OffsetDateTime.now(ZoneOffset.UTC)
        );
        completedEvents.add(payload);
        kafkaEventPublisher.publish(properties.aiCompletedTopic(), payload.requestId(), payload);
        log.info("event=ai.completed request_id={} service_id={} workflow_id={} status={} total_tokens={} latency_ms={}",
                command.requestId(), command.serviceId(), command.workflowId(), status, totalTokens, latencyMs);
    }

    public List<AiRequestedEventPayload> snapshotAiRequested() {
        return List.copyOf(requestedEvents);
    }

    public List<AiCompletedEventPayload> snapshotAiCompleted() {
        return List.copyOf(completedEvents);
    }

    public void clear() {
        requestedEvents.clear();
        completedEvents.clear();
    }
}
