package com.dmcdoc.usermanagement.core.service;

import com.dmcdoc.usermanagement.core.model.MagicLinkToken;
import com.dmcdoc.usermanagement.core.repository.MagicLinkTokenRepository;
import com.dmcdoc.usermanagement.core.repository.UserRepository;
import com.dmcdoc.usermanagement.config.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.Duration;
import java.util.UUID;

@Service
public class MagicLinkService {

    private final MagicLinkTokenRepository tokenRepo;
    private final EmailService emailService; // simple wrapper around JavaMailSender
    private final JwtService jwtService; // existing service in your project
    private final UserService userService; // existing service in your project

    @Value("${app.magic-link.expiration-minutes:15}")
    private long expirationMinutes;

    @Value("${app.magic-link.base-url:http://localhost:8080}")
    private String baseUrl;

    public MagicLinkService(MagicLinkTokenRepository tokenRepo,
            EmailService emailService,
            JwtService jwtService,
            UserService userService) {
        this.tokenRepo = tokenRepo;
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Transactional
    public String createAndSendMagicLink(String email) {
        String token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        Instant expiresAt = Instant.now().plus(Duration.ofMinutes(expirationMinutes));
        MagicLinkToken ml = new MagicLinkToken(token, email, expiresAt);
        tokenRepo.save(ml);

        String link = baseUrl + "/api/auth/magic-link/verify?token=" + token;
        String subject = "Your magic login link";
        String body = "Cliquez ici pour vous connecter : " + link + "\nCe lien expire dans " + expirationMinutes
                + " minutes.";

        emailService.send(email, subject, body);
        return token;
    }

    @Transactional
    public String verifyTokenAndIssueJwt(String token) {
        MagicLinkToken ml = tokenRepo.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token invalide"));

        if (ml.isUsed())
            throw new IllegalStateException("Token déjà utilisé");
        if (ml.getExpiresAt().isBefore(Instant.now()))
            throw new IllegalStateException("Token expiré");

        // marque comme utilisé
        ml.setUsed(true);
        tokenRepo.save(ml);

        // récupère ou crée l'utilisateur selon ta politique
        // ATTENTION: adapte userService aux signatures réelles de ton projet
        var user = userService.findByEmail(ml.getEmail())
                .orElseGet(() -> userService.registerWithEmailOnly(ml.getEmail()));

        // génère JWT (adapter selon JwtService)
        String jwt = jwtService.generateToken(user);
        return jwt;
    }

    // maintenance cleanup
    @Transactional
    public void cleanupExpired() {
        tokenRepo.deleteByExpiresAtBefore(Instant.now());
    }
}
