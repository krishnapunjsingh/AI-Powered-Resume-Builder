package com.resumeai.ai.exception;

public class AiQuotaExceededException extends RuntimeException {

    public AiQuotaExceededException(Long userId) {
        super("Monthly AI quota exceeded for user: " + userId);
    }
}
