package com.resumeai.template.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TemplateRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotBlank String thumbnailUrl,
        @NotBlank String htmlLayout,
        @NotBlank String cssStyles,
        @NotBlank String category,
        @NotNull Boolean isPremium,
        Boolean isActive
) {
}