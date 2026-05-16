package com.resumeai.auth.dto;

public record AuthResponse(
        String token,
        Long userId,
        String email,
        String fullName,
        String role
) {
}
