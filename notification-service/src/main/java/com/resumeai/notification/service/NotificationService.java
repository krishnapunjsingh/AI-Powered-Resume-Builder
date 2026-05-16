package com.resumeai.notification.service;

import com.resumeai.notification.dto.NotificationRequest;
import com.resumeai.notification.dto.NotificationResponse;
import java.util.List;

public interface NotificationService {

    NotificationResponse send(NotificationRequest request);

    NotificationResponse sendBulk(List<NotificationRequest> requests);

    NotificationResponse markAsRead(Long notificationId);

    NotificationResponse markAllAsRead(Long recipientId);

    NotificationResponse getByRecipientId(Long recipientId);

    NotificationResponse getUnreadCount(Long recipientId);

    NotificationResponse getByNotificationId(Long notificationId);

    NotificationResponse deleteNotification(Long notificationId);

    NotificationResponse deleteAllForRecipient(Long recipientId);

    NotificationResponse getAll();
}
