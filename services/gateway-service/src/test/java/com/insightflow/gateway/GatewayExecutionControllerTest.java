package com.insightflow.gateway;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.insightflow.gateway.client.AiOpsCoreServiceClient;
import com.insightflow.gateway.domain.ExecutionCreateResponse;
import com.insightflow.gateway.domain.ExecutionDetailResponse;
import com.insightflow.gateway.domain.ExecutionResult;
import com.insightflow.gateway.domain.OrchestrationTargets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class GatewayExecutionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AiOpsCoreServiceClient aiOpsCoreServiceClient;

    @Test
    void proxiesExecutionCreateToAiOpsCore() throws Exception {
        given(aiOpsCoreServiceClient.createExecution(any(), eq("req_valid_12345"), eq("u_demo_001"), eq("t_demo"), eq("platform_user")))
                .willReturn(new ExecutionCreateResponse(
                        "exe_123",
                        "req_valid_12345",
                        "svc_doc_summary",
                        "wf_001",
                        "SUCCEEDED",
                        new OrchestrationTargets("policy-service", "rate-limit-service", "mock-ai-provider"),
                        new ExecutionResult("mock-ai-provider", "proxied output", 12, 26, 38, 145L)
                ));

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
                .andExpect(jsonPath("$.data.execution_id").value("exe_123"))
                .andExpect(jsonPath("$.data.result.output").value("proxied output"));
    }

    @Test
    void proxiesExecutionDetailToAiOpsCore() throws Exception {
        given(aiOpsCoreServiceClient.getExecution(eq("exe_123"), any(), any(), any(), any()))
                .willReturn(new ExecutionDetailResponse(
                        "exe_123",
                        "req_detail_123",
                        "u_demo_001",
                        "t_demo",
                        "svc_doc_summary",
                        "wf_001",
                        "gpt-4o-mini",
                        "SUCCEEDED",
                        null,
                        null,
                        null,
                        new ExecutionResult("mock-ai-provider", "detail output", 12, 26, 38, 145L),
                        java.time.Instant.parse("2026-03-20T00:00:00Z")
                ));

        mockMvc.perform(get("/api/executions/{executionId}", "exe_123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.execution_id").value("exe_123"))
                .andExpect(jsonPath("$.data.result.output").value("detail output"));
    }
}
