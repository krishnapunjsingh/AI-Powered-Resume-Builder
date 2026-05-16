package com.resumeai.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CoverLetterRequest(
        @NotNull Long userId,
        @NotNull Long resumeId,
        @NotBlank String fullName,
        @NotBlank String companyName,
        @NotBlank String jobTitle,
        @NotBlank String resumeHighlights,
        String model
) {
}
