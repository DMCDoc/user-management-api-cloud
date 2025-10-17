package com.dmcdoc.usermanagement.config.security;

import com.dmcdoc.usermanagement.core.service.UserService;
import com.dmcdoc.usermanagement.core.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final String frontendRedirect = System.getenv().getOrDefault("FRONTEND_URL", "http://localhost:4200");

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        String email = (String) principal.getAttributes().get("email");
        if (email == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email manquant du provider OAuth2");
            return;
        }

        User user = userService.findByEmailOptional(email)
                .orElseGet(() -> userService.registerWithEmailOnly(email));

        String accessToken = jwtUtils.generateToken(user);
        // Optionnel: create refresh token
        var refresh = userService.createRefreshTokenForUser(user); // si tu exposes cette méthode, sinon utiliser RefreshTokenService

        String redirectUrl = frontendRedirect + "/oauth-callback?accessToken=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8) +
                "&refreshToken=" + URLEncoder.encode(refresh.getToken(), StandardCharsets.UTF_8);

        response.sendRedirect(redirectUrl);
    }
}
