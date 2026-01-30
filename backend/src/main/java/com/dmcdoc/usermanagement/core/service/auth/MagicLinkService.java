package com.dmcdoc.usermanagement.core.service.auth;

import com.dmcdoc.sharedcommon.dto.AuthResponse;
import com.dmcdoc.usermanagement.config.security.JwtService;
import com.dmcdoc.usermanagement.core.model.MagicLinkToken;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.repository.MagicLinkTokenRepository;
import com.dmcdoc.usermanagement.core.service.UserServiceImpl;
import com.dmcdoc.usermanagement.core.service.mail.MailService;

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
    private final JwtService jwtService;
    private final UserServiceImpl userService;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.magic-link.expiration-minutes:15}")
    private long expirationMinutes;

    @Value("${app.magic-link.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl; // pour redirection front si besoin

    /** Crée un token persisté et envoie le mail HTML */
    @Transactional
    public void createAndSendMagicLink(String email) {
        String token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        Instant expiresAt = Instant.now().plus(Duration.ofMinutes(expirationMinutes));
        MagicLinkToken ml = new MagicLinkToken(token, email, expiresAt);
        tokenRepo.save(ml);

        String link = baseUrl + "/api/auth/magic/verify?token=" + token;
        String subject = "Votre lien magique de connexion";

        String html = "<p>Bonjour,</p>"
                + "<p>Cliquez sur le lien ci-dessous pour vous connecter :</p>"
                + "<p><a href=\"" + link + "\">Se connecter</a></p>"
                + "<p>Ce lien expire dans <b>" + expirationMinutes + "</b> minutes.</p>"
                + "<p>Si vous n'êtes pas à l'origine de cette demande, ignorez cet e-mail.</p>";

        mailService.sendHtml(email, subject, html);
        log.info("Magic link created for {} expiresAt={}", email, expiresAt);
    }

    /**
     * Vérifie le token, marque utilisé, crée JWT + refresh token et renvoie
     * AuthResponse
     */
    @Transactional
    public AuthResponse verifyAndAuthenticate(String token) {
        MagicLinkToken ml = tokenRepo.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token invalide"));
        if (ml.isUsed())
            throw new IllegalStateException("Token déjà utilisé");
        if (ml.getExpiresAt().isBefore(Instant.now()))
            throw new IllegalStateException("Token expiré");

        ml.setUsed(true);
        tokenRepo.save(ml);

        String email = ml.getEmail();

        User user = userService.findOrCreateByEmailOAuth2(
                email,
                null,
                com.dmcdoc.usermanagement.tenant.TenantContext.getTenantId());
        String accessToken = jwtService.generateToken(user);
        var refresh = refreshTokenService.create(user);

        log.info("Magic link verified for {} -> issued tokens", email);
        return new AuthResponse(accessToken, refresh.getToken(), user.getEmail());
    }

    /** Supprime les tokens expirés */
    @Transactional
    public void cleanupExpired() {
        tokenRepo.deleteByExpiresAtBefore(Instant.now());
    }
}
