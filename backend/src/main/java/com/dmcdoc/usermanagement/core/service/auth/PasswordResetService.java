package com.dmcdoc.usermanagement.core.service.auth;

import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.repository.UserRepository;
import com.dmcdoc.usermanagement.core.repository.PasswordResetTokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class PasswordResetService {

    private final UserRepository userRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(UserRepository userRepo,
            PasswordResetTokenRepository tokenRepo,
            PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.tokenRepo = tokenRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void createPasswordResetToken(String email) {
        userRepo.findByEmail(email).ifPresent(user -> {
            PasswordResetToken token = new PasswordResetToken();
            token.setUser(user);
            token.setExpiryDate(Instant.now().plus(60, ChronoUnit.MINUTES));
            tokenRepo.save(token);
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

}