package com.resumeai.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OAuthLoginRequest(
        @NotBlank String provider,
        @Email @NotBlank String email,
        @NotBlank String fullName
) {
}
