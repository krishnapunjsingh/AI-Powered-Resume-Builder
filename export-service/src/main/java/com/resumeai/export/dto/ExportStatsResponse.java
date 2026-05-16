package com.resumeai.export.dto;

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
public class ExportStatsResponse {

    private Long totalExports;

    private Long totalByUser;

    private Long completedCount;

    private Long failedCount;

    private Long queuedCount;

    private Long pdfCount;

    private Long docxCount;

    private Long jsonCount;

    private Double averageFileSize; // in KB
}
