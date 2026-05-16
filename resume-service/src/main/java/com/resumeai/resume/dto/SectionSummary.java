package com.resumeai.resume.dto;

import java.time.LocalDateTime;

public record SectionSummary(
        Long id,
        Long resumeId,
        String sectionName,
        String content,
        Integer displayOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
