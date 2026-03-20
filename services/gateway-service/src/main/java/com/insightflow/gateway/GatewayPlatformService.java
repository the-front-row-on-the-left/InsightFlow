package com.insightflow.gateway;

import com.insightflow.common.error.ErrorCode;
import com.insightflow.common.error.BusinessException;
import com.insightflow.common.web.InsightRequestContextHolder;
import com.insightflow.common.web.RequestContext;
import com.insightflow.gateway.client.AiOpsCoreServiceClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
class GatewayPlatformService {

    private final DocumentSearchEngine documentSearchEngine;
    private final AiOpsCoreServiceClient aiOpsCoreServiceClient;
    private final ConcurrentMap<String, WorkflowRecord> workflows = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ExecutionRecord> executions = new ConcurrentHashMap<>();
    private final Map<String, ServiceDefinition> serviceCatalog;

    GatewayPlatformService(DocumentSearchEngine documentSearchEngine,
                           AiOpsCoreServiceClient aiOpsCoreServiceClient) {
        this.documentSearchEngine = documentSearchEngine;
        this.aiOpsCoreServiceClient = aiOpsCoreServiceClient;
        this.serviceCatalog = buildServiceCatalog();
        WorkflowRecord defaultWorkflow = new WorkflowRecord(
                "wf_template_doc_report",
                "문서 검색 후 보고서 생성",
                List.of(
                        new WorkflowStepPayload(1, "svc_doc_search", "검색 결과를 context로 수집합니다."),
                        new WorkflowStepPayload(2, "svc_report_generator", "검색 결과로 보고서를 생성합니다.")
                ),
                Instant.now().toString(),
                Instant.now().toString(),
                "doc_search_to_report"
        );
        workflows.put(defaultWorkflow.workflow_id(), defaultWorkflow);
    }

    List<ServiceCatalogItemResponse> listCatalogServices(String category, String tag, String keyword) {
        String normalizedCategory = normalize(category);
        String normalizedTag = normalize(tag);
        String normalizedKeyword = normalize(keyword);

        return serviceCatalog.values().stream()
                .filter(service -> normalizedCategory.isBlank() || normalize(service.category()).contains(normalizedCategory))
                .filter(service -> normalizedTag.isBlank() || service.tags().stream().map(this::normalize).anyMatch(candidate -> candidate.contains(normalizedTag)))
                .filter(service -> matchesKeyword(service, normalizedKeyword))
                .map(ServiceDefinition::toCatalogItem)
                .toList();
    }

    ServiceDetailResponse getCatalogService(String serviceId) {
        return requireService(serviceId).toDetailResponse();
    }

    WorkflowRecord createWorkflow(WorkflowCreateRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new GatewayApiException(ErrorCode.INVALID_REQUEST, HttpStatus.BAD_REQUEST, "워크플로우 이름을 입력해야 합니다.");
        }
        if (request.steps() == null || request.steps().size() < 1) {
            throw new GatewayApiException(ErrorCode.INVALID_REQUEST, HttpStatus.BAD_REQUEST, "워크플로우에는 최소 1개의 step이 필요합니다.");
        }

        List<WorkflowStepPayload> normalizedSteps = new ArrayList<>();
        int order = 1;
        for (WorkflowStepRequest step : request.steps()) {
            requireService(step.service_id());
            normalizedSteps.add(new WorkflowStepPayload(
                    step.order() == null ? order : step.order(),
                    step.service_id(),
                    step.notes()
            ));
            order += 1;
        }

        String now = Instant.now().toString();
        WorkflowRecord workflow = new WorkflowRecord(
                "wf_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8),
                request.name().trim(),
                normalizedSteps,
                now,
                now,
                isDocSearchReportTemplate(normalizedSteps) ? "doc_search_to_report" : null
        );
        workflows.put(workflow.workflow_id(), workflow);
        return workflow;
    }

    WorkflowRecord getWorkflow(String workflowId) {
        WorkflowRecord workflow = workflows.get(workflowId);
        if (workflow == null) {
            throw new GatewayApiException(ErrorCode.INVALID_REQUEST, HttpStatus.NOT_FOUND, "워크플로우를 찾을 수 없습니다.");
        }
        return workflow;
    }

    ExecutionCreateResponse createExecution(ExecutionCreateRequest request) {
        String requestId = currentRequestId();
        String createdAt = Instant.now().toString();
        String executionId = "exe_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        Map<String, Object> input = request.input() == null ? Map.of() : new LinkedHashMap<>(request.input());

        ExecutionErrorPayload triggeredError = detectTriggeredError(input);
        if (triggeredError != null) {
            ExecutionRecord failure = failureRecord(
                    executionId,
                    request.service_id(),
                    request.workflow_id(),
                    request.model(),
                    request.workflow_id() != null && !request.workflow_id().isBlank() ? "gateway-workflow" : "gateway",
                    requestId,
                    input,
                    createdAt,
                    triggeredError
            );
            executions.put(executionId, failure);
            throw asGatewayApiException(triggeredError);
        }

        if ((request.service_id() == null || request.service_id().isBlank()) && (request.workflow_id() == null || request.workflow_id().isBlank())) {
            ExecutionErrorPayload error = new ExecutionErrorPayload("INVALID_REQUEST", "service_id 또는 workflow_id 중 하나가 필요합니다.", Map.of(
                    "service_id", "service_id 또는 workflow_id를 지정하세요."
            ));
            ExecutionRecord failure = failureRecord(
                    executionId,
                    request.service_id(),
                    request.workflow_id(),
                    request.model(),
                    "gateway",
                    requestId,
                    input,
                    createdAt,
                    error
            );
            executions.put(executionId, failure);
            throw asGatewayApiException(error);
        }

        try {
            ExecutionRecord execution = request.workflow_id() != null && !request.workflow_id().isBlank()
                    ? executeWorkflow(executionId, request, requestId, createdAt, input)
                    : executeSingleService(request, requestId);

            executions.put(execution.execution_id(), execution);
            return execution.toCreateResponse();
        } catch (GatewayApiException exception) {
            executions.put(executionId, failureRecord(
                    executionId,
                    request.service_id(),
                    request.workflow_id(),
                    request.model(),
                    request.workflow_id() != null && !request.workflow_id().isBlank() ? "gateway-workflow" : "gateway",
                    requestId,
                    input,
                    createdAt,
                    toErrorPayload(exception)
            ));
            throw exception;
        } catch (BusinessException exception) {
            executions.put(executionId, failureRecord(
                    executionId,
                    request.service_id(),
                    request.workflow_id(),
                    request.model(),
                    "ai-ops-core",
                    requestId,
                    input,
                    createdAt,
                    toErrorPayload(exception)
            ));
            throw exception;
        }
    }

    ExecutionRecord getExecution(String executionId) {
        ExecutionRecord execution = executions.get(executionId);
        if (execution == null) {
            throw new GatewayApiException(ErrorCode.INVALID_REQUEST, HttpStatus.NOT_FOUND, "실행 결과를 찾을 수 없습니다.");
        }
        return execution;
    }

    List<ExecutionStreamItem> listExecutions() {
        return executions.values().stream()
                .sorted((left, right) -> right.created_at().compareTo(left.created_at()))
                .map(this::toExecutionStreamItem)
                .toList();
    }

    private ExecutionRecord executeSingleService(ExecutionCreateRequest request,
                                                 String requestId) {
        requireService(request.service_id());
        if ("svc_doc_search".equals(request.service_id())) {
            Object rawQuery = request.input() == null ? null : request.input().get("query");
            if (rawQuery == null || rawQuery.toString().isBlank()) {
                throw new GatewayApiException(
                        ErrorCode.INVALID_REQUEST,
                        HttpStatus.BAD_REQUEST,
                        "문서 검색에는 query 입력이 필요합니다."
                );
            }
        }
        RequestContext requestContext = currentRequestContext();
        com.insightflow.gateway.domain.ExecutionCreateResponse createResponse = aiOpsCoreServiceClient.createExecution(
                new com.insightflow.gateway.domain.ExecutionCreateRequest(
                        request.service_id(),
                        request.workflow_id(),
                        request.input(),
                        request.model()
                ),
                requestId,
                requestContext.userId(),
                requestContext.teamId(),
                requestContext.userRole()
        );
        com.insightflow.gateway.domain.ExecutionDetailResponse detailResponse = aiOpsCoreServiceClient.getExecution(
                createResponse.executionId(),
                requestId,
                requestContext.userId(),
                requestContext.teamId(),
                requestContext.userRole()
        );
        return mapAiOpsCoreExecution(detailResponse);
    }

    private ExecutionRecord executeWorkflow(String executionId,
                                            ExecutionCreateRequest request,
                                            String requestId,
                                            String createdAt,
                                            Map<String, Object> input) {
        WorkflowRecord workflow = getWorkflow(request.workflow_id());
        List<Map<String, Object>> stepResults = new ArrayList<>();
        Object finalOutput = null;
        String carriedContext = "";

        for (WorkflowStepPayload step : workflow.steps()) {
            Map<String, Object> raw;
            String summary;

            if ("svc_doc_search".equals(step.service_id())) {
                String query = Objects.toString(input.getOrDefault("query", workflow.name()), workflow.name());
                String scope = Objects.toString(input.get("document_scope"), "all-documents");
                DocSearchPayload docResult = documentSearchEngine.search(query, scope);
                raw = mapDocSearch(docResult);
                summary = docResult.answer_summary();
                carriedContext = docResult.answer_summary() + "\n" + docResult.top_chunks().stream()
                        .map(TopChunkPayload::snippet)
                        .reduce("", (left, right) -> left + (left.isBlank() ? "" : "\n") + right);
                finalOutput = raw;
            } else if ("svc_chat_assistant".equals(step.service_id())) {
                raw = Map.of(
                        "type", "chat_assistant",
                        "answer", "워크플로우 문맥을 요약한 mock 답변입니다.",
                        "bullet_points", List.of("검색 결과를 재정리합니다.", "핵심 포인트를 3개로 압축합니다.", "후속 step 입력으로 넘깁니다.")
                );
                summary = raw.get("answer").toString();
                carriedContext = summary;
                finalOutput = raw;
            } else {
                raw = buildReportResult(
                        Objects.toString(input.getOrDefault("topic", workflow.name()), workflow.name()),
                        carriedContext.isBlank() ? "문서 검색 결과를 context로 사용합니다." : carriedContext,
                        Objects.toString(input.get("format"), "executive_summary")
                );
                summary = raw.get("report_text").toString();
                finalOutput = raw;
            }

            stepResults.add(Map.of(
                    "order", step.order(),
                    "service_id", step.service_id(),
                    "title", requireService(step.service_id()).name(),
                    "summary", summary,
                    "status", "SUCCEEDED",
                    "raw", raw
            ));
        }

        Map<String, Object> workflowResult = Map.of(
                "type", "workflow",
                "workflow_name", workflow.name(),
                "step_results", stepResults,
                "final_output", finalOutput == null ? Map.of() : finalOutput
        );

        return successRecord(executionId, workflow.steps().get(0).service_id(), workflow.workflow_id(), requestId, input, createdAt,
                workflowResult, "completed", "available", "workflow 결과가 생성되었습니다.", request.model(), "gateway-workflow");
    }

    private ExecutionErrorPayload detectTriggeredError(Map<String, Object> input) {
        String text = input.values().stream()
                .map(String::valueOf)
                .map(this::normalize)
                .reduce("", (left, right) -> left + " " + right)
                .trim();

        if (text.contains("policy_block")) {
            return new ExecutionErrorPayload("POLICY_BLOCKED", "정책에 의해 요청이 차단되었습니다.", Map.of());
        }
        if (text.contains("rate_limit")) {
            return new ExecutionErrorPayload("RATE_LIMIT_EXCEEDED", "요청 제한을 초과했습니다. 잠시 후 다시 시도하세요.", Map.of());
        }
        if (text.contains("provider_error")) {
            return new ExecutionErrorPayload("AI_PROVIDER_ERROR", "AI provider 호출 중 오류가 발생했습니다.", Map.of());
        }
        return null;
    }

    private GatewayApiException asGatewayApiException(ExecutionErrorPayload error) {
        HttpStatus status = switch (error.code()) {
            case "INVALID_REQUEST" -> HttpStatus.BAD_REQUEST;
            case "POLICY_BLOCKED" -> HttpStatus.FORBIDDEN;
            case "RATE_LIMIT_EXCEEDED" -> HttpStatus.TOO_MANY_REQUESTS;
            case "AI_PROVIDER_ERROR" -> HttpStatus.BAD_GATEWAY;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        return new GatewayApiException(ErrorCode.valueOf(error.code()), status, error.message());
    }

    private ExecutionErrorPayload toErrorPayload(GatewayApiException exception) {
        return new ExecutionErrorPayload(
                exception.errorCode().name(),
                exception.getMessage(),
                Map.of()
        );
    }

    private ExecutionErrorPayload toErrorPayload(BusinessException exception) {
        return new ExecutionErrorPayload(
                exception.errorCode().name(),
                exception.getMessage(),
                Map.of()
        );
    }

    private ExecutionRecord failureRecord(String executionId,
                                          String serviceId,
                                          String workflowId,
                                          String model,
                                          String source,
                                          String requestId,
                                          Map<String, Object> input,
                                          String createdAt,
                                          ExecutionErrorPayload error) {
        return new ExecutionRecord(
                executionId,
                serviceId,
                workflowId,
                "FAILED",
                requestId,
                source,
                model,
                input,
                null,
                error,
                "pending",
                "empty",
                "실패 실행에는 추천이 제공되지 않습니다.",
                createdAt,
                createdAt
        );
    }

    private ExecutionRecord successRecord(String executionId,
                                          String serviceId,
                                          String workflowId,
                                          String requestId,
                                          Map<String, Object> input,
                                          String createdAt,
                                          Object result,
                                          String costStatus,
                                          String recommendationState,
                                          String recommendationMessage,
                                          String model,
                                          String source) {
        return new ExecutionRecord(
                executionId,
                serviceId,
                workflowId,
                "SUCCEEDED",
                requestId,
                source,
                model,
                input,
                result,
                null,
                costStatus,
                recommendationState,
                recommendationMessage,
                createdAt,
                Instant.now().toString()
        );
    }

    private Map<String, Object> buildReportResult(String topic, String context, String format) {
        return Map.of(
                "type", "report_generator",
                "report_text", topic + " 보고서 초안입니다. " + context,
                "sections", List.of("요약", "핵심 발견", "권고사항"),
                "format", format
        );
    }

    private Map<String, Object> mapDocSearch(DocSearchPayload payload) {
        return Map.of(
                "type", payload.type(),
                "answer_summary", payload.answer_summary(),
                "top_chunks", payload.top_chunks(),
                "citations", payload.citations(),
                "document_scope", payload.document_scope()
        );
    }

    private ServiceDefinition requireService(String serviceId) {
        ServiceDefinition service = serviceCatalog.get(serviceId);
        if (service == null) {
            throw new GatewayApiException(ErrorCode.INVALID_REQUEST, HttpStatus.BAD_REQUEST, "지원하지 않는 service_id 입니다: " + serviceId);
        }
        return service;
    }

    private boolean matchesKeyword(ServiceDefinition service, String keyword) {
        if (keyword.isBlank()) {
            return true;
        }
        String haystack = normalize(String.join(" ",
                service.service_id(),
                service.name(),
                service.category(),
                service.short_description(),
                service.description(),
                String.join(" ", service.tags())
        ));
        return haystack.contains(keyword);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isDocSearchReportTemplate(List<WorkflowStepPayload> steps) {
        return steps.size() == 2
                && "svc_doc_search".equals(steps.get(0).service_id())
                && "svc_report_generator".equals(steps.get(1).service_id());
    }

    private String currentRequestId() {
        return InsightRequestContextHolder.getCurrent()
                .map(RequestContext::requestId)
                .orElse("unknown");
    }

    private RequestContext currentRequestContext() {
        return InsightRequestContextHolder.getCurrent()
                .orElse(new RequestContext(currentRequestId(), "u_demo_001", "t_demo", "admin"));
    }

    private ExecutionRecord mapAiOpsCoreExecution(com.insightflow.gateway.domain.ExecutionDetailResponse detailResponse) {
        Object payload = detailResponse.result() == null ? Map.of() : detailResponse.result().payload();
        String createdAt = detailResponse.createdAt() == null ? Instant.now().toString() : detailResponse.createdAt().toString();
        return new ExecutionRecord(
                detailResponse.executionId(),
                detailResponse.serviceId(),
                detailResponse.workflowId(),
                detailResponse.status(),
                detailResponse.requestId(),
                "ai-ops-core",
                detailResponse.model(),
                Map.of(),
                payload,
                detailResponse.errorCode() == null ? null : new ExecutionErrorPayload(detailResponse.errorCode(), "AI Ops Core execution failed.", Map.of()),
                "pending",
                "pending",
                "비동기 집계 중입니다.",
                createdAt,
                Instant.now().toString()
        );
    }

    private ExecutionStreamItem toExecutionStreamItem(ExecutionRecord record) {
        Instant startedAt = parseInstant(record.created_at());
        Instant finishedAt = parseInstant(record.completed_at());
        Long durationMs = startedAt != null && finishedAt != null ? Math.max(0L, finishedAt.toEpochMilli() - startedAt.toEpochMilli()) : null;
        return new ExecutionStreamItem(
                record.execution_id(),
                record.request_id(),
                record.source() == null || record.source().isBlank() ? "gateway" : record.source(),
                record.service_id(),
                record.workflow_id(),
                record.model() == null || record.model().isBlank() ? "-" : record.model(),
                normalizeExecutionStatus(record.status()),
                record.created_at(),
                record.completed_at(),
                durationMs,
                record.error() == null ? null : record.error().message()
        );
    }

    private String normalizeExecutionStatus(String status) {
        if (status == null || status.isBlank()) {
            return "FAILED";
        }
        if (status.startsWith("BLOCKED")) {
            return "BLOCKED";
        }
        return status;
    }

    private Instant parseInstant(String value) {
        try {
            return value == null || value.isBlank() ? null : Instant.parse(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Map<String, ServiceDefinition> buildServiceCatalog() {
        Map<String, ServiceDefinition> services = new LinkedHashMap<>();
        services.put("svc_doc_search", new ServiceDefinition(
                "svc_doc_search",
                "문서 검색",
                "Knowledge Retrieval",
                "per_request",
                "기업 문서에서 근거를 찾아 요약 응답을 반환합니다.",
                List.of("gpt-4.1-mini", "gpt-4.1", "internal-doc-search"),
                "첫 단계 컨텍스트 수집",
                List.of("RAG", "citation", "search"),
                true,
                "real",
                "사내 운영 정책, 보안 핸드북, billing 문서를 대상으로 retrieval과 요약을 수행하는 실제 vertical slice입니다.",
                List.of(
                        new ExampleFieldPayload("query", "2026년 AI 사용량 비용 산정 기준은 무엇인가?"),
                        new ExampleFieldPayload("document_scope", "billing, ops")
                ),
                new ResultExamplePayload(
                        "비용 산정 기준 요약",
                        "정책 문서와 운영 가이드의 근거를 묶어 한 문단으로 요약합니다.",
                        List.of("요약 답변", "근거 chunk", "citation")
                )
        ));
        services.put("svc_chat_assistant", new ServiceDefinition(
                "svc_chat_assistant",
                "챗 어시스턴트",
                "Conversation",
                "per_token",
                "간단한 질의응답과 초안 생성을 빠르게 처리합니다.",
                List.of("gpt-5.4", "gpt-5-mini"),
                "중간 초안 생성 후보",
                List.of("qa", "draft", "summary"),
                false,
                "mock",
                "이번 단계에서는 mock 결과로 동작하는 보조 서비스입니다.",
                List.of(
                        new ExampleFieldPayload("question", "이번 주 운영 리포트 초안을 작성해줘."),
                        new ExampleFieldPayload("tone", "executive")
                ),
                new ResultExamplePayload(
                        "운영 요약 초안",
                        "짧은 요약 본문과 bullet point를 반환합니다.",
                        List.of("답변 본문", "핵심 bullet")
                )
        ));
        services.put("svc_report_generator", new ServiceDefinition(
                "svc_report_generator",
                "보고서 생성기",
                "Reporting",
                "per_request",
                "검색 결과나 입력 문맥을 바탕으로 보고서를 생성합니다.",
                List.of("gpt-5.4", "gpt-5-mini"),
                "마지막 단계 결과 생성",
                List.of("report", "workflow", "sections"),
                false,
                "mock",
                "이번 단계에서는 workflow 마지막 step 시연을 위한 mock 생성기입니다.",
                List.of(
                        new ExampleFieldPayload("topic", "Q1 AI Ops 운영 현황"),
                        new ExampleFieldPayload("format", "executive_summary")
                ),
                new ResultExamplePayload(
                        "경영진 보고서",
                        "서론, 현황, 권고사항으로 구성된 텍스트 보고서를 생성합니다.",
                        List.of("본문 텍스트", "섹션 목록")
                )
        ));
        return services;
    }
}

record ServiceCatalogItemResponse(
        String service_id,
        String name,
        String category,
        String pricing_model,
        String short_description,
        List<String> supported_models,
        String workflow_role,
        List<String> tags,
        boolean recommended,
        String execution_mode
) {
}

record ServiceDetailResponse(
        String service_id,
        String name,
        String category,
        String pricing_model,
        String short_description,
        List<String> supported_models,
        String workflow_role,
        List<String> tags,
        boolean recommended,
        String execution_mode,
        String description,
        List<ExampleFieldPayload> input_examples,
        ResultExamplePayload result_example
) {
}

record ExampleFieldPayload(
        String label,
        String value
) {
}

record ResultExamplePayload(
        String headline,
        String description,
        List<String> items
) {
}

record WorkflowCreateRequest(
        String name,
        List<WorkflowStepRequest> steps
) {
}

record WorkflowStepRequest(
        Integer order,
        String service_id,
        String notes
) {
}

record WorkflowStepPayload(
        Integer order,
        String service_id,
        String notes
) {
}

record WorkflowRecord(
        String workflow_id,
        String name,
        List<WorkflowStepPayload> steps,
        String created_at,
        String updated_at,
        String selected_template
) {
}

record ExecutionCreateRequest(
        String service_id,
        String workflow_id,
        Map<String, Object> input,
        String model
) {
}

record ExecutionCreateResponse(
        String execution_id,
        String service_id,
        String workflow_id,
        String status,
        String request_id
) {
}

record ExecutionErrorPayload(
        String code,
        String message,
        Map<String, String> details
) {
}

record ExecutionRecord(
        String execution_id,
        String service_id,
        String workflow_id,
        String status,
        String request_id,
        String source,
        String model,
        Map<String, Object> input,
        Object result,
        ExecutionErrorPayload error,
        String cost_status,
        String recommendation_state,
        String recommendation_message,
        String created_at,
        String completed_at
) {
    ExecutionCreateResponse toCreateResponse() {
        return new ExecutionCreateResponse(execution_id, service_id, workflow_id, status, request_id);
    }
}

record ExecutionStreamItem(
        String execution_id,
        String request_id,
        String source,
        String service_id,
        String workflow_id,
        String model,
        String status,
        String started_at,
        String finished_at,
        Long duration_ms,
        String error_message
) {
}

record ServiceDefinition(
        String service_id,
        String name,
        String category,
        String pricing_model,
        String short_description,
        List<String> supported_models,
        String workflow_role,
        List<String> tags,
        boolean recommended,
        String execution_mode,
        String description,
        List<ExampleFieldPayload> input_examples,
        ResultExamplePayload result_example
) {
    ServiceCatalogItemResponse toCatalogItem() {
        return new ServiceCatalogItemResponse(
                service_id,
                name,
                category,
                pricing_model,
                short_description,
                supported_models,
                workflow_role,
                tags,
                recommended,
                execution_mode
        );
    }

    ServiceDetailResponse toDetailResponse() {
        return new ServiceDetailResponse(
                service_id,
                name,
                category,
                pricing_model,
                short_description,
                supported_models,
                workflow_role,
                tags,
                recommended,
                execution_mode,
                description,
                input_examples,
                result_example
        );
    }
}
