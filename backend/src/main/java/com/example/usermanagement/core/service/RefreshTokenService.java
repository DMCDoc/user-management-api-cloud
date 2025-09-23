package com.example.usermanagement.core.service;

import com.example.usermanagement.core.model.RefreshToken;
import com.example.usermanagement.core.model.User;
import com.example.usermanagement.core.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${security.jwt.refresh-expiration:2592000000}")
    private long refreshTokenDurationMs; // 30 jours

    @Transactional
    public RefreshToken create(User user) {
        System.out.println(">>> deleteByUserId=" + user.getId());
        // ✅ supprime les anciens tokens via l’ID
        refreshTokenRepository.deleteByUserId(user.getId());

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

    @Transactional(propagation = Propagation.REQUIRED)
    public void revokeAll(User user) {
        System.out.println(">>> revokeAll pour user.id=" + user.getId());
        // ✅ idem ici
        refreshTokenRepository.deleteByUserId(user.getId());
    }
}
