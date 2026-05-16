package com.resumeai.notification.exception;

public class InvalidNotificationException extends NotificationException {

    public InvalidNotificationException(String message) {
        super(message, "INVALID_NOTIFICATION", 400);
    }
}
