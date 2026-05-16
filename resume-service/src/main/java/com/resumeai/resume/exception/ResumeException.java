package com.resumeai.resume.exception;

public abstract class ResumeException extends RuntimeException {

    protected ResumeException(String message) {
        super(message);
    }
}