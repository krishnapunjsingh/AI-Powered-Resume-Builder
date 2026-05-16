package com.resumeai.auth.dto;

public record UserValidationResponse(
        Long userId,
        boolean exists
) {
}