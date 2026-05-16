package com.resumeai.auth.exception;

public class InvalidOtpException extends AuthException {

    public InvalidOtpException() {
        super("Invalid or expired OTP");
    }
}
