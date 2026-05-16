package com.resumeai.notification.service.impl;

import com.resumeai.notification.dto.NotificationRequest;
import com.resumeai.notification.dto.NotificationResponse;
import com.resumeai.notification.entity.Notification;
import com.resumeai.notification.exception.NotificationNotFoundException;
import com.resumeai.notification.repository.NotificationRepository;
import com.resumeai.notification.service.EmailDeliveryService;
import com.resumeai.notification.service.NotificationService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailDeliveryService emailDeliveryService;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   EmailDeliveryService emailDeliveryService) {
        this.notificationRepository = notificationRepository;
        this.emailDeliveryService = emailDeliveryService;
    }

    @Override
    @Transactional
    public NotificationResponse send(NotificationRequest request) {
        validateRequest(request);

        Notification notification = new Notification(
                request.recipientId(),
                request.type(),
                request.title(),
                request.message(),
                request.channel(),
                request.relatedId(),
                request.relatedType()
        );

        notification.setStatus(deliverByChannel(request) ? "SENT" : "FAILED");
        Notification saved = notificationRepository.save(notification);

        return NotificationResponse.fromEntity(saved, "Notification sent successfully");
    }

    @Override
    @Transactional
    public NotificationResponse sendBulk(List<NotificationRequest> requests) {
        List<Notification> notifications = new ArrayList<>();

        for (NotificationRequest request : requests) {
            validateRequest(request);

            Notification notification = new Notification(
                    request.recipientId(),
                    request.type(),
                    request.title(),
                    request.message(),
                    request.channel(),
                    request.relatedId(),
                    request.relatedType()
            );
            notification.setStatus(deliverByChannel(request) ? "SENT" : "FAILED");
            notifications.add(notification);
        }

        notificationRepository.saveAll(notifications);
        return NotificationResponse.bulkSuccess("Bulk notifications sent successfully", notifications.size());
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findByNotificationId(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with ID: " + notificationId));

        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        notification.setStatus("READ");
        Notification updated = notificationRepository.save(notification);

        return NotificationResponse.fromEntity(updated, "Notification marked as read");
    }

    @Override
    @Transactional
    public NotificationResponse markAllAsRead(Long recipientId) {
        List<Notification> unreadNotifications = notificationRepository.findByRecipientIdAndIsReadFalse(recipientId);

        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification.setStatus("READ");
        }

        notificationRepository.saveAll(unreadNotifications);
        return NotificationResponse.bulkSuccess("All notifications marked as read", unreadNotifications.size());
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getByRecipientId(Long recipientId) {
        List<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId);
        return NotificationResponse.fromEntityList(notifications, "Notifications retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getUnreadCount(Long recipientId) {
        long unreadCount = notificationRepository.countByRecipientIdAndIsReadFalse(recipientId);
        return NotificationResponse.unreadCount(unreadCount);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getByNotificationId(Long notificationId) {
        Notification notification = notificationRepository.findByNotificationId(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with ID: " + notificationId));

        return NotificationResponse.fromEntity(notification, "Notification retrieved successfully");
    }

    @Override
    @Transactional
    public NotificationResponse deleteNotification(Long notificationId) {
        Notification notification = notificationRepository.findByNotificationId(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with ID: " + notificationId));

        notificationRepository.deleteByNotificationId(notificationId);
        return NotificationResponse.success("Notification deleted successfully");
    }

    @Override
    @Transactional
    public NotificationResponse deleteAllForRecipient(Long recipientId) {
        List<Notification> notifications = notificationRepository.findByRecipientId(recipientId);
        long count = notifications.size();

        for (Notification notification : notifications) {
            notificationRepository.deleteByNotificationId(notification.getNotificationId());
        }

        return NotificationResponse.bulkSuccess("All notifications deleted for recipient", count);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getAll() {
        List<Notification> notifications = notificationRepository.findAll();
        return NotificationResponse.fromEntityList(notifications, "All notifications retrieved successfully");
    }

    @Scheduled(fixedRate = 86400000) // 24 hours
    @Transactional
    public void cleanupOldNotifications() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        notificationRepository.deleteByStatusAndUpdatedAtBefore("READ", thirtyDaysAgo);
    }

    @Scheduled(fixedRate = 3600000) // 1 hour
    @Transactional
    public void retryFailedNotifications() {
        List<Notification> failedNotifications = notificationRepository.findFailedNotifications();

        for (Notification notification : failedNotifications) {
            if (!isEmailChannel(notification.getChannel())) {
                notification.setStatus("SENT");
            }
            notificationRepository.save(notification);
        }
    }

    private void validateRequest(NotificationRequest request) {
        if (request.recipientId() == null || request.recipientId() <= 0) {
            throw new IllegalArgumentException("Invalid recipient ID");
        }
        if (request.type() == null || request.type().isEmpty()) {
            throw new IllegalArgumentException("Notification type is required");
        }
        if (request.title() == null || request.title().isEmpty()) {
            throw new IllegalArgumentException("Notification title is required");
        }
        if (request.message() == null || request.message().isEmpty()) {
            throw new IllegalArgumentException("Notification message is required");
        }
        if (isEmailChannel(request.channel()) && (request.recipientEmail() == null || request.recipientEmail().isBlank())) {
            throw new IllegalArgumentException("Recipient email is required for email notifications");
        }
    }

    private boolean deliverByChannel(NotificationRequest request) {
        if (!isEmailChannel(request.channel())) {
            return true;
        }
        return emailDeliveryService.send(request.recipientEmail(), request.title(), request.message());
    }

    private boolean isEmailChannel(String channel) {
        return channel != null && "EMAIL".equalsIgnoreCase(channel);
    }
}
