package com.example.usermanagement.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.usermanagement.core.model.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    // ✅ on supprime par l'ID de l'utilisateur
    void deleteByUserId(Long userId);
}
