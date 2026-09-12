package com.solydshop.notifications.service;

import com.solydshop.notifications.dto.NotificationDTO;

import java.util.List;

public interface NotificationService {

    List<NotificationDTO> getNotifications(Long userId);

    long getUnreadCount(Long userId);

    void markRead(Long notificationId, Long userId);

    void markAllRead(Long userId);

    void deleteOne(Long notificationId, Long userId);

    void deleteAll(Long userId);

    void createForUser(Long userId, String title, String message, String type, Long resourceId);
}
