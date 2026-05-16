package com.resumeai.notification.dto;

import com.resumeai.notification.entity.Notification;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationResponse {
    private boolean success;
    private String message;
    private Object data;
    private int statusCode;

    public NotificationResponse(boolean success, String message, Object data, int statusCode) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.statusCode = statusCode;
    }

    public static NotificationResponse fromEntity(Notification notification, String message) {
        NotificationData data = new NotificationData(notification);
        return new NotificationResponse(true, message, data, 200);
    }

    public static NotificationResponse fromEntityList(List<Notification> notifications, String message) {
        List<NotificationData> data = notifications.stream()
                .map(NotificationData::new)
                .collect(Collectors.toList());
        return new NotificationResponse(true, message, data, 200);
    }

    public static NotificationResponse success(String message) {
        return new NotificationResponse(true, message, null, 200);
    }

    public static NotificationResponse bulkSuccess(String message, long count) {
        return new NotificationResponse(true, message, "Processed: " + count, 200);
    }

    public static NotificationResponse unreadCount(long count) {
        return new NotificationResponse(true, "Unread count retrieved", count, 200);
    }

    public static NotificationResponse error(String message, int statusCode) {
        return new NotificationResponse(false, message, null, statusCode);
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    // Inner class for notification data
    public static class NotificationData {
        private Long notificationId;
        private Long recipientId;
        private String type;
        private String title;
        private String message;
        private String channel;
        private Long relatedId;
        private String relatedType;
        private String status;
        private boolean isRead;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime readAt;

        public NotificationData(Notification notification) {
            this.notificationId = notification.getNotificationId();
            this.recipientId = notification.getRecipientId();
            this.type = notification.getType();
            this.title = notification.getTitle();
            this.message = notification.getMessage();
            this.channel = notification.getChannel();
            this.relatedId = notification.getRelatedId();
            this.relatedType = notification.getRelatedType();
            this.status = notification.getStatus();
            this.isRead = notification.isRead();
            this.createdAt = notification.getCreatedAt();
            this.updatedAt = notification.getUpdatedAt();
            this.readAt = notification.getReadAt();
        }

        // Getters
        public Long getNotificationId() {
            return notificationId;
        }

        public Long getRecipientId() {
            return recipientId;
        }

        public String getType() {
            return type;
        }

        public String getTitle() {
            return title;
        }

        public String getMessage() {
            return message;
        }

        public String getChannel() {
            return channel;
        }

        public Long getRelatedId() {
            return relatedId;
        }

        public String getRelatedType() {
            return relatedType;
        }

        public String getStatus() {
            return status;
        }

        @JsonProperty("isRead")
        public boolean isRead() {
            return isRead;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        public LocalDateTime getReadAt() {
            return readAt;
        }
    }
}
