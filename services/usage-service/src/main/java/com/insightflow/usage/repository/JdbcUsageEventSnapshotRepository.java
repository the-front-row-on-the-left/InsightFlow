package com.insightflow.usage.repository;

import com.insightflow.usage.domain.AiCompletedEvent;
import com.insightflow.usage.domain.AiRequestedEvent;
import com.insightflow.usage.domain.UsageEventSnapshot;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcUsageEventSnapshotRepository implements UsageEventSnapshotRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcUsageEventSnapshotRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<UsageEventSnapshot> findByRequestId(String requestId) {
        return jdbcTemplate.query(
                """
                select request_id, requested_event_id, completed_event_id, user_id, team_id, service_id, workflow_id,
                       requested_model, completed_model, status, prompt_tokens, completion_tokens, total_tokens,
                       latency_ms, billable, requested_at, completed_at
                from usage_event_snapshots
                where request_id = ?
                """,
                this::mapSnapshot,
                requestId
        ).stream().findFirst();
    }

    @Override
    public void saveRequestedEvent(AiRequestedEvent event) {
        if (findByRequestId(event.requestId()).isPresent()) {
            jdbcTemplate.update(
                    """
                    update usage_event_snapshots
                    set requested_event_id = ?, user_id = ?, team_id = ?, service_id = ?, workflow_id = ?,
                        requested_model = ?, requested_at = ?, updated_at = current_timestamp
                    where request_id = ?
                    """,
                    event.eventId(),
                    event.userId(),
                    event.teamId(),
                    event.serviceId(),
                    event.workflowId(),
                    event.model(),
                    toTimestamp(event.requestedAt()),
                    event.requestId()
            );
            return;
        }

        jdbcTemplate.update(
                """
                insert into usage_event_snapshots (
                    request_id, requested_event_id, user_id, team_id, service_id, workflow_id, requested_model,
                    requested_at, created_at, updated_at
                ) values (?, ?, ?, ?, ?, ?, ?, ?, current_timestamp, current_timestamp)
                """,
                event.requestId(),
                event.eventId(),
                event.userId(),
                event.teamId(),
                event.serviceId(),
                event.workflowId(),
                event.model(),
                toTimestamp(event.requestedAt())
        );
    }

    @Override
    public void saveCompletedEvent(AiCompletedEvent event) {
        if (findByRequestId(event.requestId()).isPresent()) {
            jdbcTemplate.update(
                    """
                    update usage_event_snapshots
                    set completed_event_id = ?, completed_model = ?, status = ?, prompt_tokens = ?,
                        completion_tokens = ?, total_tokens = ?, latency_ms = ?, billable = ?, completed_at = ?,
                        updated_at = current_timestamp
                    where request_id = ?
                    """,
                    event.eventId(),
                    event.model(),
                    event.status(),
                    event.promptTokens(),
                    event.completionTokens(),
                    event.totalTokens(),
                    event.latencyMs(),
                    event.billable(),
                    toTimestamp(event.completedAt()),
                    event.requestId()
            );
            return;
        }

        jdbcTemplate.update(
                """
                insert into usage_event_snapshots (
                    request_id, completed_event_id, completed_model, status, prompt_tokens, completion_tokens,
                    total_tokens, latency_ms, billable, completed_at, created_at, updated_at
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, current_timestamp, current_timestamp)
                """,
                event.requestId(),
                event.eventId(),
                event.model(),
                event.status(),
                event.promptTokens(),
                event.completionTokens(),
                event.totalTokens(),
                event.latencyMs(),
                event.billable(),
                toTimestamp(event.completedAt())
        );
    }

    @Override
    public long count() {
        Long count = jdbcTemplate.queryForObject("select count(*) from usage_event_snapshots", Long.class);
        return count == null ? 0 : count;
    }

    private UsageEventSnapshot mapSnapshot(ResultSet resultSet, int rowNum) throws SQLException {
        return new UsageEventSnapshot(
                resultSet.getString("request_id"),
                resultSet.getString("requested_event_id"),
                resultSet.getString("completed_event_id"),
                resultSet.getString("user_id"),
                resultSet.getString("team_id"),
                resultSet.getString("service_id"),
                resultSet.getString("workflow_id"),
                resultSet.getString("requested_model"),
                resultSet.getString("completed_model"),
                resultSet.getString("status"),
                getNullableInteger(resultSet, "prompt_tokens"),
                getNullableInteger(resultSet, "completion_tokens"),
                getNullableInteger(resultSet, "total_tokens"),
                getNullableInteger(resultSet, "latency_ms"),
                getNullableBoolean(resultSet, "billable"),
                toOffsetDateTime(resultSet.getTimestamp("requested_at")),
                toOffsetDateTime(resultSet.getTimestamp("completed_at"))
        );
    }

    private Integer getNullableInteger(ResultSet resultSet, String columnName) throws SQLException {
        int value = resultSet.getInt(columnName);
        return resultSet.wasNull() ? null : value;
    }

    private Boolean getNullableBoolean(ResultSet resultSet, String columnName) throws SQLException {
        boolean value = resultSet.getBoolean(columnName);
        return resultSet.wasNull() ? null : value;
    }

    private Timestamp toTimestamp(OffsetDateTime value) {
        return value == null ? null : Timestamp.from(value.toInstant());
    }

    private OffsetDateTime toOffsetDateTime(Timestamp value) {
        return value == null ? null : value.toInstant().atOffset(ZoneOffset.UTC);
    }
}
