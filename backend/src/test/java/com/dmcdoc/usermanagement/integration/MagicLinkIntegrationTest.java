package com.dmcdoc.usermanagement.integration;

import com.dmcdoc.sharedcommon.dto.AuthResponse;
import com.dmcdoc.usermanagement.core.model.MagicLinkToken;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.repository.MagicLinkTokenRepository;
import com.dmcdoc.usermanagement.core.repository.UserRepository;
import com.dmcdoc.usermanagement.core.service.MagicLinkService;
import com.dmcdoc.usermanagement.config.security.JwtService;
import com.dmcdoc.usermanagement.core.service.MailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MagicLinkIntegrationTest {

    @Autowired

    private MagicLinkService magicLinkService;
    private MagicLinkTokenRepository tokenRepo;
    private UserRepository userRepo;
    private MailService mailService;
    private JwtService jwtService;



    @BeforeEach
    void setup() {
        tokenRepo = mock(MagicLinkTokenRepository.class);
        userRepo = mock(UserRepository.class);
        mailService = mock(MailService.class);
        jwtService = mock(JwtService.class);

        
    }

    @Test
    void shouldCreateTokenAndSendMagicLink() {
        String email = "demo@example.com";

        // when
        magicLinkService.createAndSendMagicLink(email);

        // then
        ArgumentCaptor<MagicLinkToken> tokenCaptor = ArgumentCaptor.forClass(MagicLinkToken.class);
        verify(tokenRepo).save(tokenCaptor.capture());
        MagicLinkToken token = tokenCaptor.getValue();

        assertThat(token.getEmail()).isEqualTo(email);
        assertThat(token.getToken()).isNotBlank();
        assertThat(token.getExpiresAt()).isAfter(Instant.now());
        assertThat(token.isUsed()).isFalse();

        verify(mailService, times(1))
                .sendMagicLink(to, subject, text), contains("/api/auth/magic/verify?token="));
    }

    @Test
    void shouldVerifyAndAuthenticateValidToken() {
        String email = "demo@example.com";
        String tokenValue = UUID.randomUUID().toString();
        MagicLinkToken token = new MagicLinkToken();
        token.setEmail(email);
        token.setToken(tokenValue);
        token.setExpiresAt(Instant.now().plusSeconds(900));
        token.setUsed(false);

        User user = new User();
        user.setEmail(email);

        when(tokenRepo.findByToken(tokenValue)).thenReturn(Optional.of(token));
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("mockAccess");
        when(jwtService.generateRefreshToken(user)).thenReturn("mockRefresh");

        // when
        AuthResponse resp = magicLinkService.verifyAndAuthenticate(tokenValue);

        // then
        assertThat(resp.getAccessToken()).isEqualTo("mockAccess");
        assertThat(resp.getRefreshToken()).isEqualTo("mockRefresh");
        assertThat(resp.getEmail()).isEqualTo(email);

        verify(tokenRepo).save(argThat(t -> t.isUsed()));
    }

    @Test
    void shouldRejectExpiredToken() {
        MagicLinkToken token = new MagicLinkToken();
        token.setToken("expired");
        token.setEmail("x@example.com");
        token.setExpiresAt(Instant.now().minusSeconds(10));
        token.setUsed(false);

        when(tokenRepo.findByToken("expired")).thenReturn(Optional.of(token));

        try {
            magicLinkService.verifyAndAuthenticate("expired");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).contains("expiré");
        }
    }

    @Test
    void shouldRejectAlreadyUsedToken() {
        MagicLinkToken token = new MagicLinkToken();
        token.setToken("used");
        token.setEmail("x@example.com");
        token.setExpiresAt(Instant.now().plusSeconds(100));
        token.setUsed(true);

        when(tokenRepo.findByToken("used")).thenReturn(Optional.of(token));

        try {
            magicLinkService.verifyAndAuthenticate("used");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).contains("déjà utilisé");
        }
    }
}