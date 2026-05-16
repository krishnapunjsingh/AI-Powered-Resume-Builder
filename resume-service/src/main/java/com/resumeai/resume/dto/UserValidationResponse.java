package com.resumeai.resume.dto;

public record UserValidationResponse(
        Long userId,
        boolean exists
) {
}