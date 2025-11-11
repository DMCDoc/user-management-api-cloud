package com.dmcdoc.usermanagement.core.service.auth;

import com.dmcdoc.usermanagement.core.service.mail.MailService;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.repository.UserRepository;
import com.dmcdoc.usermanagement.core.repository.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class PasswordResetService {

    private final UserRepository userRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final String frontendUrl;

    public PasswordResetService(UserRepository userRepo,
            PasswordResetTokenRepository tokenRepo,
            MailService mailService,
            PasswordEncoder passwordEncoder,
            @Value("${app.frontend.url:http://localhost}") String frontendUrl) {
        this.userRepo = userRepo;
        this.tokenRepo = tokenRepo;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
        this.frontendUrl = frontendUrl;
    }

    @Transactional
    public void createPasswordResetToken(String email) {
        userRepo.findByEmail(email).ifPresent(user -> {
            PasswordResetToken token = new PasswordResetToken();
            token.setUser(user);
            token.setExpiryDate(Instant.now().plus(60, ChronoUnit.MINUTES));
            tokenRepo.save(token);
            String link = frontendUrl + "/reset-password?token=" + token.getToken();
            String html = buildEmailHtml(user.getFullName(), link);
            mailService.sendHtml(user.getEmail(), "Réinitialisation de votre mot de passe", html);
        });
        // always return success to caller (avoid leaking account existence)
    }

    @Transactional
    public void resetPassword(String tokenString, String newPassword) {
        PasswordResetToken token = tokenRepo.findByToken(tokenString)
                .orElseThrow(() -> new IllegalArgumentException("Token invalide"));
        if (token.isExpired()) {
            tokenRepo.delete(token);
            throw new IllegalArgumentException("Token expiré");
        }
        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
        tokenRepo.delete(token);
    }

    private String buildEmailHtml(String name, String link) {
        String n = name == null ? "" : name;
        return "<p>Bonjour " + escapeHtml(n) + ",</p>"
                + "<p>Cliquez sur le lien pour réinitialiser votre mot de passe :</p>"
                + "<p><a href=\"" + link + "\">Réinitialiser mon mot de passe</a></p>"
                + "<p>Si vous n'avez pas demandé cette action, ignorez cet e-mail.</p>";
    }

    private String escapeHtml(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}