package com.resumeai.notification.exception;

public class NotificationNotFoundException extends NotificationException {

    public NotificationNotFoundException(String message) {
        super(message, "NOTIFICATION_NOT_FOUND", 404);
    }

    public NotificationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
