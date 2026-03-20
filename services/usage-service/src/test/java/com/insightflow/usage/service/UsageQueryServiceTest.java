package com.insightflow.usage.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.insightflow.usage.dto.UsageItem;
import com.insightflow.usage.dto.UsageScopeResponse;
import com.insightflow.usage.repository.InMemoryUsageRecordRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class UsageQueryServiceTest {

    private final UsageQueryService usageQueryService = new UsageQueryService(new InMemoryUsageRecordRepository());

    @Test
    void aggregatesUsageByUserFromSeededRecords() {
        UsageScopeResponse response = usageQueryService.getUserUsage("u_demo_001", null, null, "day");

        assertThat(response.scopeType()).isEqualTo("user");
        assertThat(response.scopeId()).isEqualTo("u_demo_001");
        assertThat(response.summary().totalRequests()).isEqualTo(3);
        assertThat(response.summary().totalTokens()).isEqualTo(4460);
        assertThat(response.summary().avgTokensPerRequest()).isEqualTo(1487);
        assertThat(response.summary().avgLatencyMs()).isEqualTo(304);
        assertThat(response.summary().succeededRequests()).isEqualTo(2);
        assertThat(response.summary().failedRequests()).isEqualTo(1);
        assertThat(response.summary().blockedRequests()).isZero();
        assertThat(response.items()).extracting(UsageItem::requestId)
                .containsExactly("req_u_003", "req_u_002", "req_u_001");
    }

    @Test
    void filtersUsageByRequestedDateRange() {
        UsageScopeResponse response = usageQueryService.getUserUsage(
                "u_demo_001",
                LocalDate.parse("2026-03-18"),
                LocalDate.parse("2026-03-20"),
                "week"
        );

        assertThat(response.period().from()).isEqualTo("2026-03-18");
        assertThat(response.period().to()).isEqualTo("2026-03-20");
        assertThat(response.period().unit()).isEqualTo("week");
        assertThat(response.summary().totalRequests()).isEqualTo(2);
        assertThat(response.summary().totalTokens()).isEqualTo(3240);
        assertThat(response.items()).extracting(UsageItem::requestId)
                .containsExactly("req_u_003", "req_u_002");
    }

    @Test
    void returnsZeroSummaryForMissingScope() {
        UsageScopeResponse response = usageQueryService.getServiceUsage("svc_unknown", null, null, "day");

        assertThat(response.scopeType()).isEqualTo("service");
        assertThat(response.scopeId()).isEqualTo("svc_unknown");
        assertThat(response.period().from()).isEqualTo("2026-03-14");
        assertThat(response.period().to()).isEqualTo("2026-03-20");
        assertThat(response.summary().totalRequests()).isZero();
        assertThat(response.summary().totalTokens()).isZero();
        assertThat(response.summary().succeededRequests()).isZero();
        assertThat(response.summary().failedRequests()).isZero();
        assertThat(response.summary().blockedRequests()).isZero();
        assertThat(response.items()).isEmpty();
    }
}
