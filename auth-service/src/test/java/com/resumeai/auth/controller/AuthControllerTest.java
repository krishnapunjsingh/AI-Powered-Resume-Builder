package com.resumeai.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeai.auth.dto.*;
import com.resumeai.auth.security.CustomUserDetailsService;
import com.resumeai.auth.security.JwtService;
import com.resumeai.auth.security.OAuth2LoginSuccessHandler;
import com.resumeai.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Test
    void registerReturnsCreatedResponse() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "John Doe",
                "john.doe@example.com",
                "password123",
                "user"
        );
        RegisterResponse response = new RegisterResponse(
                "User registered successfully"
        );

        when(authService.register(request)).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(content().json(json(response)));

        verify(authService).register(request);
    }

    @Test
    void registerAdminReturnsCreatedResponse() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Admin User",
                "admin@example.com",
                "admin12345",
                "admin"
        );
        RegisterResponse response = new RegisterResponse(
                "Registration successful"
        );

        when(authService.register(request)).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(content().json(json(response)));

        verify(authService).register(request);
    }

    @Test
    void registerWithDefaultRoleReturnsCreatedResponse() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Default User",
                "default@example.com",
                "password123",
                null
        );
        RegisterResponse response = new RegisterResponse(
                "Registration successful"
        );

        when(authService.register(request)).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(content().json(json(response)));

        verify(authService).register(request);
    }

    @Test
    void loginReturnsAuthResponse() throws Exception {
        AuthRequest request = new AuthRequest("john.doe@example.com", "password123");
        AuthResponse response = new AuthResponse(
                "jwt-token",
                1L,
                "john.doe@example.com",
                "John Doe",
                "USER"
        );

        when(authService.login(request)).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(json(response)));

        verify(authService).login(request);
    }

    @Test
    void oauthLoginReturnsAuthResponse() throws Exception {
        OAuthLoginRequest request = new OAuthLoginRequest("google", "user@example.com", "John Doe");
        AuthResponse response = new AuthResponse(
                "jwt-token",
                1L,
                "john.doe@example.com",
                "John Doe",
                "USER"
        );

        when(authService.oauthLogin(request)).thenReturn(response);

        mockMvc.perform(post("/auth/oauth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(json(response)));

        verify(authService).oauthLogin(request);
    }

    @Test
    @WithMockUser
    void logoutReturnsSuccessMessage() throws Exception {
        doNothing().when(authService).logout("Bearer token");

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout successful"));

        verify(authService).logout("Bearer token");
    }

    @Test
    void refreshReturnsNewAuthResponse() throws Exception {
        AuthResponse response = new AuthResponse(
                "new-jwt-token",
                1L,
                "john.doe@example.com",
                "John Doe",
                "USER"
        );

        when(authService.refreshToken("Bearer token")).thenReturn(response);

        mockMvc.perform(post("/auth/refresh")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(content().json(json(response)));

        verify(authService).refreshToken("Bearer token");
    }

    @Test
    @WithMockUser
    void validateTokenReturnsValidMessage() throws Exception {
        when(authService.validateToken("Bearer token")).thenReturn(true);

        mockMvc.perform(post("/auth/validate")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Token is valid"));

        verify(authService).validateToken("Bearer token");
    }

    @Test
    @WithMockUser
    void validateInvalidTokenReturnsUnauthorized() throws Exception {
        when(authService.validateToken("Bearer invalid-token")).thenReturn(false);

        mockMvc.perform(post("/auth/validate")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Token is invalid or expired"));

        verify(authService).validateToken("Bearer invalid-token");
    }

    @Test
    @WithMockUser
    void getProfileReturnsProfileResponse() throws Exception {
        ProfileResponse response = new ProfileResponse(
                1L,
                "John Doe",
                "john.doe@example.com",
                "1234567890",
                "USER",
                "LOCAL",
                "BASIC",
                true
        );

        when(authService.getProfile(1L)).thenReturn(response);

        mockMvc.perform(get("/auth/profile")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json(json(response)));

        verify(authService).getProfile(1L);
    }

    @Test
    @WithMockUser
    void updateProfileReturnsUpdatedProfile() throws Exception {
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "John Updated",
                "9876543210"
        );
        ProfileResponse response = new ProfileResponse(
                1L,
                "John Updated",
                "john.doe@example.com",
                "9876543210",
                "USER",
                "LOCAL",
                "BASIC",
                true
        );

        when(authService.updateProfile(1L, request)).thenReturn(response);

        mockMvc.perform(put("/auth/profile")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(json(response)));

        verify(authService).updateProfile(1L, request);
    }

    @Test
    @WithMockUser
    void changePasswordReturnsSuccessMessage() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword", "newPassword");
        
        doNothing().when(authService).changePassword(1L, request);

        mockMvc.perform(put("/auth/password")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));

        verify(authService).changePassword(1L, request);
    }

    @Test
    @WithMockUser
    void updateSubscriptionReturnsUpdatedProfile() throws Exception {
        SubscriptionUpdateRequest request = new SubscriptionUpdateRequest("PREMIUM");
        ProfileResponse response = new ProfileResponse(
                1L,
                "John Doe",
                "john.doe@example.com",
                "1234567890",
                "USER",
                "LOCAL",
                "PREMIUM",
                true
        );

        when(authService.updateSubscription(1L, request)).thenReturn(response);

        mockMvc.perform(put("/auth/subscription")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(json(response)));

        verify(authService).updateSubscription(1L, request);
    }

    @Test
    @WithMockUser
    void deactivateAccountReturnsSuccessMessage() throws Exception {
        doNothing().when(authService).deactivateAccount(1L);

        mockMvc.perform(put("/auth/deactivate")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Account deactivated successfully"));

        verify(authService).deactivateAccount(1L);
    }

    @Test
    @WithMockUser
    void userExistsReturnsValidationResponse() throws Exception {
        UserValidationResponse response = new UserValidationResponse(1L, true);

        when(authService.userExists(1L)).thenReturn(true);

        mockMvc.perform(get("/auth/users/1/exists"))
                .andExpect(status().isOk())
                .andExpect(content().json(json(response)));

        verify(authService).userExists(1L);
    }

    @Test
    @WithMockUser
    void registerWithInvalidPayloadReturnsBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "",
                "invalid-email",
                "123",
                null
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any());
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
