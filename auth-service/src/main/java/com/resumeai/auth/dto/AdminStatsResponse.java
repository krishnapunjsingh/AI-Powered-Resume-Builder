package com.resumeai.auth.dto;

public record AdminStatsResponse(
    Long totalUsers,
    Long activeUsers,
    Long suspendedUsers,
    Long totalResumes,
    Long totalTemplates
) {}
