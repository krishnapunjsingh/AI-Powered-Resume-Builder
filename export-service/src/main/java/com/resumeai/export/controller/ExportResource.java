package com.resumeai.export.controller;

import com.resumeai.export.dto.ExportRequest;
import com.resumeai.export.dto.ExportResponse;
import com.resumeai.export.dto.ExportStatsResponse;
import com.resumeai.export.service.ExportService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/exports")
@RequiredArgsConstructor
public class ExportResource {

    private final ExportService exportService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ExportResponse submitExport(
            @Valid @RequestBody ExportRequest request,
            Authentication authentication) {
        log.info("POST /exports - Submitting export for user: {}", authentication.getName());
        Long userId = Long.parseLong(authentication.getName());
        return exportService.submitExport(request, userId);
    }

    @GetMapping("/job/{jobId}")
    public ExportResponse getJobStatus(@PathVariable String jobId) {
        log.info("GET /exports/job/{} - Fetching status", jobId);
        return exportService.getJobStatus(jobId);
    }

    @GetMapping("/download/{jobId}")
    public ExportResponse downloadFile(@PathVariable String jobId) {
        log.info("GET /exports/download/{} - Downloading file", jobId);
        return exportService.downloadFile(jobId);
    }

    @GetMapping("/file/{jobId}")
    public ResponseEntity<byte[]> downloadFileBytes(@PathVariable String jobId) {
        log.info("GET /exports/file/{} - Streaming file", jobId);
        ExportResponse export = exportService.downloadFile(jobId);
        byte[] file = exportService.getFileBytes(jobId);
        String extension = export.getFormat() == null ? "pdf" : export.getFormat().toLowerCase();
        MediaType mediaType = "PDF".equalsIgnoreCase(export.getFormat())
                ? MediaType.APPLICATION_PDF
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume." + extension + "\"")
                .body(file);
    }

    @GetMapping("/user")
    public List<ExportResponse> getExportsByUser(Authentication authentication) {
        log.info("GET /exports/user - Fetching exports for user: {}", authentication.getName());
        Long userId = Long.parseLong(authentication.getName());
        return exportService.getExportsByUser(userId);
    }

    @GetMapping("/resume/{resumeId}")
    public List<ExportResponse> getExportsByResumeId(@PathVariable Long resumeId) {
        log.info("GET /exports/resume/{} - Fetching exports", resumeId);
        return exportService.getExportsByResumeId(resumeId);
    }

    @GetMapping("/stats")
    public ExportStatsResponse getExportStats(Authentication authentication) {
        log.info("GET /exports/stats - Getting stats for user: {}", authentication.getName());
        Long userId = Long.parseLong(authentication.getName());
        return exportService.getExportStats(userId);
    }

    @DeleteMapping("/{jobId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExport(@PathVariable String jobId) {
        log.info("DELETE /exports/{} - Deleting export", jobId);
        exportService.deleteExport(jobId);
    }
}
