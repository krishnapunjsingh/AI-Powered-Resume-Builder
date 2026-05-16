package com.resumeai.auth.exception;

public class UserNotFoundException extends AuthException {

    public UserNotFoundException() {
        super("User not found");
    }
}
