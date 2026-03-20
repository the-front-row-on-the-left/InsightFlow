package com.insightflow.usage;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.insightflow.usage.controller.UsageController;
import com.insightflow.usage.repository.InMemoryUsageRecordRepository;
import com.insightflow.usage.service.UsageQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UsageController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({UsageQueryService.class, InMemoryUsageRecordRepository.class})
class UsageServiceApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void returnsUsageByUser() throws Exception {
        mockMvc.perform(get("/api/usage/users/u_demo_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scope_type").value("user"))
                .andExpect(jsonPath("$.data.scope_id").value("u_demo_001"))
                .andExpect(jsonPath("$.data.period.from").value("2026-03-14"))
                .andExpect(jsonPath("$.data.period.to").value("2026-03-20"))
                .andExpect(jsonPath("$.data.period.unit").value("day"))
                .andExpect(jsonPath("$.data.summary.total_requests").value(3))
                .andExpect(jsonPath("$.data.summary.total_tokens").value(4460))
                .andExpect(jsonPath("$.data.summary.avg_tokens_per_request").value(1487))
                .andExpect(jsonPath("$.data.summary.avg_latency_ms").value(304))
                .andExpect(jsonPath("$.data.summary.succeeded_requests").value(2))
                .andExpect(jsonPath("$.data.summary.failed_requests").value(1))
                .andExpect(jsonPath("$.data.items[0].request_id").value("req_u_003"))
                .andExpect(jsonPath("$.data.items[0].workflow_id").value("wf_monthly_report"))
                .andExpect(jsonPath("$.data.items[0].policy_result").value("ALLOWED"))
                .andExpect(jsonPath("$.data.items[0].limit_result").value("PASSED"))
                .andExpect(jsonPath("$.data.items[0].prompt_tokens").value(980))
                .andExpect(jsonPath("$.data.items[0].completion_tokens").value(420));
    }

    @Test
    void returnsUsageByTeamWithinRequestedWindow() throws Exception {
        mockMvc.perform(get("/api/usage/teams/t_demo")
                        .param("from", "2026-03-19")
                        .param("to", "2026-03-20")
                        .param("unit", "week"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scope_type").value("team"))
                .andExpect(jsonPath("$.data.scope_id").value("t_demo"))
                .andExpect(jsonPath("$.data.period.from").value("2026-03-19"))
                .andExpect(jsonPath("$.data.period.to").value("2026-03-20"))
                .andExpect(jsonPath("$.data.period.unit").value("week"))
                .andExpect(jsonPath("$.data.summary.total_requests").value(3))
                .andExpect(jsonPath("$.data.summary.blocked_requests").value(1))
                .andExpect(jsonPath("$.data.items[0].request_id").value("req_u_003"))
                .andExpect(jsonPath("$.data.items[0].status").value("FAILED"))
                .andExpect(jsonPath("$.data.items[1].request_id").value("req_t_005"))
                .andExpect(jsonPath("$.data.items[1].status").value("BLOCKED"));
    }

    @Test
    void returnsUsageByService() throws Exception {
        mockMvc.perform(get("/api/usage/services/svc_doc_summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scope_type").value("service"))
                .andExpect(jsonPath("$.data.scope_id").value("svc_doc_summary"))
                .andExpect(jsonPath("$.data.summary.total_requests").value(6))
                .andExpect(jsonPath("$.data.summary.succeeded_requests").value(4))
                .andExpect(jsonPath("$.data.summary.failed_requests").value(1))
                .andExpect(jsonPath("$.data.summary.blocked_requests").value(1))
                .andExpect(jsonPath("$.data.items[0].request_id").value("req_s_007"))
                .andExpect(jsonPath("$.data.items[0].model").value("gpt-4.1-mini"));
    }

    @Test
    void registersUsageQueryServiceBean() {
        org.assertj.core.api.Assertions.assertThat(applicationContext.getBean(UsageQueryService.class)).isNotNull();
    }
}
