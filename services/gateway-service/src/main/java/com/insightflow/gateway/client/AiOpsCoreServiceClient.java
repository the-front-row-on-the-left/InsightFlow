package com.insightflow.gateway.client;

import java.util.Map;

import com.insightflow.common.api.ApiResponse;
import com.insightflow.common.error.BusinessException;
import com.insightflow.common.error.ErrorCode;
import com.insightflow.common.web.RequestContextFilter;
import com.insightflow.gateway.config.GatewayProperties;
import com.insightflow.gateway.domain.ExecutionCreateRequest;
import com.insightflow.gateway.domain.ExecutionCreateResponse;
import com.insightflow.gateway.domain.ExecutionDetailResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class AiOpsCoreServiceClient {

    private static final ParameterizedTypeReference<ApiResponse<ExecutionCreateResponse>> CREATE_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<ApiResponse<ExecutionDetailResponse>> DETAIL_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<ApiResponse<Map<String, Object>>> SNAPSHOT_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;
    private final GatewayProperties properties;

    AiOpsCoreServiceClient(RestClient restClient, GatewayProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public ExecutionCreateResponse createExecution(ExecutionCreateRequest request,
                                                   String requestId,
                                                   String userId,
                                                   String teamId,
                                                   String userRole) {
        try {
            ApiResponse<ExecutionCreateResponse> response = restClient.post()
                    .uri(properties.aiOpsCoreServiceBaseUrl() + "/api/executions")
                    .header(RequestContextFilter.REQUEST_ID_HEADER, requestId)
                    .header(RequestContextFilter.USER_ID_HEADER, userId)
                    .header(RequestContextFilter.TEAM_ID_HEADER, teamId)
                    .header(RequestContextFilter.USER_ROLE_HEADER, userRole)
                    .header(RequestContextFilter.ACTOR_TYPE_HEADER, "user")
                    .header(RequestContextFilter.AUTHENTICATED_BY_HEADER, "gateway")
                    .body(request)
                    .retrieve()
                    .body(CREATE_RESPONSE_TYPE);
            if (response == null || response.data() == null) {
                throw new BusinessException(HttpStatus.BAD_GATEWAY, ErrorCode.DOWNSTREAM_SERVICE_ERROR,
                        "AI Ops Core returned an empty response.");
            }
            return response.data();
        } catch (RestClientException exception) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, ErrorCode.DOWNSTREAM_SERVICE_ERROR,
                    "AI Ops Core service is unavailable.");
        }
    }

    public ExecutionDetailResponse getExecution(String executionId,
                                                String requestId,
                                                String userId,
                                                String teamId,
                                                String userRole) {
        try {
            ApiResponse<ExecutionDetailResponse> response = restClient.get()
                    .uri(properties.aiOpsCoreServiceBaseUrl() + "/api/executions/{executionId}", executionId)
                    .header(RequestContextFilter.REQUEST_ID_HEADER, requestId)
                    .header(RequestContextFilter.USER_ID_HEADER, userId)
                    .header(RequestContextFilter.TEAM_ID_HEADER, teamId)
                    .header(RequestContextFilter.USER_ROLE_HEADER, userRole)
                    .header(RequestContextFilter.ACTOR_TYPE_HEADER, "user")
                    .header(RequestContextFilter.AUTHENTICATED_BY_HEADER, "gateway")
                    .retrieve()
                    .body(DETAIL_RESPONSE_TYPE);
            if (response == null || response.data() == null) {
                throw new BusinessException(HttpStatus.BAD_GATEWAY, ErrorCode.DOWNSTREAM_SERVICE_ERROR,
                        "AI Ops Core returned an empty response.");
            }
            return response.data();
        } catch (RestClientException exception) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, ErrorCode.DOWNSTREAM_SERVICE_ERROR,
                    "AI Ops Core service is unavailable.");
        }
    }

    public Map<String, Object> operationsSnapshot(String requestId,
                                                  String userId,
                                                  String teamId,
                                                  String userRole) {
        try {
            ApiResponse<Map<String, Object>> response = restClient.get()
                    .uri(properties.aiOpsCoreServiceBaseUrl() + "/internal/executions")
                    .header(RequestContextFilter.REQUEST_ID_HEADER, requestId)
                    .header(RequestContextFilter.USER_ID_HEADER, userId)
                    .header(RequestContextFilter.TEAM_ID_HEADER, teamId)
                    .header(RequestContextFilter.USER_ROLE_HEADER, userRole)
                    .header(RequestContextFilter.ACTOR_TYPE_HEADER, "user")
                    .header(RequestContextFilter.AUTHENTICATED_BY_HEADER, "gateway")
                    .retrieve()
                    .body(SNAPSHOT_RESPONSE_TYPE);
            if (response == null || response.data() == null) {
                throw new BusinessException(HttpStatus.BAD_GATEWAY, ErrorCode.DOWNSTREAM_SERVICE_ERROR,
                        "AI Ops Core returned an empty response.");
            }
            return response.data();
        } catch (RestClientException exception) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, ErrorCode.DOWNSTREAM_SERVICE_ERROR,
                    "AI Ops Core service is unavailable.");
        }
    }
}
