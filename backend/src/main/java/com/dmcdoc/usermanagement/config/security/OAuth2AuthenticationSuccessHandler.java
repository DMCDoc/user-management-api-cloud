package com.dmcdoc.usermanagement.config.security;

import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.service.UserService;
import com.dmcdoc.usermanagement.core.service.RefreshTokenService;
import com.dmcdoc.sharedcommon.dto.AuthResponse;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = (String) oAuth2User.getAttributes().get("email");

        if (email == null || email.isEmpty()) {
            log.error("OAuth2 login sans email");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Profil OAuth2 sans email");
            return;
        }

        User user = userService.findOrCreateByEmailOAuth2(email, oAuth2User);
        String accessToken = jwtService.generateToken(user);
        var refreshToken = refreshTokenService.create(user);

        AuthResponse tokens = new AuthResponse(accessToken, refreshToken.getToken());

        log.info("✅ OAuth2 success for {} — redirecting to frontend", email);

        // Redirection vers ton frontend Angular avec tokens (GET params)
        String redirectUrl = "http://localhost:4200/oauth2/success"
                + "?accessToken=" + tokens.getAccessToken()
                + "&refreshToken=" + tokens.getRefreshToken();

        response.sendRedirect(redirectUrl);
    }
}
