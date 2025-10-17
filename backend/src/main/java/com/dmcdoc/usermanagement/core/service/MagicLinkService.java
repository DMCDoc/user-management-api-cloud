package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.sharedcommon.dto.AuthResponse;
import com.dmcdoc.usermanagement.config.security.JwtUtils;
import com.dmcdoc.usermanagement.core.model.MagicLinkToken;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.repository.MagicLinkTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MagicLinkService {

    private final MagicLinkTokenRepository tokenRepo;
    private final MailService mailService;
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.magic-link.expiration-minutes:15}")
    private long expirationMinutes;

    @Value("${app.magic-link.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl; // pour redirection front si besoin

    /**
     * Crée un token persisté et envoie le mail.
     */
    @Transactional
    public void createAndSendMagicLink(String email) {
        String token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        Instant expiresAt = Instant.now().plus(Duration.ofMinutes(expirationMinutes));
        MagicLinkToken ml = new MagicLinkToken(token, email, expiresAt);
        tokenRepo.save(ml);

        String link = baseUrl + "/api/auth/magic/verify?token=" + token;
        String subject = "Votre lien magique";
        String body = "Cliquez ici pour vous connecter : " + link + "\nCe lien expire dans " + expirationMinutes + " minutes.";

        mailService.sendMail(email, subject, body);
        log.info("Magic link created for {} expiresAt={}", email, expiresAt);
    }

    /**
     * Vérifie le token, marque utilisé, crée JWT + refresh token et renvoie AuthResponse.
     */
    @Transactional
    public AuthResponse verifyAndAuthenticate(String token) {
        MagicLinkToken ml = tokenRepo.findByToken(token).orElseThrow(() -> new IllegalArgumentException("Token invalide"));
        if (ml.isUsed()) throw new IllegalStateException("Token déjà utilisé");
        if (ml.getExpiresAt().isBefore(Instant.now())) throw new IllegalStateException("Token expiré");

        ml.setUsed(true);
        tokenRepo.save(ml);

        String email = ml.getEmail();

        User user = userService.findOrCreateByEmailOAuth2(email, null);

        String accessToken = jwtUtils.generateToken(user);
        var refresh = refreshTokenService.create(user);

        log.info("Magic link verified for {} -> issued tokens", email);
        return new AuthResponse(accessToken, refresh.getToken());
    }

    /**
     * Cleanup expired tokens older than now.
     */
    @Transactional
    public void cleanupExpired() {
        tokenRepo.deleteByExpiresAtBefore(Instant.now());
    }
}
