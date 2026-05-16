package com.resumeai.resume.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResumeRequest(
        @NotNull Long userId,
        @NotBlank @Size(max = 150) String title,
        String summary,
        @Pattern(regexp = "^(?i)(DRAFT|COMPLETE)$", message = "status must be DRAFT or COMPLETE") String status
) {
}
