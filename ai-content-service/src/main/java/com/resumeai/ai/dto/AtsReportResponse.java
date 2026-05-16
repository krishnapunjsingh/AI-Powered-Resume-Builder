package com.resumeai.ai.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AtsReportResponse(
        String requestId,
        Long userId,
        Long resumeId,
        String requestType,
        String model,
        Integer tokensUsed,
        String status,
        int atsScore,
        List<String> matchedKeywords,
        List<String> missingKeywords,
        List<String> recommendations,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {
}
