package com.dmcdoc.usermanagement.integration;

import com.dmcdoc.usermanagement.config.security.OAuth2AuthenticationSuccessHandler;
import com.dmcdoc.usermanagement.core.model.OAuth2Provider;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.service.RefreshTokenService;
import com.dmcdoc.usermanagement.core.service.UserService;
import com.dmcdoc.usermanagement.config.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OAuth2IntegrationTest {

    private UserService userService;
    private RefreshTokenService refreshTokenService;
    private JwtService jwtService;
    private OAuth2AuthenticationSuccessHandler handler;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private Authentication authentication;

    @BeforeEach
    void setup() {
        userService = mock(UserService.class);
        refreshTokenService = mock(RefreshTokenService.class);
        jwtService = mock(JwtService.class);
        handler = new OAuth2AuthenticationSuccessHandler(userService, refreshTokenService, jwtService);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        authentication = mock(Authentication.class);
    }

    @Test
    void shouldRedirectAfterSuccessfulOAuth2Login() throws IOException {
        // given
        String email = "user@example.com";
        OAuth2User oauthUser = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("email", email),
                "email");

        User mockUser = new User();
        mockUser.setEmail(email);
        mockUser.setProvider(OAuth2Provider.GOOGLE);

        when(authentication.getPrincipal()).thenReturn(oauthUser);
        when(request.getAttribute("org.springframework.security.oauth2.client.registration_id"))
                .thenReturn("google");

        when(userService.findOrCreateByEmailOAuth2(email, OAuth2Provider.GOOGLE)).thenReturn(mockUser);
        when(jwtService.generateToken(mockUser)).thenReturn("mockAccessToken");
        com.dmcdoc.usermanagement.core.model.RefreshToken refreshToken =
                mock(com.dmcdoc.usermanagement.core.model.RefreshToken.class);
        when(refreshToken.getToken()).thenReturn("mockRefreshToken");
        when(refreshTokenService.create(mockUser)).thenReturn(refreshToken);

        // when
        handler.onAuthenticationSuccess(request, response, authentication);

        // then
        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response, times(1)).sendRedirect(redirectCaptor.capture());

        String redirectUrl = redirectCaptor.getValue();
        assertThat(redirectUrl).contains("access_token=mockAccessToken");
        assertThat(redirectUrl).contains("refresh_token=mockRefreshToken");
        assertThat(redirectUrl).startsWith("http://localhost:4200/oauth2/success");
    }

    @Test
    void shouldFailIfEmailNotProvided() throws IOException {
        OAuth2User oauthUser = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of(),
                "id");
        when(authentication.getPrincipal()).thenReturn(oauthUser);

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), contains("Email non fourni"));
    }
}