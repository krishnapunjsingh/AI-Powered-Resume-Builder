package com.resumeai.section.dto;

import java.time.LocalDateTime;

public record SectionResponse(
        Long id,
        Long resumeId,
        String sectionName,
        String content,
        Integer displayOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
