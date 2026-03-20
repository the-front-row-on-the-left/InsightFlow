package com.insightflow.usage.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.insightflow.usage.domain.AiCompletedEvent;
import com.insightflow.usage.domain.AiRequestedEvent;
import com.insightflow.usage.domain.TrackedUsageEvent;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:usage-repositories;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UsageRepositoryIntegrationTest {

    @Autowired
    private UsageEventSnapshotRepository usageEventSnapshotRepository;

    @Autowired
    private UsageRecordRepository usageRecordRepository;

    @Test
    void savesAndUpdatesRequestedAndCompletedSnapshotsByRequestId() {
        usageEventSnapshotRepository.saveRequestedEvent(new AiRequestedEvent(
                "evt_requested_001",
                null,
                "req_snapshot_001",
                "u_demo_001",
                "t_demo",
                "svc_doc_summary",
                "wf_weekly_digest",
                "gpt-4.1-mini",
                OffsetDateTime.parse("2026-03-20T09:00:00Z")
        ));

        usageEventSnapshotRepository.saveCompletedEvent(new AiCompletedEvent(
                "evt_completed_001",
                null,
                "req_snapshot_001",
                "gpt-4.1-mini",
                "SUCCESS",
                320,
                180,
                500,
                245,
                true,
                OffsetDateTime.parse("2026-03-20T09:00:04Z")
        ));

        assertThat(usageEventSnapshotRepository.findByRequestId("req_snapshot_001"))
                .get()
                .satisfies(snapshot -> {
                    assertThat(snapshot.requestedEventId()).isEqualTo("evt_requested_001");
                    assertThat(snapshot.completedEventId()).isEqualTo("evt_completed_001");
                    assertThat(snapshot.userId()).isEqualTo("u_demo_001");
                    assertThat(snapshot.teamId()).isEqualTo("t_demo");
                    assertThat(snapshot.serviceId()).isEqualTo("svc_doc_summary");
                    assertThat(snapshot.workflowId()).isEqualTo("wf_weekly_digest");
                    assertThat(snapshot.requestedModel()).isEqualTo("gpt-4.1-mini");
                    assertThat(snapshot.completedModel()).isEqualTo("gpt-4.1-mini");
                    assertThat(snapshot.status()).isEqualTo("SUCCESS");
                    assertThat(snapshot.promptTokens()).isEqualTo(320);
                    assertThat(snapshot.completionTokens()).isEqualTo(180);
                    assertThat(snapshot.totalTokens()).isEqualTo(500);
                    assertThat(snapshot.latencyMs()).isEqualTo(245);
                    assertThat(snapshot.billable()).isTrue();
                });
    }

    @Test
    void savesAndUpdatesTrackedUsageThroughRepositoryContract() {
        usageRecordRepository.save(new TrackedUsageEvent(
                "evt_usage_tracked_001",
                null,
                "req_usage_001",
                "u_demo_001",
                "t_demo",
                "svc_doc_summary",
                "wf_weekly_digest",
                "gpt-4.1-mini",
                "SUCCESS",
                220,
                80,
                300,
                190,
                true,
                OffsetDateTime.parse("2026-03-20T10:00:00Z"),
                OffsetDateTime.parse("2026-03-20T10:00:01Z")
        ));

        usageRecordRepository.save(new TrackedUsageEvent(
                "evt_usage_tracked_002",
                null,
                "req_usage_001",
                "u_demo_001",
                "t_demo",
                "svc_doc_summary",
                "wf_weekly_digest",
                "gpt-4.1-mini",
                "FAILED",
                250,
                70,
                320,
                240,
                false,
                OffsetDateTime.parse("2026-03-20T10:00:02Z"),
                OffsetDateTime.parse("2026-03-20T10:00:03Z")
        ));

        assertThat(usageRecordRepository.findAll())
                .filteredOn(record -> record.requestId().equals("req_usage_001"))
                .singleElement()
                .satisfies(record -> {
                    assertThat(record.status()).isEqualTo("FAILED");
                    assertThat(record.promptTokens()).isEqualTo(250);
                    assertThat(record.completionTokens()).isEqualTo(70);
                    assertThat(record.totalTokens()).isEqualTo(320);
                    assertThat(record.latencyMs()).isEqualTo(240);
                    assertThat(record.policyResult()).isEqualTo("ALLOWED");
                    assertThat(record.limitResult()).isEqualTo("PASSED");
                });
    }
}
