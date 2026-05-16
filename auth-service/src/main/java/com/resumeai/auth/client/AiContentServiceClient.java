package com.resumeai.auth.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AiContentServiceClient {

    private final RestTemplate restTemplate;
    private final String aiContentServiceUrl;

    public AiContentServiceClient(RestTemplate restTemplate, 
                              @Value("${ai-content.service.url:http://localhost:8084}") String aiContentServiceUrl) {
        this.restTemplate = restTemplate;
        this.aiContentServiceUrl = aiContentServiceUrl;
    }

    public Long getTotalAiUsageCount() {
        try {
            String url = aiContentServiceUrl + "/ai/admin/usage/count";
            Long result = restTemplate.getForObject(url, Long.class);
            return result != null ? result : 0L;
        } catch (Exception e) {
            // Fallback to 0 if service is unavailable
            return 0L;
        }
    }

    public Double getTotalAiCost() {
        try {
            String url = aiContentServiceUrl + "/ai/admin/cost/total";
            Double result = restTemplate.getForObject(url, Double.class);
            return result != null ? result : 0.0;
        } catch (Exception e) {
            // Fallback to 0 if service is unavailable
            return 0.0;
        }
    }
}
