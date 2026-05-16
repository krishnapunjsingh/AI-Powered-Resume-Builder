package com.resumeai.notification.exception;

public class NotificationException extends RuntimeException {

    private String errorCode;
    private int statusCode;

    public NotificationException(String message) {
        super(message);
        this.errorCode = "NOTIFICATION_ERROR";
        this.statusCode = 500;
    }

    public NotificationException(String message, String errorCode, int statusCode) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }

    public NotificationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "NOTIFICATION_ERROR";
        this.statusCode = 500;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
