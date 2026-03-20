package com.insightflow.notification.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightflow.notification.domain.InternalNotification;
import com.insightflow.notification.domain.NotificationChannel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcInternalNotificationRepository implements InternalNotificationRepository {

    private static final TypeReference<Map<String, String>> MAP_TYPE = new TypeReference<>() {
    };

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcInternalNotificationRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean saveIfAbsent(InternalNotification notification) {
        if (exists(notification.notificationId())) {
            return false;
        }

        jdbcTemplate.update(
                """
                insert into internal_notifications (
                    notification_id, request_id, event_type, channel, recipient_type, recipient_id,
                    title, message, status, occurred_at, metadata_json, created_at
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, current_timestamp)
                """,
                notification.notificationId(),
                notification.requestId(),
                notification.eventType(),
                notification.channel().apiValue(),
                notification.recipientType(),
                notification.recipientId(),
                notification.title(),
                notification.message(),
                notification.status(),
                toTimestamp(notification.occurredAt()),
                writeMetadata(notification.metadata())
        );
        return true;
    }

    @Override
    public List<InternalNotification> findByContext(String userId, String teamId) {
        return jdbcTemplate.query(
                """
                select notification_id, request_id, event_type, channel, recipient_type, recipient_id,
                       title, message, status, occurred_at, metadata_json
                from internal_notifications
                where (recipient_type = 'user' and recipient_id = ?)
                   or (recipient_type = 'team' and recipient_id = ?)
                order by occurred_at desc, notification_id desc
                """,
                this::mapNotification,
                userId,
                teamId
        ).stream().sorted(
                Comparator.comparing(InternalNotification::occurredAt)
                        .reversed()
                        .thenComparing(InternalNotification::notificationId)
        ).toList();
    }

    @Override
    public void clear() {
        jdbcTemplate.update("delete from internal_notifications");
    }

    private boolean exists(String notificationId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from internal_notifications where notification_id = ?",
                Integer.class,
                notificationId
        );
        return count != null && count > 0;
    }

    private InternalNotification mapNotification(ResultSet rs, int rowNum) throws SQLException {
        return new InternalNotification(
                rs.getString("notification_id"),
                rs.getString("request_id"),
                rs.getString("event_type"),
                toChannel(rs.getString("channel")),
                rs.getString("recipient_type"),
                rs.getString("recipient_id"),
                rs.getString("title"),
                rs.getString("message"),
                rs.getString("status"),
                toInstant(rs.getTimestamp("occurred_at")),
                readMetadata(rs.getString("metadata_json"))
        );
    }

    private NotificationChannel toChannel(String apiValue) {
        return switch (apiValue) {
            case "team_digest" -> NotificationChannel.TEAM_DIGEST;
            case "user_inbox" -> NotificationChannel.USER_INBOX;
            default -> throw new IllegalArgumentException("Unsupported notification channel: " + apiValue);
        };
    }

    private Map<String, String> readMetadata(String metadataJson) {
        if (metadataJson == null || metadataJson.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(metadataJson, MAP_TYPE);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to read notification metadata.", exception);
        }
    }

    private String writeMetadata(Map<String, String> metadata) {
        try {
            return objectMapper.writeValueAsString(metadata == null ? Map.of() : metadata);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize notification metadata.", exception);
        }
    }

    private Timestamp toTimestamp(Instant value) {
        return value == null ? null : Timestamp.from(value);
    }

    private Instant toInstant(Timestamp value) {
        return value == null ? null : value.toInstant();
    }
}
