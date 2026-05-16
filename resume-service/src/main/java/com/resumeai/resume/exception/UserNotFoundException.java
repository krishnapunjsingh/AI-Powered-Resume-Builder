package com.resumeai.resume.exception;

public class UserNotFoundException extends ResumeException {

    public UserNotFoundException(Long userId) {
        super("User not found for id: " + userId);
    }
}