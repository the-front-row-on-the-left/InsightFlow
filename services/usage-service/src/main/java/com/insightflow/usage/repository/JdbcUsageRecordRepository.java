package com.insightflow.usage.repository;

import com.insightflow.usage.domain.TrackedUsageEvent;
import com.insightflow.usage.domain.UsageRecord;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcUsageRecordRepository implements UsageRecordRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcUsageRecordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<UsageRecord> findAll() {
        return jdbcTemplate.query(
                """
                select request_id, user_id, team_id, service_id, workflow_id, model, status, policy_result,
                       limit_result, prompt_tokens, completion_tokens, total_tokens, latency_ms, occurred_at
                from usage_records
                """,
                this::mapUsageRecord
        );
    }

    public void save(TrackedUsageEvent trackedUsageEvent) {
        save(trackedUsageEvent, trackedUsageEvent.status().equals("BLOCKED") ? "DENIED" : "ALLOWED",
                trackedUsageEvent.status().equals("BLOCKED") ? "NOT_APPLIED" : "PASSED");
    }

    public void save(TrackedUsageEvent trackedUsageEvent, String policyResult, String limitResult) {
        if (existsByRequestId(trackedUsageEvent.requestId())) {
            jdbcTemplate.update(
                    """
                    update usage_records
                    set tracking_event_id = ?, user_id = ?, team_id = ?, service_id = ?, workflow_id = ?, model = ?,
                        status = ?, policy_result = ?, limit_result = ?, prompt_tokens = ?, completion_tokens = ?,
                        total_tokens = ?, latency_ms = ?, billable = ?, occurred_at = ?, tracked_at = ?
                    where request_id = ?
                    """,
                    trackedUsageEvent.eventId(),
                    trackedUsageEvent.userId(),
                    trackedUsageEvent.teamId(),
                    trackedUsageEvent.serviceId(),
                    trackedUsageEvent.workflowId(),
                    trackedUsageEvent.model(),
                    trackedUsageEvent.status(),
                    policyResult,
                    limitResult,
                    trackedUsageEvent.promptTokens(),
                    trackedUsageEvent.completionTokens(),
                    trackedUsageEvent.totalTokens(),
                    trackedUsageEvent.latencyMs(),
                    trackedUsageEvent.billable(),
                    toTimestamp(trackedUsageEvent.occurredAt()),
                    toTimestamp(trackedUsageEvent.trackedAt()),
                    trackedUsageEvent.requestId()
            );
            return;
        }

        jdbcTemplate.update(
                """
                insert into usage_records (
                    request_id, tracking_event_id, user_id, team_id, service_id, workflow_id, model, status,
                    policy_result, limit_result, prompt_tokens, completion_tokens, total_tokens, latency_ms,
                    billable, occurred_at, tracked_at, created_at
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, current_timestamp)
                """,
                trackedUsageEvent.requestId(),
                trackedUsageEvent.eventId(),
                trackedUsageEvent.userId(),
                trackedUsageEvent.teamId(),
                trackedUsageEvent.serviceId(),
                trackedUsageEvent.workflowId(),
                trackedUsageEvent.model(),
                trackedUsageEvent.status(),
                policyResult,
                limitResult,
                trackedUsageEvent.promptTokens(),
                trackedUsageEvent.completionTokens(),
                trackedUsageEvent.totalTokens(),
                trackedUsageEvent.latencyMs(),
                trackedUsageEvent.billable(),
                toTimestamp(trackedUsageEvent.occurredAt()),
                toTimestamp(trackedUsageEvent.trackedAt())
        );
    }

    public void saveSeedRecord(UsageRecord record) {
        save(
                new TrackedUsageEvent(
                        "seed_usage_tracked_" + record.requestId(),
                        TrackedUsageEvent.EVENT_TYPE,
                        record.requestId(),
                        record.userId(),
                        record.teamId(),
                        record.serviceId(),
                        record.workflowId(),
                        record.model(),
                        record.status(),
                        record.promptTokens(),
                        record.completionTokens(),
                        record.totalTokens(),
                        record.latencyMs(),
                        !"BLOCKED".equals(record.status()),
                        record.requestedAt(),
                        record.requestedAt()
                ),
                record.policyResult(),
                record.limitResult()
        );
    }

    public long count() {
        Long count = jdbcTemplate.queryForObject("select count(*) from usage_records", Long.class);
        return count == null ? 0 : count;
    }

    public boolean existsByRequestId(String requestId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from usage_records where request_id = ?",
                Integer.class,
                requestId
        );
        return count != null && count > 0;
    }

    private UsageRecord mapUsageRecord(ResultSet resultSet, int rowNum) throws SQLException {
        return new UsageRecord(
                resultSet.getString("request_id"),
                resultSet.getString("user_id"),
                resultSet.getString("team_id"),
                resultSet.getString("service_id"),
                resultSet.getString("workflow_id"),
                resultSet.getString("model"),
                resultSet.getString("status"),
                resultSet.getString("policy_result"),
                resultSet.getString("limit_result"),
                resultSet.getInt("prompt_tokens"),
                resultSet.getInt("completion_tokens"),
                resultSet.getInt("total_tokens"),
                resultSet.getInt("latency_ms"),
                toOffsetDateTime(resultSet.getTimestamp("occurred_at"))
        );
    }

    private Timestamp toTimestamp(OffsetDateTime value) {
        return value == null ? null : Timestamp.from(value.toInstant());
    }

    private OffsetDateTime toOffsetDateTime(Timestamp value) {
        return value == null ? null : value.toInstant().atOffset(ZoneOffset.UTC);
    }
}
