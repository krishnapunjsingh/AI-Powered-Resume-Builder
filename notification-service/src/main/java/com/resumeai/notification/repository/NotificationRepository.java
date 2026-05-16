package com.resumeai.notification.repository;

import com.resumeai.notification.entity.Notification;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByNotificationId(Long notificationId);

    List<Notification> findByRecipientId(Long recipientId);

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    List<Notification> findByRecipientIdAndIsReadFalse(Long recipientId);

    List<Notification> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(Long recipientId);

    List<Notification> findByRecipientIdAndType(Long recipientId, String type);

    List<Notification> findByRecipientIdAndStatus(Long recipientId, String status);

    List<Notification> findByRelatedIdAndRelatedType(Long relatedId, String relatedType);

    List<Notification> findByType(String type);

    List<Notification> findByStatus(String status);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientId = :recipientId AND n.isRead = false")
    long countByRecipientIdAndIsReadFalse(@Param("recipientId") Long recipientId);

    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.createdAt >= :startDate ORDER BY n.createdAt DESC")
    List<Notification> findNotificationsAfter(@Param("recipientId") Long recipientId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT n FROM Notification n WHERE n.status = 'FAILED' ORDER BY n.createdAt ASC")
    List<Notification> findFailedNotifications();

    void deleteByNotificationId(Long notificationId);

    long deleteByRecipientIdAndCreatedAtBefore(Long recipientId, LocalDateTime dateTime);

    long deleteByStatusAndUpdatedAtBefore(String status, LocalDateTime dateTime);
}
