package com.insightflow.policy;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.insightflow.common.event.KafkaEventPublisher;
import com.insightflow.policy.event.PolicyEventPublisher;
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
class PolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PolicyEventPublisher eventPublisher;

    @MockBean
    private KafkaEventPublisher kafkaEventPublisher;

    @BeforeEach
    void setUp() {
        eventPublisher.clear();
    }

    @Test
    void returnsPolicySummaries() throws Exception {
        mockMvc.perform(get("/internal/policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].policy_id").isNotEmpty());
    }

    @Test
    void blocksConfiguredTeamAndModel() throws Exception {
        mockMvc.perform(post("/internal/policies/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "request_id": "req_001",
                                  "service_id": "svc_doc_summary",
                                  "workflow_id": "wf_001",
                                  "team_id": "t_blocked",
                                  "user_id": "u_demo_001",
                                  "model": "gpt-4o"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allowed").value(false))
                .andExpect(jsonPath("$.data.reason_code").value("TEAM_MODEL_DENY"));
    }

    @Test
    void allowsSupportedServiceAndModel() throws Exception {
        mockMvc.perform(post("/internal/policies/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "request_id": "req_002",
                                  "service_id": "svc_doc_summary",
                                  "workflow_id": "wf_001",
                                  "team_id": "t_demo",
                                  "user_id": "u_demo_001",
                                  "model": "gpt-4o-mini"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allowed").value(true))
                .andExpect(jsonPath("$.data.matched_rule").value("DEFAULT_ALLOW"));
    }
}
