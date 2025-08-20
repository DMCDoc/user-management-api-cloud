package com.example.usermanagement.service;

import com.example.usermanagement.model.RefreshToken;
import com.example.usermanagement.model.User;
import com.example.usermanagement.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${security.jwt.refresh-expiration:2592000000}") // par défaut 30
                                                            // jours (30 * 24h *
                                                            // 60m * 60s *
                                                            // 1000ms)
    private long refreshTokenDurationMs;

    public RefreshToken create(User user) {
        refreshTokenRepository.deleteByUser(user);

        RefreshToken rt = RefreshToken.builder().user(user).token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs)).build();

        return refreshTokenRepository.save(rt);
    }

    public Optional<RefreshToken> findValid(String token) {
        return refreshTokenRepository.findByToken(token).filter(rt -> rt.getExpiryDate().isAfter(Instant.now()));
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expiré, veuillez vous reconnecter.");
        }
        return token;
    }

    public void revokeAll(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
