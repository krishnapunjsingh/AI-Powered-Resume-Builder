package com.resumeai.export.client;

import com.resumeai.export.dto.ProfileResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AuthServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceClient.class);

    private final RestTemplate restTemplate;
    private final String authServiceUrl;

    public AuthServiceClient(
            RestTemplate restTemplate,
            @Value("${auth.service.url:http://localhost:8081}") String authServiceUrl) {
        this.restTemplate = restTemplate;
        this.authServiceUrl = authServiceUrl;
    }

    public ProfileResponse getProfile(Long userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Id", String.valueOf(userId));
            return restTemplate
                    .exchange(authServiceUrl + "/auth/profile", org.springframework.http.HttpMethod.GET, new HttpEntity<>(headers), ProfileResponse.class)
                    .getBody();
        } catch (Exception exception) {
            log.warn("Could not fetch user profile from auth-service for user {}", userId, exception);
            return null;
        }
    }
}
