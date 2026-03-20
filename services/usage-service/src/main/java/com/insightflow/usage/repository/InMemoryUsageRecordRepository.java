package com.insightflow.usage.repository;

import com.insightflow.usage.domain.TrackedUsageEvent;
import com.insightflow.usage.domain.UsageRecord;
import java.util.ArrayList;
import java.time.OffsetDateTime;
import java.util.List;

public class InMemoryUsageRecordRepository implements UsageRecordRepository {

    private static final List<UsageRecord> SEEDED_RECORDS = List.of(
            new UsageRecord(
                    "req_u_001",
                    "u_demo_001",
                    "t_demo",
                    "svc_doc_summary",
                    "wf_weekly_digest",
                    "gpt-4o-mini",
                    "SUCCEEDED",
                    "ALLOWED",
                    "PASSED",
                    900,
                    320,
                    1220,
                    410,
                    OffsetDateTime.parse("2026-03-14T09:12:00Z")
            ),
            new UsageRecord(
                    "req_s_006",
                    "u_growth_001",
                    "t_growth",
                    "svc_doc_summary",
                    "wf_growth_brief",
                    "gpt-4o-mini",
                    "SUCCEEDED",
                    "ALLOWED",
                    "PASSED",
                    1180,
                    380,
                    1560,
                    318,
                    OffsetDateTime.parse("2026-03-17T16:45:00Z")
            ),
            new UsageRecord(
                    "req_u_002",
                    "u_demo_001",
                    "t_demo",
                    "svc_doc_summary",
                    "wf_weekly_digest",
                    "gpt-4o-mini",
                    "SUCCEEDED",
                    "ALLOWED",
                    "PASSED",
                    1320,
                    520,
                    1840,
                    365,
                    OffsetDateTime.parse("2026-03-18T13:20:00Z")
            ),
            new UsageRecord(
                    "req_t_004",
                    "u_team_002",
                    "t_demo",
                    "svc_doc_summary",
                    "wf_team_brief",
                    "gpt-4o-mini",
                    "SUCCEEDED",
                    "ALLOWED",
                    "PASSED",
                    1100,
                    520,
                    1620,
                    450,
                    OffsetDateTime.parse("2026-03-19T08:05:00Z")
            ),
            new UsageRecord(
                    "req_t_005",
                    "u_team_003",
                    "t_demo",
                    "svc_doc_summary",
                    "wf_team_brief",
                    "gpt-4o-mini",
                    "BLOCKED",
                    "DENIED",
                    "NOT_APPLIED",
                    0,
                    0,
                    0,
                    12,
                    OffsetDateTime.parse("2026-03-19T18:42:00Z")
            ),
            new UsageRecord(
                    "req_w_008",
                    "u_growth_002",
                    "t_growth",
                    "svc_research_assistant",
                    "wf_research_sync",
                    "gpt-4o-mini",
                    "SUCCEEDED",
                    "ALLOWED",
                    "PASSED",
                    1540,
                    660,
                    2200,
                    530,
                    OffsetDateTime.parse("2026-03-16T10:30:00Z")
            ),
            new UsageRecord(
                    "req_u_003",
                    "u_demo_001",
                    "t_demo",
                    "svc_report_generator",
                    "wf_monthly_report",
                    "gpt-4.1-mini",
                    "FAILED",
                    "ALLOWED",
                    "PASSED",
                    980,
                    420,
                    1400,
                    137,
                    OffsetDateTime.parse("2026-03-20T07:50:00Z")
            ),
            new UsageRecord(
                    "req_s_007",
                    "u_growth_003",
                    "t_growth",
                    "svc_doc_summary",
                    "wf_growth_brief",
                    "gpt-4.1-mini",
                    "FAILED",
                    "ALLOWED",
                    "PASSED",
                    760,
                    420,
                    1180,
                    280,
                    OffsetDateTime.parse("2026-03-20T12:05:00Z")
            )
    );

    private final List<UsageRecord> records = new ArrayList<>(SEEDED_RECORDS);

    @Override
    public List<UsageRecord> findAll() {
        return List.copyOf(records);
    }

    @Override
    public void save(TrackedUsageEvent trackedUsageEvent) {
        records.removeIf(record -> record.requestId().equals(trackedUsageEvent.requestId()));
        records.add(new UsageRecord(
                trackedUsageEvent.requestId(),
                trackedUsageEvent.userId(),
                trackedUsageEvent.teamId(),
                trackedUsageEvent.serviceId(),
                trackedUsageEvent.workflowId(),
                trackedUsageEvent.model(),
                trackedUsageEvent.status(),
                "BLOCKED".equals(trackedUsageEvent.status()) ? "DENIED" : "ALLOWED",
                "BLOCKED".equals(trackedUsageEvent.status()) ? "NOT_APPLIED" : "PASSED",
                trackedUsageEvent.promptTokens(),
                trackedUsageEvent.completionTokens(),
                trackedUsageEvent.totalTokens(),
                trackedUsageEvent.latencyMs(),
                trackedUsageEvent.occurredAt()
        ));
    }
}
