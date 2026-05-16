package com.resumeai.ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
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
@Table(name = "ai_requests")
public class AiRequest {

    @Id
    @Column(nullable = false, length = 36)
    private String requestId;

    @Column(nullable = false)
    private Long userId;

    private Long resumeId;

    @Column(nullable = false, length = 50)
    private String requestType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String inputPrompt;

    @Column(columnDefinition = "TEXT")
    private String aiResponse;

    @Column(nullable = false, length = 30)
    private String model;

    @Builder.Default
    @Column(nullable = false)
    private Integer tokensUsed = 0;

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String status = "QUEUED";

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
