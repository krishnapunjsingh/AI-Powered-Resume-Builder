package com.resumeai.ai.exception;

public class AiRequestNotFoundException extends RuntimeException {

    public AiRequestNotFoundException(String requestId) {
        super("AI request not found for id: " + requestId);
    }
}
