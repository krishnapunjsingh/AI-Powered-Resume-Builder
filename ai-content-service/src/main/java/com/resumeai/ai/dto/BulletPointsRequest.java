package com.resumeai.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BulletPointsRequest(
        @NotNull Long userId,
        @NotNull Long resumeId,
        @NotBlank String sectionName,
        @NotBlank String content,
        String model
) {
}
