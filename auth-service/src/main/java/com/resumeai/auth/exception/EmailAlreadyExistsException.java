package com.resumeai.auth.exception;

public class EmailAlreadyExistsException extends AuthException {

    public EmailAlreadyExistsException() {
        super("Email is already registered");
    }
}