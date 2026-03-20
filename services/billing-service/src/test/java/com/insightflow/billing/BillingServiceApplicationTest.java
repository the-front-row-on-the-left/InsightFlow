package com.insightflow.billing;

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
class BillingServiceApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void returnsBillingByUser() throws Exception {
        mockMvc.perform(get("/api/billing/users/u_demo_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scope_type").value("user"))
                .andExpect(jsonPath("$.data.scope_id").value("u_demo_001"))
                .andExpect(jsonPath("$.data.currency").value("KRW"))
                .andExpect(jsonPath("$.data.price_table_version").value("2026-03-v1"));
    }

    @Test
    void returnsBillingByTeam() throws Exception {
        mockMvc.perform(get("/api/billing/teams/t_demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scope_type").value("team"))
                .andExpect(jsonPath("$.data.scope_id").value("t_demo"))
                .andExpect(jsonPath("$.data.items[0].service_id").value("svc_doc_summary"));
    }

    @Test
    void returnsBillingByWorkflow() throws Exception {
        mockMvc.perform(get("/api/billing/workflows/wf_monthly_report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scope_type").value("workflow"))
                .andExpect(jsonPath("$.data.scope_id").value("wf_monthly_report"))
                .andExpect(jsonPath("$.data.summary.total_cost").value("417.20"));
    }

    @Test
    void registersBillingQueryServiceBean() {
        org.assertj.core.api.Assertions.assertThat(applicationContext.containsBean("billingQueryService")).isTrue();
    }
}
