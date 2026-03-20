package com.insightflow.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
    void policyBlockedRequestReturnsStructuredErrorEnvelope() throws Exception {
        mockMvc.perform(post("/api/executions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "service_id": "svc_doc_search",
                                  "input": {
                                    "query": "please policy_block this request",
                                    "document_scope": "billing"
                                  }
                                }
                                """))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("POLICY_BLOCKED"))
                .andExpect(jsonPath("$.error.message").isNotEmpty())
                .andExpect(jsonPath("$.meta.request_id").isNotEmpty());
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
