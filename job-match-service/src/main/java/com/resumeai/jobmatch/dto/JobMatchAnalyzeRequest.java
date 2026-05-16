package com.resumeai.jobmatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record JobMatchAnalyzeRequest(
        @NotNull Long userId,
        @NotNull Long resumeId,
        @NotBlank @Size(max = 150) String jobTitle,
        @NotBlank @Size(max = 5000) String jobDescription,
        @NotBlank @Size(max = 5000) String resumeContent,
        @Size(max = 40) String source,
        @Size(max = 120) String location,
        @Size(max = 200) String keywords
) {
}