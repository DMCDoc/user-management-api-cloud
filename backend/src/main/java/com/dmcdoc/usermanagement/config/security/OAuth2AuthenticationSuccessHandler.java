package com.dmcdoc.usermanagement.config.security;

import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.service.RefreshTokenService;
import com.dmcdoc.usermanagement.core.service.UserService;
import jakarta.servlet.ServletException;
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

    private final String frontendRedirectUrl = "http://localhost:4200/oauth2/success"; // adapt si besoin

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = (String) oauthUser.getAttributes().get("email");

        if (email == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email non fourni par le provider OAuth2");
            return;
        }

        User user = userService.findOrCreateByEmailOAuth2(email, null);

        String jwt = jwtService.generateToken(user);
        var refresh = refreshTokenService.create(user);

        log.info("OAuth2 success login for {} -> JWT issued", email);

        String redirectUrl = String.format("%s?access_token=%s&refresh_token=%s",
                frontendRedirectUrl,
                URLEncoder.encode(jwt, StandardCharsets.UTF_8),
                URLEncoder.encode(refresh.getToken(), StandardCharsets.UTF_8));

        response.sendRedirect(redirectUrl);
    }
}
