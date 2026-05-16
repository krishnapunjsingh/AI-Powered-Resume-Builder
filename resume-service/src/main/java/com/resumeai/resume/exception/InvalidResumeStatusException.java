package com.resumeai.resume.exception;

public class InvalidResumeStatusException extends ResumeException {

    public InvalidResumeStatusException(String status) {
        super("Invalid resume status: " + status + ". Allowed values are DRAFT or COMPLETE");
    }
}