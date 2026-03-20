package com.insightflow.notification.service;

import com.insightflow.notification.dto.NotificationSubscription;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class NotificationQueryService {

    public List<NotificationSubscription> getSubscriptions() {
        return List.of(
                new NotificationSubscription("optimization.recommended", "team_digest", "active")
        );
    }
}
