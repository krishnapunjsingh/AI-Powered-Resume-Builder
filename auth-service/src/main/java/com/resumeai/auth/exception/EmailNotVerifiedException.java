package com.resumeai.auth.exception;

public class EmailNotVerifiedException extends AuthException {

    public EmailNotVerifiedException() {
        super("Please verify your email before logging in");
    }
}
