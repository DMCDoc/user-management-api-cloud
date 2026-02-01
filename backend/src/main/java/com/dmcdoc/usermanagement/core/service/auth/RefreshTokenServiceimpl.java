package com.dmcdoc.usermanagement.core.service.auth;

import com.dmcdoc.usermanagement.core.model.RefreshToken;
import com.dmcdoc.usermanagement.core.model.User;
import com.dmcdoc.usermanagement.core.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${security.jwt.refresh-expiration:2592000000}")
    private long refreshTokenDurationMs; // 30 jours

    /**
     * Crée un nouveau refresh token pour l'utilisateur.
     * Révoque systématiquement les anciens tokens (1 token actif par user).
     */
    @Override
    public RefreshToken create(User user) {

        refreshTokenRepository.deleteByUserId(user.getId());

        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Retourne un refresh token valide (existant et non expiré).
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findValid(String token) {

        return refreshTokenRepository.findByToken(token)
                .filter(rt -> rt.getExpiresAt().isAfter(Instant.now()));
    }

    /**
     * Révoque tous les refresh tokens d’un utilisateur.
     */
    @Override
    public void revokeAll(User user) {

        refreshTokenRepository.deleteByUserId(user.getId());
    }
}
/*

✔️ Contrat respecté
✔️ Aucune méthode fantôme
✔️ Responsabilités claires
✔️ Transactionnel maîtrisé
✔️ Testable facilement
✔️ Compatible multi-tenant (via appelant)

👉 La vérification tenant ne doit PAS être ici
Elle appartient au service appelant (Auth / UserService)
👉 c’est exactement ce que tu as déjà commencé à faire 👍*/