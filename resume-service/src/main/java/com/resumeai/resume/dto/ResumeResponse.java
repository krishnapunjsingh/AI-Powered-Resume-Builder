package com.resumeai.resume.dto;

import java.time.LocalDateTime;

public record ResumeResponse(
        Long id,
        Long userId,
        String title,
        String summary,
        String status,
        boolean isPublic,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
