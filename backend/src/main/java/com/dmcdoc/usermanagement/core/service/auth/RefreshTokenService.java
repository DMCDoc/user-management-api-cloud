package com.dmcdoc.usermanagement.core.service.auth;

import com.dmcdoc.usermanagement.core.model.RefreshToken;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${security.jwt.refresh-expiration:2592000000}")
    private long refreshTokenDurationMs; // 30 jours

    @Transactional
    public RefreshToken create(User user) {
        System.out.println(">>> deleteByUserId=" + user.getId());
        refreshTokenRepository.deleteByUserId(user.getId());

        RefreshToken rt = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();

        return refreshTokenRepository.save(rt);
    }

    public Optional<RefreshToken> findValid(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(rt -> rt.getExpiresAt().isAfter(Instant.now()));
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expiré, veuillez vous reconnecter.");
        }
        return token;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void revokeAll(User user) {
        System.out.println(">>> revokeAll pour user.id=" + user.getId());
        refreshTokenRepository.deleteByUserId(user.getId());
    }
}
