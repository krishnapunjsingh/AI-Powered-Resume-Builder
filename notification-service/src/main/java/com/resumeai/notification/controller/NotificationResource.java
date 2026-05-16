package com.resumeai.notification.controller;

import com.resumeai.notification.dto.NotificationRequest;
import com.resumeai.notification.dto.NotificationResponse;
import com.resumeai.notification.service.NotificationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class NotificationResource {

    private final NotificationService notificationService;

    public NotificationResource(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Send a single notification
     * @param request Notification request details
     * @param authorizationHeader JWT token for authentication
     * @return NotificationResponse with the created notification
     */
    @PostMapping("/send")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<NotificationResponse> send(
            @Valid @RequestBody NotificationRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        NotificationResponse response = notificationService.send(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Send multiple notifications in bulk
     * @param requests List of notification requests
     * @param authorizationHeader JWT token for authentication
     * @return NotificationResponse with bulk operation status
     */
    @PostMapping("/send-bulk")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<NotificationResponse> sendBulk(
            @Valid @RequestBody List<NotificationRequest> requests,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        NotificationResponse response = notificationService.sendBulk(requests);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    /**
     * Mark a notification as read
     * @param notificationId ID of the notification to mark as read
     * @param authorizationHeader JWT token for authentication
     * @return NotificationResponse with updated notification
     */
    @PutMapping("/{notificationId}/mark-read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long notificationId,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        NotificationResponse response = notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(response);
    }

    /**
     * Mark all notifications as read for a recipient
     * @param recipientId ID of the recipient
     * @param authorizationHeader JWT token for authentication
     * @return NotificationResponse with operation status
     */
    @PutMapping("/recipient/{recipientId}/mark-all-read")
    public ResponseEntity<NotificationResponse> markAllAsRead(
            @PathVariable Long recipientId,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        NotificationResponse response = notificationService.markAllAsRead(recipientId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all notifications for a recipient
     * @param recipientId ID of the recipient
     * @param authorizationHeader JWT token for authentication
     * @return NotificationResponse with list of notifications
     */
    @GetMapping("/recipient/{recipientId}")
    public ResponseEntity<NotificationResponse> getByRecipientId(
            @PathVariable Long recipientId,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        NotificationResponse response = notificationService.getByRecipientId(recipientId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get unread count for a recipient
     * @param recipientId ID of the recipient
     * @param authorizationHeader JWT token for authentication
     * @return NotificationResponse with unread count
     */
    @GetMapping("/recipient/{recipientId}/unread-count")
    public ResponseEntity<NotificationResponse> getUnreadCount(
            @PathVariable Long recipientId,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        NotificationResponse response = notificationService.getUnreadCount(recipientId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific notification by ID
     * @param notificationId ID of the notification
     * @param authorizationHeader JWT token for authentication
     * @return NotificationResponse with notification details
     */
    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationResponse> getByNotificationId(
            @PathVariable Long notificationId,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        NotificationResponse response = notificationService.getByNotificationId(notificationId);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a specific notification
     * @param notificationId ID of the notification to delete
     * @param authorizationHeader JWT token for authentication
     * @return NotificationResponse with deletion status
     */
    @DeleteMapping("/{notificationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<NotificationResponse> deleteNotification(
            @PathVariable Long notificationId,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        NotificationResponse response = notificationService.deleteNotification(notificationId);
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }

    /**
     * Delete all notifications for a recipient
     * @param recipientId ID of the recipient
     * @param authorizationHeader JWT token for authentication
     * @return NotificationResponse with deletion status
     */
    @DeleteMapping("/recipient/{recipientId}/delete-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<NotificationResponse> deleteAllForRecipient(
            @PathVariable Long recipientId,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        NotificationResponse response = notificationService.deleteAllForRecipient(recipientId);
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }

    /**
     * Get all notifications (admin endpoint)
     * @param authorizationHeader JWT token for authentication
     * @return NotificationResponse with all notifications
     */
    @GetMapping("/admin/all")
    public ResponseEntity<NotificationResponse> getAll(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        NotificationResponse response = notificationService.getAll();
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     * @return Health status
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notification Service is running");
    }
}
