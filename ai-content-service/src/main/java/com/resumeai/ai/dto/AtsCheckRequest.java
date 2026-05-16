package com.resumeai.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AtsCheckRequest(
        @NotNull Long userId,
        @NotNull Long resumeId,
        @NotBlank String resumeText,
        @NotBlank String jobDescription,
        String model
) {
}
