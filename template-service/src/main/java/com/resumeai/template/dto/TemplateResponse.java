package com.resumeai.template.dto;

import java.time.LocalDateTime;

public record TemplateResponse(
        Integer templateId,
        String name,
        String description,
        String thumbnailUrl,
        String htmlLayout,
        String cssStyles,
        String category,
        Boolean isPremium,
        Boolean isActive,
        Integer usageCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}