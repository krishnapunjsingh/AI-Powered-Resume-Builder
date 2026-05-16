package com.resumeai.resume.feign;

import com.resumeai.resume.dto.UserValidationResponse;
import com.resumeai.resume.dto.ProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service", url = "http://localhost:8081")
public interface AuthServiceFeignClient {

    @GetMapping("/auth/users/{userId}/exists")
    UserValidationResponse userExists(@PathVariable("userId") Long userId);

    @GetMapping("/auth/profile")
    ProfileResponse getProfile(@RequestHeader("X-User-Id") Long userId);
}
