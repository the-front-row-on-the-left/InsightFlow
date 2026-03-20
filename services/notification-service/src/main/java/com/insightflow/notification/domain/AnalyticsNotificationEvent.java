package com.insightflow.notification.domain;

import java.time.Instant;
import java.util.Map;

public interface AnalyticsNotificationEvent {

    String eventType();

    String requestId();

    String userId();

    String teamId();

    String serviceId();

    String workflowId();

    Instant occurredAt();

    Map<String, String> metadata();
}
