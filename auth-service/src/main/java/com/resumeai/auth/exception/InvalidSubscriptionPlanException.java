package com.resumeai.auth.exception;

public class InvalidSubscriptionPlanException extends AuthException {

    public InvalidSubscriptionPlanException() {
        super("subscriptionPlan must be FREE or PREMIUM");
    }
}
