package com.insightflow.aiopscore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AiOpsCoreProperties {

    private final String policyServiceBaseUrl;
    private final String rateLimitServiceBaseUrl;
    private final String defaultWorkflowId;
    private final String aiRequestedTopic;

    AiOpsCoreProperties(@Value("${insightflow.upstream.policy-service-base-url:http://localhost:8081}") String policyServiceBaseUrl,
                        @Value("${insightflow.upstream.rate-limit-service-base-url:http://localhost:8082}") String rateLimitServiceBaseUrl,
                        @Value("${insightflow.execution.default-workflow-id:wf_ad_hoc}") String defaultWorkflowId,
                        @Value("${insightflow.kafka.topics.ai-requested:ai.requested}") String aiRequestedTopic) {
        this.policyServiceBaseUrl = policyServiceBaseUrl;
        this.rateLimitServiceBaseUrl = rateLimitServiceBaseUrl;
        this.defaultWorkflowId = defaultWorkflowId;
        this.aiRequestedTopic = aiRequestedTopic;
    }

    public String policyServiceBaseUrl() {
        return policyServiceBaseUrl;
    }

    public String rateLimitServiceBaseUrl() {
        return rateLimitServiceBaseUrl;
    }

    public String defaultWorkflowId() {
        return defaultWorkflowId;
    }

    public String aiRequestedTopic() {
        return aiRequestedTopic;
    }
}
