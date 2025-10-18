package com.dmcdoc.usermanagement.config.security;

import com.dmcdoc.usermanagement.core.model.OAuth2Provider;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.service.RefreshTokenService;
import com.dmcdoc.usermanagement.core.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    private final String frontendRedirectUrl = "http://localhost:4200/oauth2/success"; // adapte en prod

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = (String) oauthUser.getAttributes().get("email");

        if (email == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email non fourni par le provider OAuth2");
            return;
        }

        // Récupérer provider (ex: google, github...)
        String registrationId = (String) request
                .getAttribute("org.springframework.security.oauth2.client.registration_id");
        OAuth2Provider provider = registrationId != null
                ? OAuth2Provider.valueOf(registrationId.toUpperCase())
                : OAuth2Provider.LOCAL;

        User user = userService.findOrCreateByEmailOAuth2(email, provider);
        String accessToken = jwtService.generateToken(user);
        var refreshToken = refreshTokenService.create(user);

        log.info("Connexion OAuth2 réussie pour {} via {}", email, provider);

        String redirectUrl = String.format(
                "%s?access_token=%s&refresh_token=%s",
                frontendRedirectUrl,
                URLEncoder.encode(accessToken, StandardCharsets.UTF_8),
                URLEncoder.encode(refreshToken.getToken(), StandardCharsets.UTF_8));

        response.sendRedirect(redirectUrl);
    }
}
