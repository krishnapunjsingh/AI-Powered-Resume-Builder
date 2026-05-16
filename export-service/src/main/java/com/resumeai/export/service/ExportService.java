package com.resumeai.export.service;

import com.resumeai.export.dto.ExportRequest;
import com.resumeai.export.dto.ExportResponse;
import com.resumeai.export.dto.ExportStatsResponse;
import java.util.List;

public interface ExportService {

    ExportResponse submitExport(ExportRequest request, Long userId);

    ExportResponse getJobStatus(String jobId);

    ExportResponse downloadFile(String jobId);

    byte[] getFileBytes(String jobId);

    List<ExportResponse> getExportsByUser(Long userId);

    void deleteExport(String jobId);

    void cleanupExpiredExports();

    ExportStatsResponse getExportStats(Long userId);

    List<ExportResponse> getExportsByResumeId(Long resumeId);

    ExportResponse processExport(String jobId);
}
