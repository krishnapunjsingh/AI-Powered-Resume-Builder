package com.resumeai.notification.service.impl;

import com.resumeai.notification.service.EmailDeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class SmtpEmailDeliveryService implements EmailDeliveryService {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailDeliveryService.class);

    private final JavaMailSender mailSender;
    private final boolean emailEnabled;
    private final String fromAddress;

    public SmtpEmailDeliveryService(
            JavaMailSender mailSender,
            @Value("${notification.email.enabled:false}") boolean emailEnabled,
            @Value("${app.mail.from:no-reply@resumeai.local}") String fromAddress) {
        this.mailSender = mailSender;
        this.emailEnabled = emailEnabled;
        this.fromAddress = fromAddress;
    }

    @Override
    public boolean send(String to, String subject, String body) {
        if (!emailEnabled) {
            log.info("Email notifications are disabled. Skipping email to {}", to);
            return true;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            return true;
        } catch (Exception exception) {
            log.warn("Could not send email notification to {}", to, exception);
            return false;
        }
    }
}
