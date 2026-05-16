package com.resumeai.auth.client;

import com.resumeai.auth.service.AdminService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class TemplateServiceClient {

    private final RestTemplate restTemplate;
    private final String templateServiceUrl;

    public TemplateServiceClient(RestTemplate restTemplate, 
                             @Value("${template.service.url:http://localhost:8085}") String templateServiceUrl) {
        this.restTemplate = restTemplate;
        this.templateServiceUrl = templateServiceUrl;
    }

    public long getTotalTemplatesCount() {
        try {
            String url = templateServiceUrl + "/templates/admin/count";
            return restTemplate.getForObject(url, Long.class);
        } catch (Exception e) {
            // Fallback to 0 if service is unavailable
            return 0L;
        }
    }

    public List<AdminService.TemplateResponse> getAllTemplates() {
        try {
            String url = templateServiceUrl + "/templates";
            // For now, return empty list - we'll implement proper mapping later
            // This avoids complex cross-service dependencies
            return List.of();
        } catch (Exception e) {
            // Fallback to empty list if service is unavailable
            return List.of();
        }
    }
}
