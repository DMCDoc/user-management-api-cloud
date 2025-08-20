package com.example.usermanagement.service;

import com.example.usermanagement.model.RefreshToken;
import com.example.usermanagement.model.User;
import com.example.usermanagement.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    // Durée du refresh token (ici 30 jours)
    private static final long REFRESH_TOKEN_DAYS = 30;

    public RefreshToken create(User user) {
        // (optionnel) révoquer les précédents refresh tokens de l’utilisateur
        refreshTokenRepository.deleteByUser(user);

        RefreshToken rt = RefreshToken.builder().user(user).token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plus(REFRESH_TOKEN_DAYS, ChronoUnit.DAYS)).build();

        return refreshTokenRepository.save(rt);
    }

    public Optional<RefreshToken> findValid(String token) {
        return refreshTokenRepository.findByToken(token).filter(rt -> rt.getExpiryDate().isAfter(Instant.now()));
    }

    public void revokeAll(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
