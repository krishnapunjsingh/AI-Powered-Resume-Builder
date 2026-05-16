package com.resumeai.resume.exception;

public class ResumeNotFoundException extends ResumeException {

    public ResumeNotFoundException(Long id) {
        super("Resume not found for id: " + id);
    }
}