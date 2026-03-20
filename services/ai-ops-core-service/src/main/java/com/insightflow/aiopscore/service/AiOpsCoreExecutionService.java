package com.insightflow.aiopscore.service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.insightflow.aiopscore.client.PolicyServiceClient;
import com.insightflow.aiopscore.client.RateLimitServiceClient;
import com.insightflow.aiopscore.config.AiOpsCoreProperties;
import com.insightflow.aiopscore.domain.ExecutionCommand;
import com.insightflow.aiopscore.domain.ExecutionCreateRequest;
import com.insightflow.aiopscore.domain.ExecutionCreateResponse;
import com.insightflow.aiopscore.domain.ExecutionDetailResponse;
import com.insightflow.aiopscore.domain.ExecutionRecord;
import com.insightflow.aiopscore.domain.ExecutionResult;
import com.insightflow.aiopscore.domain.PolicyDecision;
import com.insightflow.aiopscore.domain.RateLimitDecision;
import com.insightflow.aiopscore.event.AiOpsCoreEventPublisher;
import com.insightflow.aiopscore.provider.AiOpsCoreDocumentSearchProvider;
import com.insightflow.aiopscore.provider.AiOpsCoreMockAiProvider;
import com.insightflow.aiopscore.repository.AiOpsCoreExecutionRepository;
import com.insightflow.common.error.BusinessException;
import com.insightflow.common.error.ErrorCode;
import com.insightflow.common.web.InsightRequestContextHolder;
import com.insightflow.common.web.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AiOpsCoreExecutionService {

    private static final Logger log = LoggerFactory.getLogger(AiOpsCoreExecutionService.class);

    private final AiOpsCoreProperties properties;
    private final PolicyServiceClient policyServiceClient;
    private final RateLimitServiceClient rateLimitServiceClient;
    private final AiOpsCoreDocumentSearchProvider documentSearchProvider;
    private final AiOpsCoreMockAiProvider mockAiProvider;
    private final AiOpsCoreExecutionRepository repository;
    private final AiOpsCoreEventPublisher eventPublisher;

    AiOpsCoreExecutionService(AiOpsCoreProperties properties,
                              PolicyServiceClient policyServiceClient,
                              RateLimitServiceClient rateLimitServiceClient,
                              AiOpsCoreDocumentSearchProvider documentSearchProvider,
                              AiOpsCoreMockAiProvider mockAiProvider,
                              AiOpsCoreExecutionRepository repository,
                              AiOpsCoreEventPublisher eventPublisher) {
        this.properties = properties;
        this.policyServiceClient = policyServiceClient;
        this.rateLimitServiceClient = rateLimitServiceClient;
        this.documentSearchProvider = documentSearchProvider;
        this.mockAiProvider = mockAiProvider;
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    public ExecutionCreateResponse createExecution(ExecutionCreateRequest request) {
        RequestContext requestContext = InsightRequestContextHolder.getCurrent()
                .orElseThrow(() -> new IllegalStateException("Missing request context."));
        validate(request);

        ExecutionCommand command = new ExecutionCommand(
                "exe_" + UUID.randomUUID(),
                requestContext.requestId(),
                requestContext.userId(),
                requestContext.teamId(),
                requestContext.userRole(),
                request.serviceId().trim(),
                normalizeWorkflowId(request.workflowId()),
                request.model().trim(),
                request.input()
        );
        eventPublisher.publishAiRequested(command);

        PolicyDecision policyDecision = policyServiceClient.evaluate(command);
        if (!policyDecision.allowed()) {
            saveAndPublish(command, "BLOCKED_POLICY", ErrorCode.POLICY_BLOCKED.name(), policyDecision, null, null);
            throw new BusinessException(HttpStatus.FORBIDDEN, ErrorCode.POLICY_BLOCKED,
                    buildPolicyMessage(policyDecision));
        }

        RateLimitDecision rateLimitDecision = rateLimitServiceClient.check(command);
        if (!rateLimitDecision.allowed()) {
            saveAndPublish(command, "BLOCKED_RATE_LIMIT", ErrorCode.RATE_LIMIT_EXCEEDED.name(), policyDecision,
                    rateLimitDecision, null);
            throw new BusinessException(HttpStatus.TOO_MANY_REQUESTS, ErrorCode.RATE_LIMIT_EXCEEDED,
                    buildRateLimitMessage(rateLimitDecision));
        }

        try {
            ExecutionResult result = executeProvider(command);
            ExecutionRecord record = saveAndPublish(command, "SUCCEEDED", null, policyDecision, rateLimitDecision, result);
            log.info("execution_id={} request_id={} service_id={} workflow_id={} status=SUCCEEDED",
                    command.executionId(), command.requestId(), command.serviceId(), command.workflowId());
            return record.toCreateResponse();
        } catch (BusinessException exception) {
            saveAndPublish(command, "FAILED", exception.errorCode().name(), policyDecision, rateLimitDecision, null);
            throw exception;
        }
    }

    public ExecutionDetailResponse getExecution(String executionId) {
        return repository.findById(executionId)
                .map(ExecutionRecord::toDetailResponse)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, ErrorCode.INVALID_REQUEST,
                        "Execution not found."));
    }

    public Map<String, Object> operationsSnapshot() {
        return Map.of(
                "executions", repository.findAll().stream().map(ExecutionRecord::toDetailResponse).toList(),
                "ai_requested_events", eventPublisher.snapshotAiRequested(),
                "ai_completed_events", eventPublisher.snapshotAiCompleted()
        );
    }

    private void validate(ExecutionCreateRequest request) {
        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST, "Execution request is required.");
        }
        if (isBlank(request.serviceId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST, "service_id is required.");
        }
        if (isBlank(request.model())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST, "model is required.");
        }
        if (request.input() == null || request.input().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST, "input is required.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalizeWorkflowId(String workflowId) {
        if (workflowId == null || workflowId.isBlank()) {
            return properties.defaultWorkflowId();
        }
        return workflowId.trim();
    }

    private String buildPolicyMessage(PolicyDecision decision) {
        if (decision.reasonCode() == null || decision.reasonCode().isBlank()) {
            return "Execution was blocked by policy.";
        }
        return "Execution was blocked by policy: " + decision.reasonCode();
    }

    private String buildRateLimitMessage(RateLimitDecision decision) {
        String scope = decision.scope() == null || decision.scope().isBlank() ? "unknown" : decision.scope();
        String scopeId = decision.scopeId() == null || decision.scopeId().isBlank() ? "unknown" : decision.scopeId();
        String appliedRule = decision.appliedRule() == null || decision.appliedRule().isBlank() ? "unknown" : decision.appliedRule();
        return "Request quota has been exceeded for scope "
                + scope
                + " ("
                + scopeId
                + "). Remaining quota: "
                + decision.remainingQuota()
                + ". Applied rule: "
                + appliedRule
                + ".";
    }

    private ExecutionResult executeProvider(ExecutionCommand command) {
        if ("svc_doc_search".equals(command.serviceId())) {
            return documentSearchProvider.execute(command);
        }
        return mockAiProvider.execute(command);
    }

    private ExecutionRecord saveAndPublish(ExecutionCommand command,
                                           String status,
                                           String errorCode,
                                           PolicyDecision policyDecision,
                                           RateLimitDecision rateLimitDecision,
                                           ExecutionResult result) {
        ExecutionRecord record = new ExecutionRecord(
                command.executionId(),
                command.requestId(),
                command.userId(),
                command.teamId(),
                command.serviceId(),
                command.workflowId(),
                command.model(),
                status,
                errorCode,
                policyDecision,
                rateLimitDecision,
                result,
                Instant.now()
        );
        repository.save(record);
        eventPublisher.publishAiCompleted(command, status, result, "SUCCEEDED".equals(status));
        return record;
    }
}
