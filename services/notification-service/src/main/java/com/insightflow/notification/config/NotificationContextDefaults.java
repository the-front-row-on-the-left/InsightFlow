package com.insightflow.notification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NotificationContextDefaults {

    private final String defaultUserId;
    private final String defaultTeamId;

    public NotificationContextDefaults(
            @Value("${insightflow.defaults.user-id:u_demo_001}") String defaultUserId,
            @Value("${insightflow.defaults.team-id:t_demo}") String defaultTeamId
    ) {
        this.defaultUserId = defaultUserId;
        this.defaultTeamId = defaultTeamId;
    }

    public String defaultUserId() {
        return defaultUserId;
    }

    public String defaultTeamId() {
        return defaultTeamId;
    }
}
