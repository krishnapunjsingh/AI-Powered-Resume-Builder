package com.resumeai.auth.exception;

public class UnsupportedProviderException extends AuthException {

    public UnsupportedProviderException() {
        super("Only GOOGLE and LINKEDIN OAuth providers are supported");
    }
}
