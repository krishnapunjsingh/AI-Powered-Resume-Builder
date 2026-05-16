package com.resumeai.auth.service.impl;

import com.resumeai.auth.client.NotificationServiceClient;
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
import com.resumeai.auth.exception.EmailAlreadyExistsException;
import com.resumeai.auth.exception.InactiveUserException;
import com.resumeai.auth.exception.InvalidCredentialsException;
import com.resumeai.auth.exception.InvalidOtpException;
import com.resumeai.auth.exception.InvalidSubscriptionPlanException;
import com.resumeai.auth.exception.InvalidTokenException;
import com.resumeai.auth.exception.UnsupportedProviderException;
import com.resumeai.auth.exception.UserNotFoundException;
import com.resumeai.auth.repository.UserAccountRepository;
import com.resumeai.auth.security.JwtService;
import com.resumeai.auth.security.TokenBlacklistService;
import com.resumeai.auth.service.AuthService;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Locale;
import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;
    private final NotificationServiceClient notificationServiceClient;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userAccountRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException();
        }

        UserAccount user = new UserAccount();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setProvider("LOCAL");
        String otp = createOtp();
        user.setEmailVerified(false);
        user.setEmailOtpHash(passwordEncoder.encode(otp));
        user.setEmailOtpExpiresAt(LocalDateTime.now().plusMinutes(10));
        
        // Set role - default to ROLE_USER if not specified
        String role = request.role();
        if (role == null || role.trim().isEmpty()) {
            role = "ROLE_USER";
        } else if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role.toUpperCase();
        }
        user.setRole(role);
        
        UserAccount savedUser = userAccountRepository.save(user);
        sendEmailVerification(savedUser, otp);

        return new RegisterResponse("Registration successful. Please verify the OTP sent to your email.");
    }

    @Override
    @Transactional
    public RegisterResponse verifyEmail(EmailOtpRequest request) {
        UserAccount user = userAccountRepository.findByEmail(request.email())
                .orElseThrow(UserNotFoundException::new);

        if (user.isEmailVerified()) {
            return new RegisterResponse("Email already verified");
        }

        if (user.getEmailOtpHash() == null
                || user.getEmailOtpExpiresAt() == null
                || user.getEmailOtpExpiresAt().isBefore(LocalDateTime.now())
                || !passwordEncoder.matches(request.otp(), user.getEmailOtpHash())) {
            throw new InvalidOtpException();
        }

        user.setEmailVerified(true);
        user.setEmailOtpHash(null);
        user.setEmailOtpExpiresAt(null);
        userAccountRepository.save(user);

        sendAccountNotification(user, "EMAIL_VERIFIED", "Email verified", "Your ResumeAI account email has been verified.");
        return new RegisterResponse("Email verified successfully");
    }

    @Override
    @Transactional
    public RegisterResponse resendEmailOtp(ResendOtpRequest request) {
        UserAccount user = userAccountRepository.findByEmail(request.email())
                .orElseThrow(UserNotFoundException::new);

        if (user.isEmailVerified()) {
            return new RegisterResponse("Email already verified");
        }

        String otp = createOtp();
        user.setEmailOtpHash(passwordEncoder.encode(otp));
        user.setEmailOtpExpiresAt(LocalDateTime.now().plusMinutes(10));
        UserAccount savedUser = userAccountRepository.save(user);
        sendEmailVerification(savedUser, otp);

        return new RegisterResponse("A new OTP has been sent to your email.");
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserAccount user = userAccountRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.isActive()) {
            throw new InactiveUserException();
        }
        user.setLastLoginAt(java.time.LocalDateTime.now());
        userAccountRepository.save(user);

        String token = jwtService.generateToken(user.getEmail(), user.getId(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getFullName(), user.getRole());
    }

    @Override
    @Transactional
    public AuthResponse oauthLogin(OAuthLoginRequest request) {
        String provider = normalizeProvider(request.provider());

        UserAccount user = userAccountRepository.findByEmail(request.email())
            .orElseGet(() -> {
                UserAccount newUser = new UserAccount();
                newUser.setFullName(request.fullName());
                newUser.setEmail(request.email());
                newUser.setPassword(passwordEncoder.encode("oauth-user"));
                newUser.setProvider(provider);
                newUser.setEmailVerified(true);
                return userAccountRepository.save(newUser);
            });

        user.setProvider(provider);
        user.setActive(true);
        user.setEmailVerified(true);
        user.setLastLoginAt(java.time.LocalDateTime.now());
        userAccountRepository.save(user);

        String token = jwtService.generateToken(user.getEmail(), user.getId(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getFullName(), user.getRole());
    }

    @Override
    public void logout(String authorizationHeader) {
        String token = jwtService.stripBearerPrefix(authorizationHeader);
        if (!isTokenValid(token)) {
            throw new InvalidTokenException();
        }
        Instant expiration = jwtService.extractExpiration(token).toInstant();
        tokenBlacklistService.blacklist(token, expiration);
    }

    @Override
    public boolean validateToken(String authorizationHeader) {
        String token = jwtService.stripBearerPrefix(authorizationHeader);
        return isTokenValid(token);
    }

    @Override
    public AuthResponse refreshToken(String authorizationHeader) {
        String token = jwtService.stripBearerPrefix(authorizationHeader);
        if (!isTokenValid(token)) {
            throw new InvalidTokenException();
        }

        UserAccount user = getUserByEmail(jwtService.extractUsername(token));
        String newToken = jwtService.generateToken(user.getEmail(), user.getId(), user.getRole());
        return new AuthResponse(newToken, user.getId(), user.getEmail(), user.getFullName(), user.getRole());
    }

    @Override
    @Transactional(readOnly = true)
    public UserAccount getUserByEmail(String email) {
        return userAccountRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long userId) {
        UserAccount user = getUserById(userId);
        return toProfileResponse(user);
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        UserAccount user = getUserById(userId);
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        ProfileResponse response = toProfileResponse(userAccountRepository.save(user));
        sendAccountNotification(user, "PROFILE_UPDATED", "Profile updated", "Your ResumeAI profile details were updated.");
        return response;
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        UserAccount user = getUserById(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userAccountRepository.save(user);
        sendAccountNotification(user, "PASSWORD_CHANGED", "Password changed", "Your ResumeAI account password was changed.");
    }

    @Override
    @Transactional
    public ProfileResponse updateSubscription(Long userId, SubscriptionUpdateRequest request) {
        UserAccount user = getUserById(userId);
        String normalizedPlan = normalizePlan(request.subscriptionPlan());
        user.setSubscriptionPlan(normalizedPlan);
        ProfileResponse response = toProfileResponse(userAccountRepository.save(user));
        sendAccountNotification(user, "SUBSCRIPTION_UPDATED", "Subscription updated",
                "Your ResumeAI subscription plan is now " + normalizedPlan + ".");
        return response;
    }

    @Override
    @Transactional
    public void deactivateAccount(Long userId) {
        UserAccount user = getUserById(userId);
        user.setActive(false);
        userAccountRepository.save(user);
        sendAccountNotification(user, "ACCOUNT_DEACTIVATED", "Account deactivated", "Your ResumeAI account was deactivated.");
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userExists(Long userId) {
        return userAccountRepository.existsById(userId);
    }

    private UserAccount getUserById(Long userId) {
        return userAccountRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    private String normalizeProvider(String provider) {
        String normalized = provider.toUpperCase(Locale.ROOT);
        if (!"GOOGLE".equals(normalized) && !"LINKEDIN".equals(normalized)) {
            throw new UnsupportedProviderException();
        }
        return normalized;
    }

    private String normalizePlan(String subscriptionPlan) {
        String normalized = subscriptionPlan.toUpperCase(Locale.ROOT);
        if (!"FREE".equals(normalized) && !"PREMIUM".equals(normalized)) {
            throw new InvalidSubscriptionPlanException();
        }
        return normalized;
    }

    private ProfileResponse toProfileResponse(UserAccount user) {
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

    private String createOtp() {
        return String.valueOf(100000 + secureRandom.nextInt(900000));
    }

    private void sendEmailVerification(UserAccount user, String otp) {
        sendAccountNotification(
                user,
                "EMAIL_OTP",
                "Verify your ResumeAI email",
                "Your ResumeAI verification OTP is " + otp + ". It expires in 10 minutes."
        );
    }

    private void sendAccountNotification(UserAccount user, String type, String title, String message) {
        notificationServiceClient.send(new NotificationServiceClient.NotificationRequest(
                user.getId(),
                user.getEmail(),
                type,
                title,
                message,
                "EMAIL",
                user.getId(),
                "USER"
        ));
    }

    private boolean isTokenValid(String token) {
        if (token == null || token.isBlank() || tokenBlacklistService.isBlacklisted(token)) {
            return false;
        }
        try {
            String username = jwtService.extractUsername(token);
            return jwtService.isTokenValid(token, username);
        } catch (Exception exception) {
            return false;
        }
    }
}
