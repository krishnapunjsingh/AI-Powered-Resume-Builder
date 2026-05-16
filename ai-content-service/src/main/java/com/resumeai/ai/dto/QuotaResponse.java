package com.resumeai.ai.dto;

public record QuotaResponse(
        Long userId,
        long monthlyLimit,
        long requestsUsed,
        long remainingQuota,
        long tokensUsedThisMonth
) {
}
