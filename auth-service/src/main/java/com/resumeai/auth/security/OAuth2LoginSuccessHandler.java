package com.resumeai.auth.security;

import com.resumeai.auth.entity.UserAccount;
import com.resumeai.auth.repository.UserAccountRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final String frontendRedirectUri;

    public OAuth2LoginSuccessHandler(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${app.oauth2.frontend-redirect-uri:http://localhost:4200/auth}") String frontendRedirectUri) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.frontendRedirectUri = frontendRedirectUri;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        String email = principal.getAttribute("email");
        String fullName = principal.getAttribute("name");

        if (email == null || email.isBlank()) {
            response.sendRedirect(frontendRedirectUri + "?oauthError=email");
            return;
        }

        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseGet(() -> createGoogleUser(email, fullName));

        if (fullName != null && !fullName.isBlank()) {
            user.setFullName(fullName);
        }
        user.setProvider("GOOGLE");
        user.setActive(true);
        user.setLastLoginAt(LocalDateTime.now());
        userAccountRepository.save(user);

        String token = jwtService.generateToken(user.getEmail(), user.getId(), user.getRole());
        String redirectUrl = frontendRedirectUri
                + "?token=" + encode(token)
                + "&userId=" + user.getId()
                + "&email=" + encode(user.getEmail())
                + "&fullName=" + encode(user.getFullName())
                + "&role=" + encode(user.getRole());

        response.sendRedirect(redirectUrl);
    }

    private UserAccount createGoogleUser(String email, String fullName) {
        UserAccount user = new UserAccount();
        user.setEmail(email);
        user.setFullName((fullName == null || fullName.isBlank()) ? email : fullName);
        user.setPassword(passwordEncoder.encode("oauth2-google-user"));
        user.setProvider("GOOGLE");
        user.setRole("ROLE_USER");
        return userAccountRepository.save(user);
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
