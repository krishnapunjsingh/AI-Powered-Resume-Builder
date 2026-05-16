package com.resumeai.section.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SectionRequest(
        @NotNull Long resumeId,
        @NotBlank String sectionName,
        @NotBlank String content,
        @NotNull @Min(0) Integer displayOrder
) {
}
