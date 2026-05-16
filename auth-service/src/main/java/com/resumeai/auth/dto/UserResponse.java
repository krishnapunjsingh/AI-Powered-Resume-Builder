package com.resumeai.auth.dto;

import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    String fullName,
    String email,
    String role,
    String subscriptionPlan,
    boolean isActive,
    LocalDateTime lastLoginAt,
    LocalDateTime createdAt
) {}
