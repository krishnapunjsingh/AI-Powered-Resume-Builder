package com.resumeai.jobmatch.entity;

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
@Table(name = "job_matches")
public class JobMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long matchId;

    @Column(nullable = false)
    private Long resumeId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 150)
    private String jobTitle;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String jobDescription;

    @Column(nullable = false)
    private Integer matchScore;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String missingSkills;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String recommendations;

    @Column(nullable = false, length = 40)
    private String source;

    @Builder.Default
    @Column(nullable = false)
    private boolean bookmarked = false;

    @Column(nullable = false)
    private LocalDateTime matchedAt;

    @PrePersist
    void onCreate() {
        if (source == null || source.isBlank()) {
            source = "MANUAL";
        }
        matchedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        matchedAt = matchedAt == null ? LocalDateTime.now() : matchedAt;
    }
}