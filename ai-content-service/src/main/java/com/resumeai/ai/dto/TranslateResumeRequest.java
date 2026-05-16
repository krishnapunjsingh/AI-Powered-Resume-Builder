package com.resumeai.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TranslateResumeRequest(
        @NotNull Long userId,
        @NotNull Long resumeId,
        @NotBlank String text,
        @NotBlank String targetLanguage,
        String model
) {
}
