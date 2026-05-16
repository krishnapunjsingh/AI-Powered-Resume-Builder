package com.resumeai.auth.exception;

public class InvalidTokenException extends AuthException {

    public InvalidTokenException() {
        super("Invalid or expired token");
    }
}
