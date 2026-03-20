package com.insightflow.ratelimit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.insightflow.common.event.KafkaEventPublisher;
import com.insightflow.ratelimit.event.RateLimitEventPublisher;
import com.insightflow.ratelimit.repository.RateLimitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "insightflow.rate-limit.repository-type=in-memory")
@AutoConfigureMockMvc
class RateLimitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RateLimitRepository counterStore;

    @Autowired
    private RateLimitEventPublisher eventPublisher;

    @MockBean
    private KafkaEventPublisher kafkaEventPublisher;

    @BeforeEach
    void setUp() {
        counterStore.clear();
        eventPublisher.clear();
    }

    @Test
    void allowsRequestWithinQuota() throws Exception {
        mockMvc.perform(post("/internal/rate-limit/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "request_id": "req_101",
                                  "user_id": "u_demo_001",
                                  "team_id": "t_demo",
                                  "service_id": "svc_doc_summary",
                                  "workflow_id": "wf_001",
                                  "model": "gpt-4o-mini"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allowed").value(true))
                .andExpect(jsonPath("$.data.result").value("PASSED"));
    }

    @Test
    void blocksWhenUserQuotaExceeded() throws Exception {
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/internal/rate-limit/check")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "request_id": "req_pre_%s",
                              "user_id": "u_demo_001",
                              "team_id": "t_demo",
                              "service_id": "svc_doc_summary",
                              "workflow_id": "wf_001",
                              "model": "gpt-4o-mini"
                            }
                            """.formatted(i))).andExpect(status().isOk());
        }

        mockMvc.perform(post("/internal/rate-limit/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "request_id": "req_blocked",
                                  "user_id": "u_demo_001",
                                  "team_id": "t_demo",
                                  "service_id": "svc_doc_summary",
                                  "workflow_id": "wf_001",
                                  "model": "gpt-4o-mini"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allowed").value(false))
                .andExpect(jsonPath("$.data.applied_rule").value("USER_DAILY_LIMIT"));
    }

    @Test
    void exposesCountersForOperations() throws Exception {
        mockMvc.perform(get("/internal/rate-limit/counters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user_daily_limit").value(3))
                .andExpect(jsonPath("$.data.team_daily_limit").value(5));
    }
}
