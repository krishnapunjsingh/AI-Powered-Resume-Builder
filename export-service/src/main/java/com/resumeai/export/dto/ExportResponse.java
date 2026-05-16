package com.resumeai.export.dto;

import java.time.LocalDateTime;
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
public class ExportResponse {

    private Long id;

    private String jobId;

    private Long resumeId;

    private String format;

    private String status;

    private String fileUrl;

    private Long fileSizeKb;

    private LocalDateTime requestedAt;

    private LocalDateTime completedAt;

    private LocalDateTime expiresAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String errorMessage;
}
