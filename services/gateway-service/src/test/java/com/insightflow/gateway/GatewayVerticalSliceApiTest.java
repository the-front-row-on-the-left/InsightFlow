package com.insightflow.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightflow.gateway.client.AiOpsCoreServiceClient;
import com.insightflow.gateway.domain.ExecutionCreateResponse;
import com.insightflow.gateway.domain.ExecutionDetailResponse;
import com.insightflow.gateway.domain.ExecutionResult;
import com.insightflow.gateway.domain.OrchestrationTargets;
import com.insightflow.gateway.domain.PolicyDecision;
import com.insightflow.gateway.domain.RateLimitDecision;
import com.insightflow.common.error.BusinessException;
import com.insightflow.common.error.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "insightflow.ai.openai.enabled=false")
@AutoConfigureMockMvc
class GatewayVerticalSliceApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiOpsCoreServiceClient aiOpsCoreServiceClient;

    @Test
    void catalogEndpointsExposeDocumentSearchContract() throws Exception {
        JsonNode catalogResponse = readJson(mockMvc.perform(get("/api/catalog/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.meta.request_id").isNotEmpty())
                .andReturn());

        JsonNode services = catalogResponse.path("data");
        assertThat(services.isArray()).isTrue();

        JsonNode documentSearchService = findByField(services, "service_id", "svc_doc_search");
        assertThat(documentSearchService).isNotNull();
        assertThat(documentSearchService.path("name").asText()).isNotBlank();
        assertThat(documentSearchService.path("supported_models").isArray()).isTrue();
        assertThat(documentSearchService.path("supported_models").get(0).asText()).isEqualTo("gpt-4.1-mini");
        assertThat(documentSearchService.path("execution_mode").asText()).isEqualTo("real");

        mockMvc.perform(get("/api/catalog/services/svc_doc_search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.meta.request_id").isNotEmpty())
                .andExpect(jsonPath("$.data.service_id").value("svc_doc_search"))
                .andExpect(jsonPath("$.data.supported_models[0]").isNotEmpty())
                .andExpect(jsonPath("$.data.input_examples[0].label").isNotEmpty())
                .andExpect(jsonPath("$.data.result_example.headline").isNotEmpty());
    }

    @Test
    void docSearchExecutionCreateAndDetailReturnsRagPayload() throws Exception {
        given(aiOpsCoreServiceClient.createExecution(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()))
                .willReturn(new ExecutionCreateResponse(
                        "exe_proxy_001",
                        "req_proxy_001",
                        "svc_doc_search",
                        "wf_ad_hoc",
                        "SUCCEEDED",
                        new OrchestrationTargets("policy-service", "rate-limit-service", "document-search-provider"),
                        new ExecutionResult(
                                "document-search-provider",
                                Map.of(
                                        "type", "doc_search",
                                        "answer_summary", "billing 문서 근거를 바탕으로 정책을 요약했습니다.",
                                        "top_chunks", List.of(Map.of("doc_id", "doc_bill_018", "snippet", "청구 집계는 일 단위 잠정 계산 후 월말 확정 상태로 전환된다.", "score", 0.91)),
                                        "citations", List.of(Map.of("doc_id", "doc_bill_018", "title", "월간 Billing Runbook", "section", "4.3")),
                                        "document_scope", "billing"
                                ),
                                "billing 문서 근거를 바탕으로 정책을 요약했습니다.",
                                120,
                                88,
                                208,
                                420L
                        )
                ));
        given(aiOpsCoreServiceClient.getExecution(
                org.mockito.ArgumentMatchers.eq("exe_proxy_001"),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()))
                .willReturn(new ExecutionDetailResponse(
                        "exe_proxy_001",
                        "req_proxy_001",
                        "u_demo_001",
                        "t_demo",
                        "svc_doc_search",
                        "wf_ad_hoc",
                        "gpt-5.4-mini",
                        "SUCCEEDED",
                        null,
                        new PolicyDecision(true, null, "DEFAULT_ALLOW"),
                        new RateLimitDecision(true, "user", "u_demo_001", 9, "PASSED", "USER_DAILY_LIMIT"),
                        new ExecutionResult(
                                "document-search-provider",
                                Map.of(
                                        "type", "doc_search",
                                        "answer_summary", "billing 문서 근거를 바탕으로 정책을 요약했습니다.",
                                        "top_chunks", List.of(Map.of("doc_id", "doc_bill_018", "snippet", "청구 집계는 일 단위 잠정 계산 후 월말 확정 상태로 전환된다.", "score", 0.91)),
                                        "citations", List.of(Map.of("doc_id", "doc_bill_018", "title", "월간 Billing Runbook", "section", "4.3")),
                                        "document_scope", "billing"
                                ),
                                "billing 문서 근거를 바탕으로 정책을 요약했습니다.",
                                120,
                                88,
                                208,
                                420L
                        ),
                        Instant.parse("2026-03-20T08:00:00Z")
                ));

        JsonNode createResponse = readJson(mockMvc.perform(post("/api/executions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "service_id": "svc_doc_search",
                                  "model": "gpt-5.4-mini",
                                  "input": {
                                    "query": "billing settlement policy",
                                    "document_scope": "billing"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.meta.request_id").isNotEmpty())
                .andReturn());

        String executionId = createResponse.path("data").path("execution_id").asText();
        assertThat(executionId).startsWith("exe_");
        assertThat(createResponse.path("data").path("service_id").asText()).isEqualTo("svc_doc_search");
        assertThat(createResponse.path("data").path("request_id").asText()).startsWith("req_");

        mockMvc.perform(get("/api/executions/{executionId}", executionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.execution_id").value(executionId))
                .andExpect(jsonPath("$.data.service_id").value("svc_doc_search"))
                .andExpect(jsonPath("$.data.request_id").isNotEmpty())
                .andExpect(jsonPath("$.data.result.type").value("doc_search"))
                .andExpect(jsonPath("$.data.result.answer_summary").isNotEmpty())
                .andExpect(jsonPath("$.data.result.top_chunks[0].doc_id").isNotEmpty())
                .andExpect(jsonPath("$.data.result.top_chunks[0].snippet").isNotEmpty())
                .andExpect(jsonPath("$.data.result.citations[0].title").isNotEmpty())
                .andExpect(jsonPath("$.data.result.document_scope").value("billing"));
    }

    @Test
    void workflowCreateDetailAndRerunPathReturnStoredExecutionResult() throws Exception {
        String workflowName = "문서 검색 후 보고서 생성 " + UUID.randomUUID();

        JsonNode workflowCreateResponse = readJson(mockMvc.perform(post("/api/workflows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "steps": [
                                    { "service_id": "svc_doc_search" },
                                    { "service_id": "svc_report_generator" }
                                  ]
                                }
                                """.formatted(workflowName)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.meta.request_id").isNotEmpty())
                .andReturn());

        String workflowId = workflowCreateResponse.path("data").path("workflow_id").asText();
        assertThat(workflowId).startsWith("wf_");

        mockMvc.perform(get("/api/workflows/{workflowId}", workflowId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.workflow_id").value(workflowId))
                .andExpect(jsonPath("$.data.name").value(workflowName))
                .andExpect(jsonPath("$.data.steps[0].service_id").value("svc_doc_search"))
                .andExpect(jsonPath("$.data.steps[1].service_id").value("svc_report_generator"));

        JsonNode rerunResponse = readJson(mockMvc.perform(post("/api/executions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "workflow_id": "%s",
                                  "input": {
                                    "query": "billing settlement policy",
                                    "document_scope": "billing",
                                    "topic": "%s"
                                  }
                                }
                                """.formatted(workflowId, workflowName)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.meta.request_id").isNotEmpty())
                .andReturn());

        String executionId = rerunResponse.path("data").path("execution_id").asText();
        assertThat(executionId).startsWith("exe_");
        assertThat(rerunResponse.path("data").path("workflow_id").asText()).isEqualTo(workflowId);

        mockMvc.perform(get("/api/executions/{executionId}", executionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.workflow_id").value(workflowId))
                .andExpect(jsonPath("$.data.result.type").value("workflow"))
                .andExpect(jsonPath("$.data.result.workflow_name").value(workflowName))
                .andExpect(jsonPath("$.data.result.step_results[0].service_id").value("svc_doc_search"))
                .andExpect(jsonPath("$.data.result.step_results[1].service_id").value("svc_report_generator"))
                .andExpect(jsonPath("$.data.result.final_output").isMap());
    }

    @Test
    void executionsListReturnsRecentExecutionStreamItems() throws Exception {
        given(aiOpsCoreServiceClient.createExecution(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()))
                .willReturn(new ExecutionCreateResponse(
                        "exe_proxy_list_001",
                        "req_proxy_list_001",
                        "svc_doc_search",
                        "wf_ad_hoc",
                        "SUCCEEDED",
                        new OrchestrationTargets("policy-service", "rate-limit-service", "document-search-provider"),
                        null
                ));
        given(aiOpsCoreServiceClient.getExecution(
                org.mockito.ArgumentMatchers.eq("exe_proxy_list_001"),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()))
                .willReturn(new ExecutionDetailResponse(
                        "exe_proxy_list_001",
                        "req_proxy_list_001",
                        "u_demo_001",
                        "t_demo",
                        "svc_doc_search",
                        "wf_ad_hoc",
                        "gpt-5.4-mini",
                        "SUCCEEDED",
                        null,
                        new PolicyDecision(true, null, "DEFAULT_ALLOW"),
                        new RateLimitDecision(true, "user", "u_demo_001", 8, "PASSED", "USER_DAILY_LIMIT"),
                        new ExecutionResult("document-search-provider", Map.of("type", "doc_search"), "ok", 10, 5, 15, 120L),
                        Instant.parse("2026-03-20T08:00:00Z")
                ));

        mockMvc.perform(post("/api/executions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "service_id": "svc_doc_search",
                                  "model": "gpt-5.4-mini",
                                  "input": {
                                    "query": "billing settlement policy",
                                    "document_scope": "billing"
                                  }
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/executions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].execution_id").value("exe_proxy_list_001"))
                .andExpect(jsonPath("$.data[0].source").value("ai-ops-core"))
                .andExpect(jsonPath("$.data[0].model").value("gpt-5.4-mini"))
                .andExpect(jsonPath("$.data[0].status").value("SUCCEEDED"))
                .andExpect(jsonPath("$.data[0].duration_ms").isNumber())
                .andExpect(jsonPath("$.data[0].error_message").isEmpty());
    }

    @Test
    void invalidRequestReturnsStructuredErrorEnvelope() throws Exception {
        mockMvc.perform(post("/api/executions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "service_id": "svc_doc_search",
                                  "input": {
                                    "document_scope": "billing"
                                  }
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.error.message").isNotEmpty())
                .andExpect(jsonPath("$.meta.request_id").isNotEmpty());
    }

    @Test
    void rateLimitedRequestReturnsStructuredErrorEnvelopeAndExecutionHistory() throws Exception {
        given(aiOpsCoreServiceClient.createExecution(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()))
                .willThrow(new BusinessException(
                        HttpStatus.TOO_MANY_REQUESTS,
                        ErrorCode.RATE_LIMIT_EXCEEDED,
                        "Request quota has been exceeded for the current scope."
                ));

        mockMvc.perform(post("/api/executions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "service_id": "svc_doc_search",
                                  "input": {
                                    "query": "billing settlement policy",
                                    "document_scope": "billing"
                                  }
                                }
                                """))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RATE_LIMIT_EXCEEDED"))
                .andExpect(jsonPath("$.error.message").isNotEmpty())
                .andExpect(jsonPath("$.meta.request_id").isNotEmpty());

        mockMvc.perform(get("/api/executions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("FAILED"))
                .andExpect(jsonPath("$.data[0].source").value("ai-ops-core"))
                .andExpect(jsonPath("$.data[0].error_message").value("Request quota has been exceeded for the current scope."));
    }

    private JsonNode readJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode findByField(JsonNode items, String fieldName, String fieldValue) {
        for (JsonNode item : items) {
            if (fieldValue.equals(item.path(fieldName).asText())) {
                return item;
            }
        }
        return null;
    }
}
