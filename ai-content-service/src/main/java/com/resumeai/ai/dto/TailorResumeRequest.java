package com.resumeai.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TailorResumeRequest(
        @NotNull Long userId,
        @NotNull Long resumeId,
        @NotBlank String resumeJson,
        @NotBlank String jobDescription,
        String model
) {
}
