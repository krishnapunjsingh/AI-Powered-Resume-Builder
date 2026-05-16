package com.resumeai.export.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportRequest {

    @NotNull(message = "Resume ID is required")
    private Long resumeId;

    @NotBlank(message = "Format is required (PDF, DOCX, JSON)")
    private String format; // PDF, DOCX, JSON

    private Long templateId;

    private String customizations; // JSON color/font customizations
}
