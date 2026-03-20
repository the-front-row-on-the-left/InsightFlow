package com.insightflow.billing;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:billing-service;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=true"
})
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
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
                .andExpect(jsonPath("$.data.price_table_version").value("2026-03-v1"))
                .andExpect(jsonPath("$.data.summary.total_cost").value("174.40"))
                .andExpect(jsonPath("$.data.summary.cost_before_rounding").value("174.4000"))
                .andExpect(jsonPath("$.data.summary.item_count").value(1))
                .andExpect(jsonPath("$.data.items[0].pricing_model").value("per_token"))
                .andExpect(jsonPath("$.data.items[0].prompt_tokens").value(900))
                .andExpect(jsonPath("$.data.items[0].completion_tokens").value(320));
    }

    @Test
    void returnsBillingByTeam() throws Exception {
        mockMvc.perform(get("/api/billing/teams/t_demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scope_type").value("team"))
                .andExpect(jsonPath("$.data.scope_id").value("t_demo"))
                .andExpect(jsonPath("$.data.summary.total_cost").value("417.20"))
                .andExpect(jsonPath("$.data.summary.item_count").value(2))
                .andExpect(jsonPath("$.data.items[0].request_id").value("req_t_002"))
                .andExpect(jsonPath("$.data.items[0].service_id").value("svc_report_generator"));
    }

    @Test
    void returnsBillingByWorkflow() throws Exception {
        mockMvc.perform(get("/api/billing/workflows/wf_monthly_report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scope_type").value("workflow"))
                .andExpect(jsonPath("$.data.scope_id").value("wf_monthly_report"))
                .andExpect(jsonPath("$.data.summary.total_cost").value("417.20"))
                .andExpect(jsonPath("$.data.period.from").value("2026-03-01"))
                .andExpect(jsonPath("$.data.period.to").value("2026-03-20"));
    }

    @Test
    void returnsPricingTableByVersion() throws Exception {
        mockMvc.perform(get("/api/billing/pricing-tables/2026-03-v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.price_table_version").value("2026-03-v1"))
                .andExpect(jsonPath("$.data.entries[0].service_id").value("svc_doc_summary"))
                .andExpect(jsonPath("$.data.entries[3].pricing_model").value("fixed"));
    }

    @Test
    void returnsZeroTotalsForUnknownUserScope() throws Exception {
        mockMvc.perform(get("/api/billing/users/u_missing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scope_id").value("u_missing"))
                .andExpect(jsonPath("$.data.summary.total_cost").value("0.00"))
                .andExpect(jsonPath("$.data.summary.item_count").value(0))
                .andExpect(jsonPath("$.data.items").isEmpty());
    }

    @Test
    void registersBillingQueryServiceBean() {
        org.assertj.core.api.Assertions.assertThat(applicationContext.containsBean("billingQueryService")).isTrue();
    }
}
