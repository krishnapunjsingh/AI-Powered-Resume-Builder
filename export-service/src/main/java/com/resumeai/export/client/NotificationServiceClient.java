package com.resumeai.export.client;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class NotificationServiceClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceClient.class);

    private final RestTemplate restTemplate;
    private final String notificationServiceUrl;

    public NotificationServiceClient(
            RestTemplate restTemplate,
            @Value("${notification.service.url:http://localhost:8089}") String notificationServiceUrl) {
        this.restTemplate = restTemplate;
        this.notificationServiceUrl = notificationServiceUrl;
    }

    public void sendToAppAndEmail(Long recipientId,
                                  String recipientEmail,
                                  String type,
                                  String title,
                                  String message,
                                  Long relatedId) {
        List.of("APP", "EMAIL").forEach(channel -> send(new NotificationRequest(
                recipientId,
                recipientEmail,
                type,
                title,
                message,
                channel,
                relatedId,
                "EXPORT"
        )));
    }

    private void send(NotificationRequest request) {
        try {
            restTemplate.postForObject(notificationServiceUrl + "/notifications/send", request, Object.class);
        } catch (Exception exception) {
            log.warn("Could not send export notification", exception);
        }
    }

    public record NotificationRequest(
            Long recipientId,
            String recipientEmail,
            String type,
            String title,
            String message,
            String channel,
            Long relatedId,
            String relatedType
    ) {
    }
}
