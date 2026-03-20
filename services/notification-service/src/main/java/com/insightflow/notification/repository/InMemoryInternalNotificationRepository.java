package com.insightflow.notification.repository;

import com.insightflow.notification.domain.InternalNotification;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryInternalNotificationRepository implements InternalNotificationRepository {

    private final Map<String, InternalNotification> notifications = new ConcurrentHashMap<>();

    @Override
    public boolean saveIfAbsent(InternalNotification notification) {
        return notifications.putIfAbsent(notification.notificationId(), notification) == null;
    }

    @Override
    public List<InternalNotification> findByContext(String userId, String teamId) {
        return notifications.values().stream()
                .filter(notification -> notification.matchesContext(userId, teamId))
                .sorted(
                        Comparator.comparing(InternalNotification::occurredAt)
                                .reversed()
                                .thenComparing(InternalNotification::notificationId)
                )
                .toList();
    }

    @Override
    public void clear() {
        notifications.clear();
    }
}
