package com.insightflow.aiopscore.client;

import java.util.Map;

import com.insightflow.aiopscore.config.AiOpsCoreProperties;
import com.insightflow.aiopscore.domain.ExecutionCommand;
import com.insightflow.aiopscore.domain.RateLimitDecision;
import com.insightflow.common.api.ApiResponse;
import com.insightflow.common.error.BusinessException;
import com.insightflow.common.error.ErrorCode;
import com.insightflow.common.web.RequestContextFilter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class RateLimitServiceClient {

    private static final ParameterizedTypeReference<ApiResponse<RateLimitDecision>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;
    private final AiOpsCoreProperties properties;

    RateLimitServiceClient(RestClient restClient, AiOpsCoreProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public RateLimitDecision check(ExecutionCommand command) {
        try {
            ApiResponse<RateLimitDecision> response = restClient.post()
                    .uri(properties.rateLimitServiceBaseUrl() + "/internal/rate-limit/check")
                    .header(RequestContextFilter.REQUEST_ID_HEADER, command.requestId())
                    .header(RequestContextFilter.USER_ID_HEADER, command.userId())
                    .header(RequestContextFilter.TEAM_ID_HEADER, command.teamId())
                    .header(RequestContextFilter.USER_ROLE_HEADER, command.userRole())
                    .header(RequestContextFilter.ACTOR_TYPE_HEADER, "user")
                    .header(RequestContextFilter.AUTHENTICATED_BY_HEADER, "ai-ops-core")
                    .body(Map.of(
                            "request_id", command.requestId(),
                            "user_id", command.userId(),
                            "team_id", command.teamId(),
                            "service_id", command.serviceId(),
                            "workflow_id", command.workflowId(),
                            "model", command.model()
                    ))
                    .retrieve()
                    .body(RESPONSE_TYPE);
            if (response == null || response.data() == null) {
                throw new BusinessException(HttpStatus.BAD_GATEWAY, ErrorCode.DOWNSTREAM_SERVICE_ERROR,
                        "Rate limit service returned an empty response.");
            }
            return response.data();
        } catch (RestClientException exception) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, ErrorCode.DOWNSTREAM_SERVICE_ERROR,
                    "Rate limit service is unavailable.");
        }
    }
}
