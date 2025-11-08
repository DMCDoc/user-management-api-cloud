package com.dmcdoc.usermanagement.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.dmcdoc.usermanagement.core.service.auth.PasswordResetToken;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {
    Optional<PasswordResetToken> findByToken(String token);

    void deleteByToken(String token);
}
