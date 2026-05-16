package com.resumeai.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EmailOtpRequest(
        @Email @NotBlank String email,

        @NotBlank
        @Pattern(regexp = "\\d{6}", message = "OTP must be a 6 digit code")
        String otp
) {
}
