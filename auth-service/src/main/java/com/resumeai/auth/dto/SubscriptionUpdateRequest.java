package com.resumeai.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record SubscriptionUpdateRequest(
        @NotBlank String subscriptionPlan
) {
}
