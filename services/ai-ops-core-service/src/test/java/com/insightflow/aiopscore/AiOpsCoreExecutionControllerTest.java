package com.insightflow.aiopscore;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.insightflow.aiopscore.client.PolicyServiceClient;
import com.insightflow.aiopscore.client.RateLimitServiceClient;
import com.insightflow.aiopscore.domain.PolicyDecision;
import com.insightflow.aiopscore.domain.RateLimitDecision;
import com.insightflow.aiopscore.event.AiOpsCoreEventPublisher;
import com.insightflow.aiopscore.repository.AiOpsCoreExecutionRepository;
import com.insightflow.common.event.KafkaEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AiOpsCoreExecutionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AiOpsCoreExecutionRepository repository;

    @Autowired
    private AiOpsCoreEventPublisher eventPublisher;

    @MockBean
    private PolicyServiceClient policyServiceClient;

    @MockBean
    private RateLimitServiceClient rateLimitServiceClient;

    @MockBean
    private KafkaEventPublisher kafkaEventPublisher;

    @BeforeEach
    void setUp() {
        repository.clear();
        eventPublisher.clear();
        given(policyServiceClient.evaluate(any()))
                .willReturn(new PolicyDecision(true, null, "DEFAULT_ALLOW"));
        given(rateLimitServiceClient.check(any()))
                .willReturn(new RateLimitDecision(true, "user", "u_demo_001", 2, "PASSED", "USER_DAILY_LIMIT"));
    }

    @Test
    void createsExecutionAndPreservesRequestId() throws Exception {
        mockMvc.perform(post("/api/executions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Request-Id", "req_valid_12345")
                        .content("""
                                {
                                  "service_id": "svc_doc_summary",
                                  "workflow_id": "wf_001",
                                  "model": "gpt-4o-mini",
                                  "input": {
                                    "text": "hello gateway"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", "req_valid_12345"))
                .andExpect(jsonPath("$.meta.request_id").value("req_valid_12345"))
                .andExpect(jsonPath("$.data.execution_id").isNotEmpty())
                .andExpect(jsonPath("$.data.request_id").value("req_valid_12345"))
                .andExpect(jsonPath("$.data.status").value("SUCCEEDED"));
    }

    @Test
    void blocksExecutionWhenPolicyDenies() throws Exception {
        given(policyServiceClient.evaluate(any()))
                .willReturn(new PolicyDecision(false, "TEAM_MODEL_DENY", "TEAM_MODEL_DENY"));

        mockMvc.perform(post("/api/executions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "service_id": "svc_doc_summary",
                                  "workflow_id": "wf_001",
                                  "model": "gpt-4o",
                                  "input": {
                                    "text": "blocked"
                                  }
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("POLICY_BLOCKED"));
    }

    @Test
    void exposesExecutionDetails() throws Exception {
        String body = mockMvc.perform(post("/api/executions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Request-Id", "req_detail_123")
                        .content("""
                                {
                                  "service_id": "svc_doc_summary",
                                  "workflow_id": "wf_001",
                                  "model": "gpt-4o-mini",
                                  "input": {
                                    "text": "detail please"
                                  }
                                }
                                """))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String executionId = body.replaceAll("(?s).*\"execution_id\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/api/executions/{executionId}", executionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.execution_id").value(executionId))
                .andExpect(jsonPath("$.data.request_id").value("req_detail_123"))
                .andExpect(jsonPath("$.data.status").value("SUCCEEDED"));
    }
}
