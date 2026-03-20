package com.insightflow.notification.repository;

import com.insightflow.notification.domain.NotificationChannel;
import com.insightflow.notification.domain.NotificationPreferenceStatus;
import com.insightflow.notification.domain.NotificationPreferences;
import com.insightflow.notification.domain.NotificationSubscriptionPreference;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcNotificationPreferencesRepository implements NotificationPreferencesRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcNotificationPreferencesRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public NotificationPreferences findByContext(String userId, String teamId) {
        List<NotificationSubscriptionPreference> subscriptions = jdbcTemplate.query(
                """
                select event_type, channel, status
                from notification_preferences
                where user_id = ? and team_id = ?
                order by
                    case event_type
                        when 'limit.exceeded' then 1
                        when 'cost.calculated' then 2
                        else 99
                    end,
                    case channel
                        when 'team_digest' then 1
                        when 'user_inbox' then 2
                        else 99
                    end
                """,
                this::mapPreference,
                userId,
                teamId
        );

        return new NotificationPreferences(userId, teamId, subscriptions);
    }

    private NotificationSubscriptionPreference mapPreference(ResultSet rs, int rowNum) throws SQLException {
        return new NotificationSubscriptionPreference(
                rs.getString("event_type"),
                toChannel(rs.getString("channel")),
                toStatus(rs.getString("status"))
        );
    }

    private NotificationChannel toChannel(String apiValue) {
        return switch (apiValue) {
            case "team_digest" -> NotificationChannel.TEAM_DIGEST;
            case "user_inbox" -> NotificationChannel.USER_INBOX;
            default -> throw new IllegalArgumentException("Unsupported notification channel: " + apiValue);
        };
    }

    private NotificationPreferenceStatus toStatus(String apiValue) {
        return switch (apiValue) {
            case "active" -> NotificationPreferenceStatus.ACTIVE;
            case "muted" -> NotificationPreferenceStatus.MUTED;
            default -> throw new IllegalArgumentException("Unsupported notification status: " + apiValue);
        };
    }
}
