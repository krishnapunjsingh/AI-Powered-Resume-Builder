package com.resumeai.export.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
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
@Entity
@Table(name = "export_jobs")
public class ExportJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String jobId;

    @Column(nullable = false)
    private Long resumeId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 20)
    private String format; // PDF, DOCX, JSON

    @Builder.Default
    @Column(nullable = false, length = 30)
    private String status = "QUEUED"; // QUEUED, PROCESSING, COMPLETED, FAILED

    @Column(columnDefinition = "TEXT")
    private String fileUrl; // S3 pre-signed URL

    @Column
    private Long fileSizeKb;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    @Column
    private LocalDateTime completedAt;

    @Column
    private LocalDateTime expiresAt;

    @Column
    private Long templateId;

    @Column(columnDefinition = "JSON")
    private String customizations; // JSON color/font customizations

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
