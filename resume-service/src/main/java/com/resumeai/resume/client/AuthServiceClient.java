package com.resumeai.resume.client;

import com.resumeai.resume.dto.UserValidationResponse;
import com.resumeai.resume.dto.ProfileResponse;
import com.resumeai.resume.exception.ExternalServiceException;
import com.resumeai.resume.feign.AuthServiceFeignClient;
import org.springframework.stereotype.Component;

@Component
public class AuthServiceClient {

    private final AuthServiceFeignClient authServiceFeignClient;

    public AuthServiceClient(AuthServiceFeignClient authServiceFeignClient) {
        this.authServiceFeignClient = authServiceFeignClient;
    }

    public boolean userExists(Long userId) {
        try {
            UserValidationResponse response = authServiceFeignClient.userExists(userId);
            return response != null && response.exists();
        } catch (Exception exception) {
            throw new ExternalServiceException("Unable to validate user with auth-service");
        }
    }

    public ProfileResponse getProfile(Long userId) {
        try {
            return authServiceFeignClient.getProfile(userId);
        } catch (Exception exception) {
            throw new ExternalServiceException("Unable to load user profile from auth-service");
        }
    }
}
