package com.resumeai.ai.dto;

import java.time.LocalDateTime;

public record AiRequestResponse(
        String requestId,
        Long userId,
        Long resumeId,
        String requestType,
        String model,
        Integer tokensUsed,
        String status,
        String aiResponse,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {
}
