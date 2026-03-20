package com.insightflow.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GatewayProperties {

    private final String aiOpsCoreServiceBaseUrl;

    GatewayProperties(@Value("${insightflow.upstream.ai-ops-core-service-base-url:http://localhost:8087}") String aiOpsCoreServiceBaseUrl) {
        this.aiOpsCoreServiceBaseUrl = aiOpsCoreServiceBaseUrl;
    }

    public String aiOpsCoreServiceBaseUrl() {
        return aiOpsCoreServiceBaseUrl;
    }
}
