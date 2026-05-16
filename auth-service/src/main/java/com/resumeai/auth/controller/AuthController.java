package com.resumeai.auth.controller;

import com.resumeai.auth.dto.AuthRequest;
import com.resumeai.auth.dto.AuthResponse;
import com.resumeai.auth.dto.ChangePasswordRequest;
import com.resumeai.auth.dto.EmailOtpRequest;
import com.resumeai.auth.dto.OAuthLoginRequest;
import com.resumeai.auth.dto.ProfileResponse;
import com.resumeai.auth.dto.ProfileUpdateRequest;
import com.resumeai.auth.dto.RegisterResponse;
import com.resumeai.auth.dto.RegisterRequest;
import com.resumeai.auth.dto.ResendOtpRequest;
import com.resumeai.auth.dto.SubscriptionUpdateRequest;
import com.resumeai.auth.dto.UserValidationResponse;
import com.resumeai.auth.entity.UserAccount;
import com.resumeai.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String MESSAGE_KEY = "message";
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/verify-email")
    public RegisterResponse verifyEmail(@Valid @RequestBody EmailOtpRequest request) {
        return authService.verifyEmail(request);
    }

    @PostMapping("/resend-otp")
    public RegisterResponse resendEmailOtp(@Valid @RequestBody ResendOtpRequest request) {
        return authService.resendEmailOtp(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        return authService.login(request);
    }

    @PostMapping("/oauth/login")
    public AuthResponse oauthLogin(@Valid @RequestBody OAuthLoginRequest request) {
        return authService.oauthLogin(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String authorizationHeader) {
        authService.logout(authorizationHeader);
        Map<String, String> response = new HashMap<>();
        response.put(MESSAGE_KEY, "Logout successful");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestHeader("Authorization") String authorizationHeader) {
        return authService.refreshToken(authorizationHeader);
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, String>> validateToken(@RequestHeader("Authorization") String authorizationHeader) {
        if (authService.validateToken(authorizationHeader)) {
            Map<String, String> response = new HashMap<>();
            response.put(MESSAGE_KEY, "Token is valid");
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put(MESSAGE_KEY, "Token is invalid or expired");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @GetMapping("/profile")
    public ProfileResponse getProfile(@RequestHeader("X-User-Id") Long userId) {
        return authService.getProfile(userId);
    }

    @PutMapping("/profile")
    public ProfileResponse updateProfile(@RequestHeader("X-User-Id") Long userId,
                                         @Valid @RequestBody ProfileUpdateRequest request) {
        return authService.updateProfile(userId, request);
    }

    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(@RequestHeader("X-User-Id") Long userId,
                               @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userId, request);
        Map<String, String> response = new HashMap<>();
        response.put(MESSAGE_KEY, "Password changed successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/subscription")
    public ProfileResponse updateSubscription(@RequestHeader("X-User-Id") Long userId,
                                              @Valid @RequestBody SubscriptionUpdateRequest request) {
        return authService.updateSubscription(userId, request);
    }

    @PutMapping("/deactivate")
    public ResponseEntity<Map<String, String>> deactivateAccount(@RequestHeader("X-User-Id") Long userId) {
        authService.deactivateAccount(userId);
        Map<String, String> response = new HashMap<>();
        response.put(MESSAGE_KEY, "Account deactivated successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/by-email")
    public ProfileResponse getUserByEmail(@RequestHeader("X-User-Email") String email) {
        UserAccount user = authService.getUserByEmail(email);
        return new ProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getProvider(),
                user.getSubscriptionPlan(),
                user.isActive()
        );
    }

    @GetMapping("/users/{userId}/exists")
    public UserValidationResponse userExists(@PathVariable Long userId) {
        return new UserValidationResponse(userId, authService.userExists(userId));
    }
}
