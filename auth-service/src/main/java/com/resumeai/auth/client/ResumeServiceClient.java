package com.resumeai.auth.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ResumeServiceClient {

    private final RestTemplate restTemplate;
    private final String resumeServiceUrl;

    public ResumeServiceClient(RestTemplate restTemplate, 
                           @Value("${resume.service.url:http://localhost:8082}") String resumeServiceUrl) {
        this.restTemplate = restTemplate;
        this.resumeServiceUrl = resumeServiceUrl;
    }

    public long getTotalResumesCount() {
        try {
            String url = resumeServiceUrl + "/resumes/admin/count";
            return restTemplate.getForObject(url, Long.class);
        } catch (Exception e) {
            // Fallback to 0 if service is unavailable
            return 0L;
        }
    }
}
