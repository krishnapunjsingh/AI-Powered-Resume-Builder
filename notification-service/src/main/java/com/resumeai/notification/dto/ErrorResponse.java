package com.resumeai.notification.dto;

public class ErrorResponse {
    private boolean success;
    private String message;
    private String errorCode;
    private int statusCode;

    public ErrorResponse(String message, String errorCode, int statusCode) {
        this.success = false;
        this.message = message;
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }

    public static ErrorResponse notFound(String message) {
        return new ErrorResponse(message, "NOT_FOUND", 404);
    }

    public static ErrorResponse badRequest(String message) {
        return new ErrorResponse(message, "BAD_REQUEST", 400);
    }

    public static ErrorResponse internalError(String message) {
        return new ErrorResponse(message, "INTERNAL_ERROR", 500);
    }

    public static ErrorResponse unauthorized(String message) {
        return new ErrorResponse(message, "UNAUTHORIZED", 401);
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

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
