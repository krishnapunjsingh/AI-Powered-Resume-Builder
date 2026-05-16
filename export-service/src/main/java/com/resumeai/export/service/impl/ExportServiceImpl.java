package com.resumeai.export.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.resumeai.export.client.AuthServiceClient;
import com.resumeai.export.client.NotificationServiceClient;
import com.resumeai.export.dto.ExportRequest;
import com.resumeai.export.dto.ExportResponse;
import com.resumeai.export.dto.ExportStatsResponse;
import com.resumeai.export.dto.ProfileResponse;
import com.resumeai.export.entity.ExportJob;
import com.resumeai.export.exception.ExportException;
import com.resumeai.export.exception.ExportJobNotFoundException;
import com.resumeai.export.exception.InvalidExportFormatException;
import com.resumeai.export.repository.ExportRepository;
import com.resumeai.export.service.ExportService;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExportServiceImpl implements ExportService {

    private final ExportRepository exportRepository;
    private final ObjectMapper objectMapper;
    private final AuthServiceClient authServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    private static final List<String> VALID_FORMATS = List.of("PDF", "DOCX", "JSON");
    private static final int EXPIRY_DAYS = 7;
    private static final long MAX_EXPORTS_PER_DAY = 10;

    @Override
    public ExportResponse submitExport(ExportRequest request, Long userId) {
        log.info("Submitting export job for user {} with format {}", userId, request.getFormat());

        // Validate format
        if (!VALID_FORMATS.contains(request.getFormat().toUpperCase())) {
            throw new InvalidExportFormatException(
                    "Invalid format: " + request.getFormat() + ". Supported formats: PDF, DOCX, JSON"
            );
        }

        // Check daily limit
        Long dailyCount = exportRepository.countByUserIdToday(userId, LocalDateTime.now());
        if (dailyCount >= MAX_EXPORTS_PER_DAY) {
            throw new ExportException("Daily export limit of " + MAX_EXPORTS_PER_DAY + " reached");
        }

        // Create export job
        String jobId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        ExportJob job = ExportJob.builder()
                .jobId(jobId)
                .resumeId(request.getResumeId())
                .userId(userId)
                .format(request.getFormat().toUpperCase())
                .status("QUEUED")
                .templateId(request.getTemplateId())
                .customizations(request.getCustomizations())
                .requestedAt(now)
                .expiresAt(now.plusDays(EXPIRY_DAYS))
                .build();

        ExportJob savedJob = exportRepository.save(job);
        log.info("Export job created with ID: {}", jobId);

        // Process asynchronously
        processExportAsync(jobId);

        return mapToResponse(savedJob);
    }

    @Override
    public ExportResponse getJobStatus(String jobId) {
        log.info("Fetching status for job: {}", jobId);
        ExportJob job = exportRepository.findByJobId(jobId)
                .orElseThrow(() -> new ExportJobNotFoundException("Export job not found: " + jobId));
        return mapToResponse(job);
    }

    @Override
    public ExportResponse downloadFile(String jobId) {
        log.info("Downloading file for job: {}", jobId);
        ExportJob job = exportRepository.findByJobId(jobId)
                .orElseThrow(() -> new ExportJobNotFoundException("Export job not found: " + jobId));

        if (!"COMPLETED".equals(job.getStatus())) {
            throw new ExportException("Export job is not completed yet. Status: " + job.getStatus());
        }

        if (job.getFileUrl() == null) {
            throw new ExportException("File URL not available for download");
        }

        return mapToResponse(job);
    }

    @Override
    public byte[] getFileBytes(String jobId) {
        log.info("Generating downloadable bytes for job: {}", jobId);
        ExportJob job = exportRepository.findByJobId(jobId)
                .orElseThrow(() -> new ExportJobNotFoundException("Export job not found: " + jobId));

        if (!"COMPLETED".equals(job.getStatus())) {
            throw new ExportException("Export job is not completed yet. Status: " + job.getStatus());
        }

        if ("PDF".equalsIgnoreCase(job.getFormat())) {
            return generateSimplePdf(job);
        }

        return generateExportContent(job).getBytes(StandardCharsets.UTF_8);
    }  

    @Override
    public List<ExportResponse> getExportsByUser(Long userId) {
        log.info("Fetching exports for user: {}", userId);
        return exportRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void deleteExport(String jobId) {
        log.info("Deleting export job: {}", jobId);
        ExportJob job = exportRepository.findByJobId(jobId)
                .orElseThrow(() -> new ExportJobNotFoundException("Export job not found: " + jobId));
        exportRepository.delete(job);
    }

    @Override
    @Scheduled(cron = "0 0 0 * * *") // Run at midnight daily
    public void cleanupExpiredExports() {
        log.info("Starting cleanup of expired exports");
        List<ExportJob> expiredJobs = exportRepository.findExpiredJobs(LocalDateTime.now());
        log.info("Found {} expired jobs to clean up", expiredJobs.size());

        for (ExportJob job : expiredJobs) {
            exportRepository.delete(job);
            log.debug("Deleted expired export job: {}", job.getJobId());
        }

        log.info("Cleanup completed, deleted {} jobs", expiredJobs.size());
    }

    @Override
    public ExportStatsResponse getExportStats(Long userId) {
        log.info("Generating export statistics for user: {}", userId);

        long totalByUser = exportRepository.findByUserId(userId).size();
        long completedCount = exportRepository.findByStatus("COMPLETED").size();
        long failedCount = exportRepository.findByStatus("FAILED").size();
        long queuedCount = exportRepository.findByStatus("QUEUED").size();
        long pdfCount = exportRepository.countByFormat("PDF");
        long docxCount = exportRepository.countByFormat("DOCX");
        long jsonCount = exportRepository.countByFormat("JSON");

        double avgFileSize = calculateAverageFileSize();

        return ExportStatsResponse.builder()
                .totalExports(exportRepository.count())
                .totalByUser(totalByUser)
                .completedCount(completedCount)
                .failedCount(failedCount)
                .queuedCount(queuedCount)
                .pdfCount(pdfCount)
                .docxCount(docxCount)
                .jsonCount(jsonCount)
                .averageFileSize(avgFileSize)
                .build();
    }

    @Override
    public List<ExportResponse> getExportsByResumeId(Long resumeId) {
        log.info("Fetching exports for resume: {}", resumeId);
        return exportRepository.findByResumeId(resumeId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ExportResponse processExport(String jobId) {
        log.info("Processing export job: {}", jobId);
        ExportJob job = exportRepository.findByJobId(jobId)
                .orElseThrow(() -> new ExportJobNotFoundException("Export job not found: " + jobId));

        try {
            job.setStatus("PROCESSING");
            exportRepository.save(job);

            // Simulate export processing based on format
            String content = generateExportContent(job);
            String fileUrl = generateDownloadUrl(job);

            job.setStatus("COMPLETED");
            job.setFileUrl(fileUrl);
            job.setFileSizeKb(Math.max(1L, (long) Math.ceil(getFileBytesForSize(job, content).length / 1024.0)));
            job.setCompletedAt(LocalDateTime.now());

            ExportJob savedJob = exportRepository.save(job);
            log.info("Export job processed successfully: {}", jobId);
            sendExportNotification(savedJob, true);

            return mapToResponse(savedJob);

        } catch (Exception e) {
            log.error("Error processing export job: {}", jobId, e);
            job.setStatus("FAILED");
            job.setErrorMessage(e.getMessage());
            ExportJob failedJob = exportRepository.save(job);
            sendExportNotification(failedJob, false);
            throw new ExportException("Failed to process export: " + e.getMessage(), e);
        }
    }

    @Async
    protected void processExportAsync(String jobId) {
        log.info("Starting async processing for job: {}", jobId);
        try {
            Thread.sleep(2000); // Simulate processing delay
            processExport(jobId);
        } catch (InterruptedException e) {
            log.error("Async processing interrupted for job: {}", jobId);
            Thread.currentThread().interrupt();
        }
    }

    private String generateExportContent(ExportJob job) {
        log.debug("Generating export content for format: {}", job.getFormat());

        Map<String, Object> content = new HashMap<>();
        content.put("jobId", job.getJobId());
        content.put("resumeId", job.getResumeId());
        content.put("format", job.getFormat());
        content.put("generatedAt", LocalDateTime.now());
        content.put("resume", parseCustomizations(job).path("resume"));
        content.put("sections", parseCustomizations(job).path("sections"));

        try {
            return objectMapper.writeValueAsString(content);
        } catch (Exception e) {
            log.error("Error generating export content", e);
            throw new ExportException("Failed to generate export content", e);
        }
    }

    private String generateDownloadUrl(ExportJob job) {
        return String.format("http://localhost:8080/exports/file/%s", job.getJobId());
    }

    private void sendExportNotification(ExportJob job, boolean completed) {
        try {
            ProfileResponse profile = authServiceClient.getProfile(job.getUserId());
            String email = profile == null ? null : profile.email();
            String type = completed ? "EXPORT_READY" : "EXPORT_FAILED";
            String title = completed ? "Export ready" : "Export failed";
            String message = completed
                    ? "Your " + job.getFormat() + " export is ready to download."
                    : "Your " + job.getFormat() + " export could not be generated. Please try again.";

            notificationServiceClient.sendToAppAndEmail(
                    job.getUserId(),
                    email,
                    type,
                    title,
                    message,
                    job.getId()
            );
        } catch (Exception exception) {
            log.warn("Could not send export notification for job {}", job.getJobId(), exception);
        }
    }

    private byte[] getFileBytesForSize(ExportJob job, String content) {
        if ("PDF".equalsIgnoreCase(job.getFormat())) {
            return generateSimplePdf(job);
        }
        return content.getBytes(StandardCharsets.UTF_8);
    }

    private byte[] generateSimplePdf(ExportJob job) {
        JsonNode root = parseCustomizations(job);
        JsonNode resume = root.path("resume");
        JsonNode sections = root.path("sections");
        String title = textValue(resume.path("title"), "Resume");
        String summary = textValue(resume.path("summary"), "");

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfDocument pdf = new PdfDocument(new PdfWriter(output));
            Document document = new Document(pdf);
            document.setMargins(42, 50, 42, 50);

            PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            DeviceRgb accent = new DeviceRgb(18, 107, 95);
            DeviceRgb body = new DeviceRgb(39, 48, 57);
            DeviceRgb muted = new DeviceRgb(93, 105, 116);

            document.add(new Paragraph(title)
                    .setFont(bold)
                    .setFontSize(24)
                    .setFontColor(ColorConstants.BLACK)
                    .setMarginBottom(8)
                    .setTextAlignment(TextAlignment.LEFT));

            if (!summary.isBlank()) {
                document.add(new Paragraph(summary)
                        .setFont(regular)
                        .setFontSize(10.8f)
                        .setFontColor(body)
                        .setFixedLeading(15)
                        .setMarginTop(0)
                        .setMarginBottom(14));
            }

            if (sections.isArray() && sections.size() > 0) {
                for (JsonNode section : sections) {
                    String sectionName = textValue(section.path("sectionName"), "Section");
                    String content = textValue(section.path("content"), "");
                    if (!content.isBlank()) {
                        addSection(document, regular, bold, accent, body, sectionName, content);
                    }
                }
            } else {
                addSection(document, regular, bold, accent, body, "Resume Content",
                        "No resume sections were sent with this export. Open Builder, add sections, then create a new export.");
            }

            document.add(new Paragraph("Generated by ResumeAI on " + LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                    .setFont(regular)
                    .setFontSize(8.5f)
                    .setFontColor(muted)
                    .setMarginTop(18));

            document.close();
            return output.toByteArray();
        } catch (Exception exception) {
            log.error("Error generating styled PDF for job {}", job.getJobId(), exception);
            throw new ExportException("Failed to generate PDF export", exception);
        }
    }

    private void addSection(Document document,
                            PdfFont regular,
                            PdfFont bold,
                            DeviceRgb accent,
                            DeviceRgb body,
                            String sectionName,
                            String content) {
        document.add(new Paragraph(sectionName.toUpperCase())
                .setFont(bold)
                .setFontSize(11.5f)
                .setFontColor(accent)
                .setMarginTop(11)
                .setMarginBottom(5)
                .setPaddingBottom(3)
                .setBorderBottom(new SolidBorder(accent, 0.8f)));

        for (String paragraph : content.split("\\R+")) {
            String text = paragraph.trim();
            if (text.isBlank()) {
                continue;
            }
            document.add(new Paragraph(text)
                    .setFont(regular)
                    .setFontSize(10.4f)
                    .setFontColor(body)
                    .setFixedLeading(14.4f)
                    .setMarginTop(0)
                    .setMarginBottom(3));
        }
    }

    private List<String> buildResumeLines(ExportJob job) {
        JsonNode root = parseCustomizations(job);
        JsonNode resume = root.path("resume");
        JsonNode sections = root.path("sections");
        List<String> lines = new ArrayList<>();

        String title = textValue(resume.path("title"), "Resume");
        lines.add(title);
        addWrapped(lines, textValue(resume.path("summary"), ""), 86);
        lines.add("");

        if (sections.isArray() && sections.size() > 0) {
            for (JsonNode section : sections) {
                String sectionName = textValue(section.path("sectionName"), "Section");
                String content = textValue(section.path("content"), "");
                if (!content.isBlank()) {
                    lines.add(sectionName.toUpperCase());
                    addWrapped(lines, content, 86);
                    lines.add("");
                }
            }
        } else {
            lines.add("No resume sections were sent with this export.");
            lines.add("Open Builder, add sections, then create a new export.");
        }

        lines.add("Generated by ResumeAI on " + LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        return lines;
    }

    private JsonNode parseCustomizations(ExportJob job) {
        if (job.getCustomizations() == null || job.getCustomizations().isBlank()) {
            return objectMapper.createObjectNode();
        }

        try {
            return objectMapper.readTree(job.getCustomizations());
        } catch (Exception e) {
            log.warn("Could not parse export customizations for job {}", job.getJobId(), e);
            return objectMapper.createObjectNode();
        }
    }

    private String textValue(JsonNode node, String fallback) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return fallback;
        }
        String value = node.asText("").replace('\r', ' ').trim();
        return value.isBlank() ? fallback : value;
    }

    private void addWrapped(List<String> lines, String text, int width) {
        if (text == null || text.isBlank()) {
            return;
        }

        for (String paragraph : text.split("\\n")) {
            String current = "";
            for (String word : paragraph.trim().split("\\s+")) {
                if (word.isBlank()) {
                    continue;
                }
                if (current.isBlank()) {
                    current = word;
                } else if (current.length() + word.length() + 1 <= width) {
                    current += " " + word;
                } else {
                    lines.add(current);
                    current = word;
                }
            }
            if (!current.isBlank()) {
                lines.add(current);
            }
        }
    }

    private double calculateAverageFileSize() {
        List<ExportJob> completedJobs = exportRepository.findByStatus("COMPLETED");
        if (completedJobs.isEmpty()) {
            return 0.0;
        }

        long totalSize = completedJobs.stream()
                .map(ExportJob::getFileSizeKb)
                .filter(size -> size != null)
                .mapToLong(Long::longValue)
                .sum();

        return (double) totalSize / completedJobs.size();
    }

    private ExportResponse mapToResponse(ExportJob job) {
        return ExportResponse.builder()
                .id(job.getId())
                .jobId(job.getJobId())
                .resumeId(job.getResumeId())
                .format(job.getFormat())
                .status(job.getStatus())
                .fileUrl(job.getFileUrl())
                .fileSizeKb(job.getFileSizeKb())
                .requestedAt(job.getRequestedAt())
                .completedAt(job.getCompletedAt())
                .expiresAt(job.getExpiresAt())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .errorMessage(job.getErrorMessage())
                .build();
    }
}
