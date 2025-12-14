package com.dmcdoc.usermanagement.integration;

import com.dmcdoc.usermanagement.config.security.JwtService;
import com.dmcdoc.usermanagement.config.security.OAuth2AuthenticationSuccessHandler;
import com.dmcdoc.usermanagement.core.model.OAuth2Provider;
import com.dmcdoc.usermanagement.core.model.Role;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.repository.RoleRepository;
import com.dmcdoc.usermanagement.core.repository.UserRepository;
import com.dmcdoc.usermanagement.core.service.auth.RefreshTokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

class OAuth2IntegrationTest {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private RefreshTokenService refreshTokenService;
    private JwtService jwtService;
    private com.dmcdoc.usermanagement.tenant.provisioning.TenantProvisioningService tenantProvisioningService;
    private OAuth2AuthenticationSuccessHandler handler;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private Authentication authentication;

    private PrintWriter writer;

    @BeforeEach
    void setup() throws IOException {
        userRepository = mock(UserRepository.class);
        refreshTokenService = mock(RefreshTokenService.class);
        jwtService = mock(JwtService.class);
        tenantProvisioningService = mock(com.dmcdoc.usermanagement.tenant.provisioning.TenantProvisioningService.class);
        handler = new OAuth2AuthenticationSuccessHandler(userRepository, refreshTokenService,
                jwtService, tenantProvisioningService);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        authentication = mock(Authentication.class);
        writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);
    }

@Test
void shouldReuseExistingUserIfAlreadyExists() throws Exception {
    String email = "existing@example.com";
    OAuth2User oauthUser = new DefaultOAuth2User(
            List.of(new SimpleGrantedAuthority("GITHUB")),
            Map.of("email", email),
            "email");

    when(authentication.getPrincipal()).thenReturn(oauthUser);
    doReturn(List.of(new SimpleGrantedAuthority("GOOGLE"))).when(authentication).getAuthorities();

    User existingUser = User.builder()
            .id(UUID.randomUUID())
            .email(email)
            .username("existing")
            .provider(OAuth2Provider.GITHUB)
            .build();

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
    when(jwtService.generateToken(existingUser)).thenReturn("existingAccessToken");

    var refreshToken = mock(com.dmcdoc.usermanagement.core.model.RefreshToken.class);
    when(refreshToken.getToken()).thenReturn("existingRefreshToken");
    when(refreshTokenService.create(existingUser)).thenReturn(refreshToken);

    handler.onAuthenticationSuccess(request, response, authentication);

    verify(userRepository, never()).save(any());
    verify(response).setStatus(HttpServletResponse.SC_OK);
    verify(writer).write(contains("existingAccessToken"));
    verify(writer).write(contains("existingRefreshToken"));
}


    @Test
    void shouldReturnJsonTokensOnSuccess() throws Exception {
        // given
        String email = "user@example.com";
        OAuth2User oauthUser = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("GOOGLE")),
                Map.of("email", email),
                "email");

        when(authentication.getPrincipal()).thenReturn(oauthUser);
        doReturn(List.of(new SimpleGrantedAuthority("GOOGLE"))).when(authentication).getAuthorities();



        Role userRole = Role.builder().id(UUID.randomUUID()).name("ROLE_USER").build();
        User newUser = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .username("user")
                .roles(Set.of(userRole))
                .provider(OAuth2Provider.GOOGLE)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("mockAccessToken");

        var refreshToken = mock(com.dmcdoc.usermanagement.core.model.RefreshToken.class);
        when(refreshToken.getToken()).thenReturn("mockRefreshToken");
        when(refreshTokenService.create(any(User.class))).thenReturn(refreshToken);

        // when
        handler.onAuthenticationSuccess(request, response, authentication);

        // then
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(response).setContentType("application/json");

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(jsonCaptor.capture());

        String json = jsonCaptor.getValue();
        assertThat(json).contains("mockAccessToken");
        assertThat(json).contains("mockRefreshToken");
    }

    @Test
    void shouldThrowIfEmailMissing() {
        OAuth2User oauthUser = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("GOOGLE")),
                Map.of(),
                "id");

        when(authentication.getPrincipal()).thenReturn(oauthUser);

        try {
            handler.onAuthenticationSuccess(request, response, authentication);
        } catch (Exception e) {
            assertThat(e).hasMessageContaining("Email manquant");
        }
    }
}
