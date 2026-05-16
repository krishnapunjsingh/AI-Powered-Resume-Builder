package com.resumeai.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationRequest(
        @NotNull(message = "Recipient ID is required")
        Long recipientId,

        @Email(message = "Recipient email must be valid")
        String recipientEmail,

        @NotBlank(message = "Notification type is required")
        String type,

        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Message is required")
        String message,

        String channel,

        @NotNull(message = "Related ID is required")
        Long relatedId,

        @NotBlank(message = "Related type is required")
        String relatedType
) {
}
