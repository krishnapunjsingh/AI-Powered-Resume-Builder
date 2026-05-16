package com.resumeai.auth.exception;

public class InactiveUserException extends AuthException {

    public InactiveUserException() {
        super("Account is deactivated");
    }
}
