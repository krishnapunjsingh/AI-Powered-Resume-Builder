package com.resumeai.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SkillsSuggestionRequest(
        @NotNull Long userId,
        @NotNull Long resumeId,
        @NotBlank String resumeText,
        @NotBlank String jobDescription,
        List<String> currentSkills,
        String model
) {
}
