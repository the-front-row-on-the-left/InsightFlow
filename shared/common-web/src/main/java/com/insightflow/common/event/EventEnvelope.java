package com.insightflow.common.event;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EventEnvelope<T>(
        @JsonProperty("event_id")
        String eventId,
        @JsonProperty("event_type")
        String eventType,
        @JsonProperty("occurred_at")
        String occurredAt,
        @JsonProperty("request_id")
        String requestId,
        T payload
) {
}
