package com.dmcdoc.usermanagement.integration;

import com.dmcdoc.sharedcommon.dto.AuthResponse;
import com.dmcdoc.usermanagement.core.model.MagicLinkToken;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.repository.MagicLinkTokenRepository;
import com.dmcdoc.usermanagement.core.service.MailService;
import com.dmcdoc.usermanagement.core.service.MagicLinkService;
import com.dmcdoc.usermanagement.core.service.RefreshTokenService;
import com.dmcdoc.usermanagement.core.service.UserService;
import com.dmcdoc.usermanagement.config.security.JwtUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MagicLinkIntegrationTest {

    @Mock
    private MagicLinkTokenRepository tokenRepo;

    @Mock
    private MailService mailService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserService userService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private MagicLinkService magicLinkService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateAndSendMagicLink_Success() {
        String email = "test@example.com";

        // mock save token
        when(tokenRepo.save(any(MagicLinkToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        magicLinkService.createAndSendMagicLink(email);

        // capture l'email envoyé
        ArgumentCaptor<String> mailCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailService, times(1)).sendMail(mailCaptor.capture(), anyString(),
                contains("/api/auth/magic/verify?token="));
        assertThat(mailCaptor.getValue()).isEqualTo(email);

        verify(tokenRepo, times(1)).save(any(MagicLinkToken.class));
    }

    @Test
    void testVerifyAndAuthenticate_Success() {
        String email = "test@example.com";
        String token = UUID.randomUUID().toString();
        MagicLinkToken ml = new MagicLinkToken(token, email, Instant.now().plusSeconds(900));

        when(tokenRepo.findByToken(token)).thenReturn(Optional.of(ml));

        User user = new User();
        user.setEmail(email);
        when(userService.findOrCreateByEmailOAuth2(eq(email), isNull())).thenReturn(user);

        when(jwtUtils.generateToken(any(User.class))).thenReturn("access-token");
        var refreshMock = new com.dmcdoc.usermanagement.core.model.RefreshToken();
        refreshMock.setToken("refresh-token");
        when(refreshTokenService.create(any(User.class))).thenReturn(refreshMock);

        AuthResponse response = magicLinkService.verifyAndAuthenticate(token);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(ml.isUsed()).isTrue();

        verify(tokenRepo).save(ml);
        verify(userService).findOrCreateByEmailOAuth2(email, null);
        verify(jwtUtils).generateToken(user);
        verify(refreshTokenService).create(user);
    }

    @Test
    void testVerifyAndAuthenticate_ExpiredToken() {
        String token = UUID.randomUUID().toString();
        MagicLinkToken ml = new MagicLinkToken(token, "expired@example.com", Instant.now().minusSeconds(60));

        when(tokenRepo.findByToken(token)).thenReturn(Optional.of(ml));

        assertThrows(IllegalStateException.class, () -> magicLinkService.verifyAndAuthenticate(token));
    }

    @Test
    void testVerifyAndAuthenticate_InvalidToken() {
        when(tokenRepo.findByToken(anyString())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> magicLinkService.verifyAndAuthenticate("fake-token"));
    }
}
