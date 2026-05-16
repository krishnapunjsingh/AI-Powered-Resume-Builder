package com.resumeai.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SummaryRequest(
        @NotNull Long userId,
        @NotNull Long resumeId,
        @NotBlank @Size(max = 150) String resumeTitle,
        @NotBlank String resumeText,
        String targetRole,
        String model
) {
}
