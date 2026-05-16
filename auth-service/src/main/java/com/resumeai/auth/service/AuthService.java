package com.resumeai.auth.service;

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
import com.resumeai.auth.entity.UserAccount;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    RegisterResponse verifyEmail(EmailOtpRequest request);

    RegisterResponse resendEmailOtp(ResendOtpRequest request);

    AuthResponse login(AuthRequest request);

    AuthResponse oauthLogin(OAuthLoginRequest request);

    void logout(String authorizationHeader);

    boolean validateToken(String authorizationHeader);

    AuthResponse refreshToken(String authorizationHeader);

    UserAccount getUserByEmail(String email);

    ProfileResponse getProfile(Long userId);

    ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    ProfileResponse updateSubscription(Long userId, SubscriptionUpdateRequest request);

    void deactivateAccount(Long userId);

    boolean userExists(Long userId);
}
