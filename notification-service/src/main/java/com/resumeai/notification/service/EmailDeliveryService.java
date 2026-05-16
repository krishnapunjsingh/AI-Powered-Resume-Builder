package com.resumeai.notification.service;

public interface EmailDeliveryService {

    boolean send(String to, String subject, String body);
}
