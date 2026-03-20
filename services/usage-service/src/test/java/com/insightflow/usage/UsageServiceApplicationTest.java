package com.insightflow.usage;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
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
                .andExpect(jsonPath("$.data.period.unit").value("day"))
                .andExpect(jsonPath("$.data.summary.total_requests").value(3))
                .andExpect(jsonPath("$.data.summary.total_tokens").value(4460));
    }

    @Test
    void returnsUsageByTeam() throws Exception {
        mockMvc.perform(get("/api/usage/teams/t_demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scope_type").value("team"))
                .andExpect(jsonPath("$.data.scope_id").value("t_demo"))
                .andExpect(jsonPath("$.data.summary.total_requests").value(9));
    }

    @Test
    void returnsUsageByService() throws Exception {
        mockMvc.perform(get("/api/usage/services/svc_doc_summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scope_type").value("service"))
                .andExpect(jsonPath("$.data.scope_id").value("svc_doc_summary"))
                .andExpect(jsonPath("$.data.items[0].model").value("gpt-4o-mini"));
    }

    @Test
    void registersUsageQueryServiceBean() {
        org.assertj.core.api.Assertions.assertThat(applicationContext.containsBean("usageQueryService")).isTrue();
    }
}
