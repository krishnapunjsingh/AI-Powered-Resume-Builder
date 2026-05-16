package com.resumeai.jobmatch.exception;

public class JobMatchNotFoundException extends RuntimeException {

    public JobMatchNotFoundException(Long id) {
        super("Job match not found: " + id);
    }
}