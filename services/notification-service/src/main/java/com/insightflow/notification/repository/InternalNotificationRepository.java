package com.insightflow.notification.repository;

import com.insightflow.notification.domain.InternalNotification;
import java.util.List;

public interface InternalNotificationRepository {

    boolean saveIfAbsent(InternalNotification notification);

    List<InternalNotification> findByContext(String userId, String teamId);

    void clear();
}
