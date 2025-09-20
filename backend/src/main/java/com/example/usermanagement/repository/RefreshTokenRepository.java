package com.example.usermanagement.repository;

import com.example.usermanagement.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    // ✅ on supprime par l'ID de l'utilisateur
    void deleteByUserId(Long userId);
}
