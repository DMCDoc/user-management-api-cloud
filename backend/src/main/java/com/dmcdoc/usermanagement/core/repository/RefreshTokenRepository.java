package com.dmcdoc.usermanagement.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dmcdoc.usermanagement.core.model.RefreshToken;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    // ✅ on cherche par le token
    Optional<RefreshToken> findByToken(String token);

    // ✅ on supprime par l'ID de l'utilisateur
    void deleteByUserId(UUID userId);
}
