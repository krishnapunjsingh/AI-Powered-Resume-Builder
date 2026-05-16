package com.resumeai.auth.dto;

public record ProfileResponse(
        Long userId,
        String fullName,
        String email,
        String phone,
        String role,
        String provider,
        String subscriptionPlan,
        boolean isActive
) {
}
